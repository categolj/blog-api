package am.ik.blog.admin.web;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryService;
import am.ik.blog.github.EntryFetcher;
import am.ik.blog.github.GitHubProps;
import am.ik.blog.util.Tuple2;
import am.ik.blog.util.Tuples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpClientErrorException;

import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "admin")
public class EntryImportController {

	private final EntryFetcher entryFetcher;

	private final EntryService entryService;

	private final GitHubProps props;

	private final Logger log = LoggerFactory.getLogger(EntryImportController.class);

	public EntryImportController(EntryFetcher entryFetcher, EntryService entryService, GitHubProps props) {
		this.entryFetcher = entryFetcher;
		this.entryService = entryService;
		this.props = props;
	}

	@PostMapping(path = "/admin/import", produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	@Operation(security = { @SecurityRequirement(name = "basic") })
	public List<String> importEntries(@RequestParam(defaultValue = "0") int from,
			@RequestParam(defaultValue = "0") int to) {
		return this.importEntriesForTenant(from, to, null);
	}

	@PostMapping(path = "/tenants/{tenantId}/admin/import", produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	@Operation(security = { @SecurityRequirement(name = "basic") })
	public List<String> importEntriesForTenant(@RequestParam(defaultValue = "0") int from,
			@RequestParam(defaultValue = "0") int to,
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId) {
		final Tuple2<String, String> ownerAndRepo = this.getOwnerAndRepo(tenantId);
		log.info("Importing entries from https://github.com/{}/{} ({}-{})", ownerAndRepo.getT1(), ownerAndRepo.getT2(),
				from, to);
		return IntStream.rangeClosed(from, to).boxed().map(entryId -> {
			try {
				return this.entryFetcher.fetch(tenantId, ownerAndRepo.getT1(), ownerAndRepo.getT2(),
						String.format("content/%05d.md", entryId));
			}
			catch (HttpClientErrorException e) {
				log.warn(e.getMessage(), e);
				return Optional.<Entry>empty();
			}
		})
			.filter(Optional::isPresent)
			.map(Optional::get)
			.peek(entry -> this.entryService.save(entry, tenantId))
			.map(e -> e.getEntryId() + " " + e.getFrontMatter().getTitle())
			.toList();
	}

	private Tuple2<String, String> getOwnerAndRepo(@Nullable String tenantId) {
		if (tenantId == null) {
			return Tuples.of(this.props.getContentOwner(), this.props.getContentRepo());
		}
		else {
			final GitHubProps props = this.props.getTenants().get(tenantId);
			if (props == null) {
				return this.getOwnerAndRepo(null);
			}
			return Tuples.of(Objects.requireNonNullElse(props.getContentOwner(), this.props.getContentOwner()),
					Objects.requireNonNullElse(props.getContentRepo(), this.props.getContentRepo()));
		}
	}

}

package am.ik.blog.admin.web;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryService;
import am.ik.blog.github.EntryFetcher;
import am.ik.blog.github.GitHubProps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;

@RestController
@Tag(name = "admin")
public class EntryImportController {
	private final EntryFetcher entryFetcher;

	private final EntryService entryService;

	private final GitHubProps props;

	private final Logger log = LoggerFactory.getLogger(EntryImportController.class);

	public EntryImportController(EntryFetcher entryFetcher, EntryService entryService,
			GitHubProps props) {
		this.entryFetcher = entryFetcher;
		this.entryService = entryService;
		this.props = props;
	}

	@PostMapping(path = "/admin/import", produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	@Operation(security = { @SecurityRequirement(name = "basic") })
	public Optional<List<String>> importEntries(
			@RequestParam(defaultValue = "0") int from,
			@RequestParam(defaultValue = "0") int to) {
		return this.importEntriesForTenant(from, to, null);
	}

	@PostMapping(path = "/tenants/{tenantId}/admin/import", produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	@Operation(security = { @SecurityRequirement(name = "basic") })
	public Optional<List<String>> importEntriesForTenant(
			@RequestParam(defaultValue = "0") int from,
			@RequestParam(defaultValue = "0") int to,
			@PathVariable(name = "tenantId", required = false) String tenantId) {
		final Tuple2<String, String> ownerAndRepo = this.getOwnerAndRepo(tenantId);
		log.info("Importing entries from https://github.com/{}/{} ({}-{})",
				ownerAndRepo.getT1(), ownerAndRepo.getT2(), from, to);
		final Optional<List<Entry>> fetched = Flux
				.fromStream(IntStream.rangeClosed(from, to).boxed())
				.flatMap(i -> this.entryFetcher
						.fetch(tenantId, ownerAndRepo.getT1(), ownerAndRepo.getT2(),
								String.format("content/%05d.md", i))
						.onErrorResume(e -> (e instanceof NotFound) ? Mono.empty()
								: Mono.error(e)),
						8)
				.collectList()
				// blocking intentionally so that trace id is properly propagated
				.blockOptional();
		return fetched.map(entries -> entries.stream()
				.peek(entry -> this.entryService.save(entry, tenantId))
				.map(e -> e.getEntryId() + " " + e.getFrontMatter().getTitle()).toList());
	}

	private Tuple2<String, String> getOwnerAndRepo(String tenantId) {
		if (tenantId == null) {
			return Tuples.of(this.props.getContentOwner(), this.props.getContentRepo());
		}
		else {
			final GitHubProps props = this.props.getTenants().get(tenantId);
			if (props == null) {
				return this.getOwnerAndRepo(null);
			}
			return Tuples.of(
					Objects.requireNonNullElse(props.getContentOwner(),
							this.props.getContentOwner()),
					Objects.requireNonNullElse(props.getContentRepo(),
							this.props.getContentRepo()));
		}
	}
}

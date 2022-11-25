package am.ik.blog.admin.web;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryService;
import am.ik.blog.github.EntryFetcher;
import am.ik.blog.github.GitHubProps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;

@RestController
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
	public Optional<List<String>> importEntries(
			@RequestParam(defaultValue = "0") int from,
			@RequestParam(defaultValue = "0") int to) {
		log.info("Importing entries from https://github.com/{}/{} ({}-{})", this.props.getContentOwner(), this.props.getContentRepo(), from, to);
		final Optional<List<Entry>> fetched = Flux.fromStream(IntStream.rangeClosed(from, to).boxed())
				.flatMap(i -> this.entryFetcher
						.fetch(this.props.getContentOwner(), this.props.getContentRepo(), String.format("content/%05d.md", i))
						.onErrorResume(
								e -> (e instanceof NotFound)
										? Mono.empty()
										: Mono.error(e)))
				.collectList()
				// blocking intentionally so that trace id is properly propagated
				.blockOptional();
		return fetched
				.map(entries -> entries
						.stream()
						.peek(this.entryService::save)
						.map(e -> e.getEntryId() + " " + e.getFrontMatter().getTitle())
						.toList());
	}
}

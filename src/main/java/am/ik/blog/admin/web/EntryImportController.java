package am.ik.blog.admin.web;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryMapper;
import am.ik.blog.github.EntryFetcher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;

@RestController
public class EntryImportController {
	private final EntryFetcher entryFetcher;

	private final EntryMapper entryMapper;

	public EntryImportController(EntryFetcher entryFetcher, EntryMapper entryMapper) {
		this.entryFetcher = entryFetcher;
		this.entryMapper = entryMapper;
	}

	@PostMapping(path = "/admin/import", produces = MediaType.APPLICATION_JSON_VALUE)
	public Optional<List<String>> importEntries(
			@RequestParam(defaultValue = "0") int from,
			@RequestParam(defaultValue = "0") int to,
			@RequestParam(defaultValue = "making") String owner,
			@RequestParam(defaultValue = "blog.ik.am") String repo) {
		final Optional<List<Entry>> fetched = Flux.fromStream(IntStream.rangeClosed(from, to).boxed())
				.flatMap(i -> this.entryFetcher
						.fetch(owner, repo, String.format("content/%05d.md", i))
						.log("entry")
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
						.peek(this.entryMapper::save)
						.map(e -> e.getEntryId() + " " + e.getFrontMatter().getTitle())
						.toList());
	}
}

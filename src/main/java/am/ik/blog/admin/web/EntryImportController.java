package am.ik.blog.admin.web;

import java.util.stream.IntStream;

import am.ik.blog.entry.EntryMapper;
import am.ik.blog.github.EntryFetcher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestController
public class EntryImportController {
	private final EntryFetcher entryFetcher;

	private final EntryMapper entryMapper;

	public EntryImportController(EntryFetcher entryFetcher, EntryMapper entryMapper) {
		this.entryFetcher = entryFetcher;
		this.entryMapper = entryMapper;
	}

	@PostMapping(path = "/admin/import", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> importEntries(
			@RequestParam(defaultValue = "0") int from,
			@RequestParam(defaultValue = "0") int to,
			@RequestParam(defaultValue = "making") String owner,
			@RequestParam(defaultValue = "blog.ik.am") String repo) {
		return Flux.fromStream(IntStream.rangeClosed(from, to).boxed())
				.flatMap(i -> this.entryFetcher
								.fetch(owner, repo, String.format("content/%05d.md", i))
								.onErrorResume(
										e -> (e instanceof WebClientResponseException.NotFound)
												? Mono.empty()
												: Mono.error(e)),
						2) //
				.flatMap(this.entryMapper::save, 4) //
				.map(e -> e.getEntryId() + " " + e.getFrontMatter().getTitle());
	}
}

package am.ik.blog.admin;

import java.util.stream.IntStream;

import am.ik.blog.github.EntryFetcher;
import am.ik.blog.reactive.EntryMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Component
public class EntryImportHandler {
	private final EntryFetcher entryFetcher;
	private final EntryMapper entryMapper;

	public EntryImportHandler(EntryFetcher entryFetcher,
							  EntryMapper entryMapper) {
		this.entryFetcher = entryFetcher;
		this.entryMapper = entryMapper;
	}

	public RouterFunction<ServerResponse> routes() {
		return route() //
				.POST("/admin/import", this::importEntries) //
				.build();
	}

	Mono<ServerResponse> importEntries(ServerRequest request) {
		int from = request.queryParam("from").map(Integer::parseInt).orElse(0);
		int to = request.queryParam("to").map(Integer::parseInt).orElse(0);
		String owner = request.queryParam("owner").orElse("making");
		String repo = request.queryParam("repo").orElse("blog.ik.am");
		Flux<String> flux = Flux.fromStream(IntStream.rangeClosed(from, to).boxed())
				.flatMap(i -> this.entryFetcher
						.fetch(owner, repo, String.format("content/%05d.md", i))
						.onErrorResume(
								e -> (e instanceof WebClientResponseException.NotFound)
										? Mono.empty()
										: Mono.error(e)),
						2) //
				.flatMap(this.entryMapper::save, 4) //
				.map(e -> e.getEntryId() + " " + e.getFrontMatter().getTitle());
		return ServerResponse.ok() //
				.contentType(MediaType.TEXT_EVENT_STREAM) //
				.body(flux, String.class);
	}
}

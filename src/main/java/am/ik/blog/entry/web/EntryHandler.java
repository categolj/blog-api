package am.ik.blog.entry.web;

import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryMapper;
import am.ik.blog.entry.search.SearchCriteria;
import reactor.core.publisher.Mono;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Component
public class EntryHandler {
	private final EntryMapper entryMapper;

	public EntryHandler(EntryMapper entryMapper) {
		this.entryMapper = entryMapper;
	}

	public RouterFunction<ServerResponse> routes() {
		return route() //
				.GET("/entries/{entryId}", this::getEntry) //
				.GET("/entries", this::getEntries) //
				.build();
	}

	Mono<ServerResponse> getEntry(ServerRequest request) {
		final long entryId = Long.parseLong(request.pathVariable("entryId"));
		final Mono<Entry> entry = this.entryMapper.findOne(entryId, false);
		return entry
				.flatMap(e -> ServerResponse.ok().bodyValue(e))
				.switchIfEmpty(Mono.error(() -> new ResponseStatusException(NOT_FOUND, String.format("The requested entry is not found (entryId = %d)", entryId))));
	}

	Mono<ServerResponse> getEntries(ServerRequest request) {
		final Integer page = request.queryParam("page").map(Integer::parseInt).orElse(0);
		final Integer size = request.queryParam("size").map(Integer::parseInt).orElse(10);
		final Pageable pageable = PageRequest.of(page, size);
		final Mono<Page<Entry>> entryPage = this.entryMapper.findPage(SearchCriteria.defaults().build(), pageable);
		return ServerResponse.ok().body(entryPage, Page.class);
	}
}

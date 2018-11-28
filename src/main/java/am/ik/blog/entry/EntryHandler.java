package am.ik.blog.entry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import am.ik.blog.entry.criteria.CategoryOrders;
import am.ik.blog.entry.criteria.SearchCriteria;
import am.ik.blog.support.PageableImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.queryParam;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Component
public class EntryHandler {
	private final EntryMapper entryMapper;
	private final ParameterizedTypeReference<Page<Entry>> typeReference = new ParameterizedTypeReference<Page<Entry>>() {
	};
	private static final boolean DEFAULT_EXCLUDE_CONTENT = false;
	static final MediaType STREAM_SMILE_MIME_TYPE = MediaType
			.valueOf("application/stream+x-jackson-smile");

	public EntryHandler(EntryMapper entryMapper) {
		this.entryMapper = entryMapper;
	}

	private static final RequestPredicate notAcceptAll = RequestPredicates
			.headers(h -> !h.accept().contains(MediaType.ALL));

	public RouterFunction<ServerResponse> routes() {
		return route() //
				.HEAD("/api/entries/{entryId}", this::headEntry) //
				.GET("/api/entries/{entryId}", this::getEntry) //
				.HEAD("/api/entries", this::headEntries) //
				.GET("/api/entries", queryParam("q", Objects::nonNull),
						this::searchEntries) //
				.GET("/api/entries", notAcceptAll.and(accept(APPLICATION_STREAM_JSON)),
						this::jsonStreamEntries)
				.GET("/api/entries", notAcceptAll.and(accept(TEXT_EVENT_STREAM)),
						this::textEventStreamEntries) //
				.GET("/api/entries", notAcceptAll.and(accept(STREAM_SMILE_MIME_TYPE)),
						this::smileStreamEntries) //
				.GET("/api/entries", accept(APPLICATION_JSON), this::getEntries) //
				.GET("/api/users/{updatedBy}/entries",
						queryParam("updated", Objects::nonNull),
						this::getEntriesByUpdatedBy) //
				.GET("/api/users/{createdBy}/entries", this::getEntriesByCreatedBy) //
				.GET("/api/tags/{tag}/entries", this::getEntriesByTag) //
				.GET("/api/categories/{categories}/entries", this::getEntriesByCategories) //
				.build();
	}

	public Mono<ServerResponse> headEntry(ServerRequest request) {
		EntryId entryId = new EntryId(request.pathVariable("entryId"));
		Mono<EventTime> lastModifiedDate = entryMapper.findLastModifiedDate(entryId);
		return lastModifiedDate.flatMap(e -> {
			String rfc1123 = e.rfc1123();
			return ServerResponse.ok() //
					.header(LAST_MODIFIED, rfc1123) //
					.header(CACHE_CONTROL, "max-age=0") //
					.header(EXPIRES, rfc1123).build();
		}) // does not check body
				.switchIfEmpty(Mono.defer(
						() -> ServerResponse.status(HttpStatus.NOT_FOUND).build()));
	}

	public Mono<ServerResponse> getEntry(ServerRequest request) {
		boolean excludeContent = request.queryParam("excludeContent")
				.map(Boolean::valueOf).orElse(DEFAULT_EXCLUDE_CONTENT);
		EntryId entryId = new EntryId(request.pathVariable("entryId"));
		Mono<Entry> entry = entryMapper.findOne(entryId, excludeContent);
		return entry.flatMap(e -> {
			String rfc1123 = e.getUpdated().getDate().rfc1123();
			return ServerResponse.ok() //
					.header(LAST_MODIFIED, rfc1123) //
					.header(CACHE_CONTROL, "max-age=0") //
					.header(EXPIRES, rfc1123) //
					.syncBody(e);
		}) //
				.switchIfEmpty(
						Mono.defer(() -> ServerResponse.status(HttpStatus.NOT_FOUND)
								.syncBody(Collections.singletonMap("message",
										"entry " + entryId + " is not found."))));
	}

	public Mono<ServerResponse> headEntries(ServerRequest request) {
		Mono<EventTime> latestModifiedDate = this.entryMapper.findLatestModifiedDate();
		return latestModifiedDate.flatMap(e -> {
			String rfc1123 = e.rfc1123();
			return ServerResponse.ok() //
					.header(LAST_MODIFIED, rfc1123) //
					.header(CACHE_CONTROL, "max-age=0") //
					.header(EXPIRES, rfc1123) //
					.build();
		}) // does not check body
				.switchIfEmpty(Mono.defer(
						() -> ServerResponse.status(HttpStatus.NOT_FOUND).build()));
	}

	public Mono<ServerResponse> getEntries(ServerRequest request) {
		SearchCriteria criteria = SearchCriteria.builder().excludeContent(true).build();
		Pageable pageable = new PageableImpl(request);
		Mono<Page<Entry>> entries = entryMapper.findPage(criteria, pageable);
		return ServerResponse.ok().body(entries, typeReference);
	}

	public Mono<ServerResponse> searchEntries(ServerRequest request) {
		String q = request.queryParam("q").orElse("");
		SearchCriteria criteria = SearchCriteria.builder().excludeContent(true).keyword(q)
				.build();
		Pageable pageable = new PageableImpl(request);
		Mono<Page<Entry>> entries = entryMapper.findPage(criteria, pageable);
		return ServerResponse.ok().body(entries, typeReference);
	}

	private Mono<ServerResponse> streamEntries(ServerRequest request,
			MediaType mediaType) {
		SearchCriteria criteria = SearchCriteria.builder().excludeContent(true).build();
		Pageable pageable = new PageableImpl(request);
		Flux<Entry> entries = entryMapper.collectAll(criteria, pageable);
		return ServerResponse.ok() //
				.contentType(mediaType) //
				.body(entries, Entry.class);
	}

	public Mono<ServerResponse> jsonStreamEntries(ServerRequest request) {
		return this.streamEntries(request, APPLICATION_STREAM_JSON);
	}

	public Mono<ServerResponse> textEventStreamEntries(ServerRequest request) {
		return this.streamEntries(request, TEXT_EVENT_STREAM);

	}

	public Mono<ServerResponse> smileStreamEntries(ServerRequest request) {
		return this.streamEntries(request, STREAM_SMILE_MIME_TYPE);

	}

	public Mono<ServerResponse> getEntriesByCreatedBy(ServerRequest request) {
		boolean excludeContent = request.queryParam("excludeContent")
				.map(Boolean::valueOf).orElse(DEFAULT_EXCLUDE_CONTENT);
		Name createdBy = new Name(request.pathVariable("createdBy"));
		SearchCriteria criteria = SearchCriteria.builder().createdBy(createdBy)
				.excludeContent(excludeContent).build();
		Pageable pageable = new PageableImpl(request);
		Mono<Page<Entry>> entries = entryMapper.findPage(criteria, pageable);
		return ServerResponse.ok().body(entries, typeReference);
	}

	public Mono<ServerResponse> getEntriesByUpdatedBy(ServerRequest request) {
		boolean excludeContent = request.queryParam("excludeContent")
				.map(Boolean::valueOf).orElse(DEFAULT_EXCLUDE_CONTENT);
		Name updatedBy = new Name(request.pathVariable("updatedBy"));
		SearchCriteria criteria = SearchCriteria.builder().lastModifiedBy(updatedBy)
				.excludeContent(excludeContent).build();
		Pageable pageable = new PageableImpl(request);
		Mono<Page<Entry>> entries = entryMapper.findPage(criteria, pageable);
		return ServerResponse.ok().body(entries, typeReference);
	}

	public Mono<ServerResponse> getEntriesByTag(ServerRequest request) {
		Tag tag = new Tag(request.pathVariable("tag"));
		SearchCriteria criteria = SearchCriteria.builder().tag(tag).excludeContent(true)
				.build();
		Pageable pageable = new PageableImpl(request);
		Mono<Page<Entry>> entries = entryMapper.findPage(criteria, pageable);
		return ServerResponse.ok().body(entries, typeReference);
	}

	public Mono<ServerResponse> getEntriesByCategories(ServerRequest request) {
		List<Category> categories = Arrays
				.stream(request.pathVariable("categories").split(",")).map(Category::new)
				.collect(Collectors.toList());
		int order = categories.size() - 1;
		Category category = categories.get(order);
		SearchCriteria criteria = SearchCriteria.builder()
				.categoryOrders(new CategoryOrders().add(category, order) /* TODO */)
				.excludeContent(true).build();
		Pageable pageable = new PageableImpl(request);
		Mono<Page<Entry>> entries = entryMapper.findPage(criteria, pageable);
		return ServerResponse.ok().body(entries, typeReference);
	}
}

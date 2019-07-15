package am.ik.blog.entry.v2;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import am.ik.blog.entry.Category;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryHandler;
import am.ik.blog.entry.EntryId;
import am.ik.blog.entry.EntryMapper;
import am.ik.blog.entry.Name;
import am.ik.blog.entry.Tag;
import am.ik.blog.entry.criteria.CategoryOrders;
import am.ik.blog.entry.criteria.SearchCriteria;
import am.ik.blog.support.PageableImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
import static org.springframework.http.HttpHeaders.EXPIRES;
import static org.springframework.http.HttpHeaders.LAST_MODIFIED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.queryParam;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.seeOther;

@Component
public class EntryV2Handler {
	private final EntryMapper entryMapper;
	private final ParameterizedTypeReference<Page<EntryV2>> typeReference = new ParameterizedTypeReference<>() {
	};
	private static final boolean DEFAULT_EXCLUDE_CONTENT = false;

	public EntryV2Handler(EntryMapper entryMapper) {
		this.entryMapper = entryMapper;
	}

	private static final RequestPredicate notAcceptAll = RequestPredicates
			.headers(h -> !h.accept().contains(MediaType.ALL));

	public RouterFunction<ServerResponse> routes(EntryHandler v1) {
		return route() //
				.GET("/entries/next", req -> this.entryMapper.nextId()
						.flatMap(id -> seeOther(URI.create(String.format(
								"https://github.com/making/blog.ik.am/new/master/content/%05d.md",
								id))).build()))
				.HEAD("/entries/{entryId}", v1::headEntry) //
				.GET("/entries/{entryId}", this::getEntry) //
				.HEAD("/entries", v1::headEntries) //
				.GET("/entries", queryParam("q", Objects::nonNull), this::searchEntries) //
				.GET("/entries", notAcceptAll.and(accept(APPLICATION_STREAM_JSON)),
						this::jsonStreamEntries)
				.GET("/entries", notAcceptAll.and(accept(TEXT_EVENT_STREAM)),
						this::textEventStreamEntries) //
				.GET("/entries", accept(APPLICATION_JSON), this::getEntries) //
				.GET("/users/{updatedBy}/entries",
						queryParam("updated", Objects::nonNull),
						this::getEntriesByUpdatedBy) //
				.GET("/users/{createdBy}/entries", this::getEntriesByCreatedBy) //
				.GET("/tags/{tag}/entries", this::getEntriesByTag) //
				.GET("/categories/{categories}/entries", this::getEntriesByCategories) //
				.build();
	}

	public Mono<ServerResponse> getEntry(ServerRequest request) {
		boolean excludeContent = request.queryParam("excludeContent")
				.map(Boolean::valueOf).orElse(DEFAULT_EXCLUDE_CONTENT);
		EntryId entryId = new EntryId(request.pathVariable("entryId"));
		Mono<Entry> entry = entryMapper.findOne(entryId, excludeContent);
		return entry.flatMap(e -> {
			String rfc1123 = e.getUpdated().getDate().rfc1123();
			return ok() //
					.header(LAST_MODIFIED, rfc1123) //
					.header(CACHE_CONTROL, "max-age=0") //
					.header(EXPIRES, rfc1123) //
					.syncBody(EntryV2.from(e));
		}) //
				.switchIfEmpty(Mono.error(() -> new ResponseStatusException(NOT_FOUND,
						"entry " + entryId + " is not found.")));
	}

	public Mono<ServerResponse> getEntries(ServerRequest request) {
		SearchCriteria criteria = SearchCriteria.builder().excludeContent(true).build();
		Pageable pageable = new PageableImpl(request);
		Mono<Page<EntryV2>> entries = entryMapper.findPage(criteria, pageable)
				.map(this::toEntryV2Page);
		return ok().body(entries, typeReference);
	}

	public Mono<ServerResponse> searchEntries(ServerRequest request) {
		String q = request.queryParam("q").orElse("");
		SearchCriteria criteria = SearchCriteria.builder().excludeContent(true).keyword(q)
				.build();
		Pageable pageable = new PageableImpl(request);
		Mono<Page<Entry>> entries = entryMapper.findPage(criteria, pageable);
		return ok().body(entries.map(this::toEntryV2Page), typeReference);
	}

	private Mono<ServerResponse> streamEntries(ServerRequest request,
			MediaType mediaType) {
		SearchCriteria criteria = SearchCriteria.builder().excludeContent(true).build();
		Pageable pageable = new PageableImpl(request);
		Flux<Entry> entries = entryMapper.collectAll(criteria, pageable);
		return ok() //
				.contentType(mediaType) //
				.body(entries.map(EntryV2::from), EntryV2.class);
	}

	public Mono<ServerResponse> jsonStreamEntries(ServerRequest request) {
		return this.streamEntries(request, APPLICATION_STREAM_JSON);
	}

	public Mono<ServerResponse> textEventStreamEntries(ServerRequest request) {
		return this.streamEntries(request, TEXT_EVENT_STREAM);

	}

	public Mono<ServerResponse> getEntriesByCreatedBy(ServerRequest request) {
		boolean excludeContent = request.queryParam("excludeContent")
				.map(Boolean::valueOf).orElse(DEFAULT_EXCLUDE_CONTENT);
		Name createdBy = new Name(request.pathVariable("createdBy"));
		SearchCriteria criteria = SearchCriteria.builder().createdBy(createdBy)
				.excludeContent(excludeContent).build();
		Pageable pageable = new PageableImpl(request);
		Mono<Page<Entry>> entries = entryMapper.findPage(criteria, pageable);
		return ok().body(entries.map(this::toEntryV2Page), typeReference);
	}

	public Mono<ServerResponse> getEntriesByUpdatedBy(ServerRequest request) {
		boolean excludeContent = request.queryParam("excludeContent")
				.map(Boolean::valueOf).orElse(DEFAULT_EXCLUDE_CONTENT);
		Name updatedBy = new Name(request.pathVariable("updatedBy"));
		SearchCriteria criteria = SearchCriteria.builder().lastModifiedBy(updatedBy)
				.excludeContent(excludeContent).build();
		Pageable pageable = new PageableImpl(request);
		Mono<Page<Entry>> entries = entryMapper.findPage(criteria, pageable);
		return ok().body(entries.map(this::toEntryV2Page), typeReference);
	}

	public Mono<ServerResponse> getEntriesByTag(ServerRequest request) {
		Tag tag = new Tag(request.pathVariable("tag"));
		SearchCriteria criteria = SearchCriteria.builder().tag(tag).excludeContent(true)
				.build();
		Pageable pageable = new PageableImpl(request);
		Mono<Page<Entry>> entries = entryMapper.findPage(criteria, pageable);
		return ok().body(entries.map(this::toEntryV2Page), typeReference);
	}

	public Mono<ServerResponse> getEntriesByCategories(ServerRequest request) {
		List<Category> categories = Arrays
				.stream(request.pathVariable("categories").split(",")).map(Category::new)
				.collect(toList());
		int order = categories.size() - 1;
		Category category = categories.get(order);
		SearchCriteria criteria = SearchCriteria.builder()
				.categoryOrders(new CategoryOrders().add(category, order) /* TODO */)
				.excludeContent(true).build();
		Pageable pageable = new PageableImpl(request);
		Mono<Page<Entry>> entries = entryMapper.findPage(criteria, pageable);
		return ok().body(entries.map(this::toEntryV2Page), typeReference);
	}

	public Page<EntryV2> toEntryV2Page(Page<Entry> page) {
		return new PageImpl<>(page.stream().map(EntryV2::from).collect(toList()),
				page.getPageable(), page.getTotalElements());
	}
}

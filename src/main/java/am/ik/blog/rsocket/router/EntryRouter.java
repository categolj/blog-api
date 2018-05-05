package am.ik.blog.rsocket.router;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import am.ik.blog.entry.Category;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryId;
import am.ik.blog.entry.Tag;
import am.ik.blog.entry.criteria.CategoryOrders;
import am.ik.blog.entry.criteria.SearchCriteria;
import am.ik.blog.reactive.ReactiveEntryMapper;
import am.ik.blog.rsocket.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class EntryRouter implements RSocketRouter {
	private final ReactiveEntryMapper entryMapper;

	public EntryRouter(ReactiveEntryMapper entryMapper) {
		this.entryMapper = entryMapper;
	}

	Mono<RSocketResponse> getEntry(RSocketRequest req) {
		EntryId entryId = new EntryId(req.getPathVariables().get("entryId"));
		boolean excludeContent = req.getQueryParams().asBoolean("excludeContent")
				.orElse(false);
		Mono<Entry> entry = this.entryMapper.findOne(entryId, excludeContent);

		return entry.flatMap(RSocketResponse::syncBody) //
				.switchIfEmpty(Mono.error(new ResponseStatusException(
						HttpStatus.NOT_FOUND, "entry " + entryId + " is not found.")));
	}

	Mono<RSocketResponse> getEntries(RSocketRequest req) {
		RSocketQueryParams params = req.getQueryParams();
		boolean excludeContent = params.asBoolean("excludeContent").orElse(true);
		SearchCriteria searchCriteria = params.asString("q")
				.map(keyword -> SearchCriteria.defaults().keyword(keyword))
				.orElseGet(SearchCriteria::defaults) //
				.excludeContent(excludeContent) //
				.build();
		Flux<Entry> entries = this.entryMapper.collectAll(searchCriteria,
				params.pageRequest());
		return RSocketResponse.body(entries, Entry.class);
	}

	Mono<RSocketResponse> getEntriesByTag(RSocketRequest req) {
		RSocketQueryParams params = req.getQueryParams();
		boolean excludeContent = params.asBoolean("excludeContent").orElse(true);
		Tag tag = new Tag(req.getPathVariables().get("tag"));
		SearchCriteria searchCriteria = SearchCriteria.builder().tag(tag)
				.excludeContent(excludeContent).build();
		Flux<Entry> entries = this.entryMapper.collectAll(searchCriteria,
				params.pageRequest());
		return RSocketResponse.body(entries, Entry.class);
	}

	Mono<RSocketResponse> getEntriesByCategories(RSocketRequest req) {
		RSocketQueryParams params = req.getQueryParams();
		List<Category> categories = Arrays
				.stream(req.getPathVariables().get("categories").split(",")) //
				.map(Category::new) //
				.collect(Collectors.toList());
		int order = categories.size() - 1;
		boolean excludeContent = params.asBoolean("excludeContent").orElse(true);
		Category category = categories.get(order);
		SearchCriteria searchCriteria = SearchCriteria.builder()
				.categoryOrders(new CategoryOrders().add(category, order) /* TODO */)
				.excludeContent(excludeContent).build();
		Flux<Entry> entries = this.entryMapper.collectAll(searchCriteria,
				params.pageRequest());
		return RSocketResponse.body(entries, Entry.class);
	}

	@Override
	public List<RSocketRoute> routes() {
		return Arrays.asList(new RSocketRoute("/entries/{entryId}", this::getEntry),
				new RSocketRoute("/entries", this::getEntries),
				new RSocketRoute("/tags/{tag}/entries", this::getEntriesByTag),
				new RSocketRoute("/categories/{categories}/entries",
						this::getEntriesByCategories));
	}
}

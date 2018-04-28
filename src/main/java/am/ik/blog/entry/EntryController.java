package am.ik.blog.entry;

import java.util.List;

import am.ik.blog.entry.criteria.CategoryOrders;
import am.ik.blog.entry.criteria.SearchCriteria;
import am.ik.blog.exception.ResourceNotFoundException;
import am.ik.blog.reactive.ReactiveEntryMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api")
public class EntryController {
	private final ReactiveEntryMapper entryMapper;
	private static final String DEFAULT_EXCLUDE_CONTENT = "false";
	public final static String STREAM_SMILE_MIME_TYPE_VALUE = "application/stream+x-jackson-smile";

	public EntryController(ReactiveEntryMapper entryMapper) {
		this.entryMapper = entryMapper;
	}

	@GetMapping(path = "entries")
	public Mono<Page<Entry>> getEntries(@PageableDefault Pageable pageable) {
		SearchCriteria criteria = SearchCriteria.builder().excludeContent(true).build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "entries", produces = { MediaType.APPLICATION_STREAM_JSON_VALUE,
			MediaType.TEXT_EVENT_STREAM_VALUE, STREAM_SMILE_MIME_TYPE_VALUE })
	public Flux<Entry> streamEntries(@PageableDefault Pageable pageable) {
		SearchCriteria criteria = SearchCriteria.builder().excludeContent(true).build();
		return entryMapper.collectAll(criteria, pageable);
	}

	@GetMapping(path = "entries", params = "q")
	public Mono<Page<Entry>> searchEntries(@PageableDefault Pageable pageable,
			@RequestParam String q) {
		SearchCriteria criteria = SearchCriteria.builder().excludeContent(true).keyword(q)
				.build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "users/{createdBy}/entries")
	public Mono<Page<Entry>> getEntriesByCreatedBy(@PageableDefault Pageable pageable,
			@PathVariable Name createdBy,
			@RequestParam(defaultValue = DEFAULT_EXCLUDE_CONTENT) boolean excludeContent) {
		SearchCriteria criteria = SearchCriteria.builder().createdBy(createdBy)
				.excludeContent(excludeContent).build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "users/{updatedBy}/entries", params = "updated")
	public Mono<Page<Entry>> getEntriesByUpdatedBy(@PageableDefault Pageable pageable,
			@PathVariable Name updatedBy) {
		SearchCriteria criteria = SearchCriteria.builder().lastModifiedBy(updatedBy)
				.excludeContent(true).build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "tags/{tag}/entries")
	public Mono<Page<Entry>> getEntriesByTag(@PageableDefault Pageable pageable,
			@PathVariable Tag tag) {
		SearchCriteria criteria = SearchCriteria.builder().tag(tag).excludeContent(true)
				.build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "categories/{categories}/entries")
	public Mono<Page<Entry>> getEntriesByCategories(@PageableDefault Pageable pageable,
			@PathVariable List<Category> categories) {
		int order = categories.size() - 1;
		Category category = categories.get(order);
		SearchCriteria criteria = SearchCriteria.builder()
				.categoryOrders(new CategoryOrders().add(category, order) /* TODO */)
				.excludeContent(true).build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "entries/{entryId}")
	public Mono<Entry> getEntry(@PathVariable EntryId entryId,
			@RequestParam(defaultValue = DEFAULT_EXCLUDE_CONTENT) boolean excludeContent) {
		return entryMapper.findOne(entryId, excludeContent)
				.switchIfEmpty(Mono.defer(() -> Mono.error(new ResourceNotFoundException(
						"entry " + entryId + " is not found."))));
	}
}

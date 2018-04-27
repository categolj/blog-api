package am.ik.blog.entry;

import java.util.List;
import java.util.Optional;

import am.ik.blog.entry.criteria.CategoryOrders;
import am.ik.blog.entry.criteria.SearchCriteria;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import static am.ik.blog.exception.ResourceNotFoundException.defer;

@RestController
@RequestMapping(path = "api")
@RequiredArgsConstructor
public class EntryController {
	private final EntryMapper entryMapper;
	private static final String DEFAULT_EXCLUDE_CONTENT = "false";

	@GetMapping(path = "entries")
	public Page<Entry> getEntries(@PageableDefault Pageable pageable) {
		SearchCriteria criteria = SearchCriteria.builder().excludeContent(true).build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "entries", produces = { MediaType.APPLICATION_STREAM_JSON_VALUE,
			MediaType.TEXT_EVENT_STREAM_VALUE })
	public Flux<Entry> streamEntries(@PageableDefault Pageable pageable) {
		SearchCriteria criteria = SearchCriteria.builder().excludeContent(true).build();
		return entryMapper.collectAll(criteria, pageable)
				.subscribeOn(Schedulers.elastic());
	}

	@GetMapping(path = "entries", params = "q")
	public Page<Entry> searchEntries(@PageableDefault Pageable pageable,
			@RequestParam String q) {
		SearchCriteria criteria = SearchCriteria.builder().excludeContent(true).keyword(q)
				.build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "users/{createdBy}/entries")
	public Page<Entry> getEntriesByCreatedBy(@PageableDefault Pageable pageable,
			@PathVariable Name createdBy,
			@RequestParam(defaultValue = DEFAULT_EXCLUDE_CONTENT) boolean excludeContent) {
		SearchCriteria criteria = SearchCriteria.builder().createdBy(createdBy)
				.excludeContent(excludeContent).build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "users/{updatedBy}/entries", params = "updated")
	public Page<Entry> getEntriesByUpdatedBy(@PageableDefault Pageable pageable,
			@PathVariable Name updatedBy) {
		SearchCriteria criteria = SearchCriteria.builder().lastModifiedBy(updatedBy)
				.excludeContent(true).build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "tags/{tag}/entries")
	public Page<Entry> getEntriesByTag(@PageableDefault Pageable pageable,
			@PathVariable Tag tag) {
		SearchCriteria criteria = SearchCriteria.builder().tag(tag).excludeContent(true)
				.build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "categories/{categories}/entries")
	public Page<Entry> getEntriesByCategories(@PageableDefault Pageable pageable,
			@PathVariable List<Category> categories) {
		int order = categories.size() - 1;
		Category category = categories.get(order);
		SearchCriteria criteria = SearchCriteria.builder()
				.categoryOrders(new CategoryOrders().add(category, order) /* TODO */)
				.excludeContent(true).build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "entries/{entryId}")
	public Entry getEntry(@PathVariable EntryId entryId,
			@RequestParam(defaultValue = DEFAULT_EXCLUDE_CONTENT) boolean excludeContent,
			UriComponentsBuilder builder) {
		Optional<Entry> entry = Optional
				.ofNullable(entryMapper.findOne(entryId, excludeContent));
		return entry.orElseThrow(defer("entry " + entryId + " is not found."));
	}

	@GetMapping(path = "p/entries/{entryId}")
	public Entry getPremiumEntry(@PathVariable EntryId entryId,
			@RequestParam(defaultValue = DEFAULT_EXCLUDE_CONTENT) boolean excludeContent) {
		Optional<Entry> entry = Optional
				.ofNullable(entryMapper.findOne(entryId, excludeContent));
		return entry.orElseThrow(defer("entry " + entryId + " is not found."));
	}
}

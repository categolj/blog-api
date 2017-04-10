package am.ik.blog.entry;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import am.ik.blog.entry.criteria.CategoryOrders;
import am.ik.blog.entry.criteria.SearchCriteria;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "api")
@RequiredArgsConstructor
public class EntryController {
	private final EntryMapper entryMapper;

	@GetMapping(path = "entries")
	Page<Entry> getEntries(@PageableDefault Pageable pageable,
			@RequestParam(defaultValue = "true") boolean excludeContent) {
		SearchCriteria criteria = SearchCriteria.builder().excludeContent(excludeContent)
				.build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "entries", params = "q")
	Page<Entry> searchEntries(@PageableDefault Pageable pageable, @RequestParam String q,
			@RequestParam(defaultValue = "true") boolean excludeContent) {
		SearchCriteria criteria = SearchCriteria.builder().excludeContent(excludeContent)
				.keyword(q).build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "users/{createdBy}/entries")
	Page<Entry> getEntriesByCreatedBy(@PageableDefault Pageable pageable,
			@PathVariable Name createdBy,
			@RequestParam(defaultValue = "true") boolean excludeContent) {
		SearchCriteria criteria = SearchCriteria.builder().createdBy(createdBy)
				.excludeContent(excludeContent).build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "users/{updatedBy}/entries", params = "updated")
	Page<Entry> getEntriesByUpdatedBy(@PageableDefault Pageable pageable,
			@PathVariable Name updatedBy,
			@RequestParam(defaultValue = "true") boolean excludeContent) {
		SearchCriteria criteria = SearchCriteria.builder().lastModifiedBy(updatedBy)
				.excludeContent(excludeContent).build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "tags/{tag}/entries")
	Page<Entry> getEntriesByTag(@PageableDefault Pageable pageable, @PathVariable Tag tag,
			@RequestParam(defaultValue = "true") boolean excludeContent) {
		SearchCriteria criteria = SearchCriteria.builder().tag(tag)
				.excludeContent(excludeContent).build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "categories/{categories}/entries")
	Page<Entry> getEntriesByCategories(@PageableDefault Pageable pageable,
			@PathVariable List<Category> categories,
			@RequestParam(defaultValue = "true") boolean excludeContent) {
		int order = categories.size() - 1;
		Category category = categories.get(order);
		SearchCriteria criteria = SearchCriteria.builder()
				.categoryOrders(new CategoryOrders().add(category, order) /* TODO */)
				.excludeContent(excludeContent).build();
		return entryMapper.findPage(criteria, pageable);
	}

	@GetMapping(path = "entries/{entryId}")
	Entry getEntry(@PathVariable EntryId entryId,
			@RequestParam(defaultValue = "true") boolean excludeContent) {
		return entryMapper.findOne(entryId, excludeContent);
	}
}

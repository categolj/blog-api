package am.ik.blog.entry.web;

import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryService;
import am.ik.blog.entry.search.SearchCriteria;
import reactor.core.publisher.Mono;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class EntryRestController {
	private final EntryService entryService;

	public EntryRestController(EntryService entryService) {
		this.entryService = entryService;
	}

	@GetMapping(path = "/entries/{entryId}")
	public Mono<Entry> getEntry(@PathVariable("entryId") Long entryId, @RequestParam(defaultValue = "false") boolean excludeContent) {
		final Mono<Entry> entry = this.entryService.findOne(entryId, excludeContent);
		return entry.switchIfEmpty(Mono.error(() -> new ResponseStatusException(NOT_FOUND, String.format("The requested entry is not found (entryId = %d)", entryId))));
	}

	@GetMapping(path = "/entries", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<Entry> getEntries(Pageable pageable, @ModelAttribute EntryRequest request) {
		final SearchCriteria searchCriteria = request.toCriteria();
		return this.entryService.findPage(searchCriteria, pageable);
	}

	@DeleteMapping(path = "/entries/{entryId}")
	public ResponseEntity<Void> deleteEntry(@PathVariable("entryId") Long entryId) {
		this.entryService.delete(entryId);
		return ResponseEntity.noContent().build();
	}
}

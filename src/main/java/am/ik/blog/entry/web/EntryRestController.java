package am.ik.blog.entry.web;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import am.ik.blog.entry.Author;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryBuilder;
import am.ik.blog.entry.EntryService;
import am.ik.blog.entry.search.SearchCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class EntryRestController {
	private final EntryService entryService;

	private final Clock clock;

	public EntryRestController(EntryService entryService, Clock clock) {
		this.entryService = entryService;
		this.clock = clock;
	}

	@GetMapping(path = "/entries/{entryId}")
	public Entry getEntry(@PathVariable("entryId") Long entryId, @RequestParam(defaultValue = "false") boolean excludeContent) {
		final Optional<Entry> entry = this.entryService.findOne(entryId, excludeContent);
		return entry.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, String.format("The requested entry is not found (entryId = %d)", entryId)));
	}

	@GetMapping(path = "/entries", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<Entry> getEntries(Pageable pageable, @ModelAttribute EntrySearchRequest request) {
		final SearchCriteria searchCriteria = request.toCriteria();
		return this.entryService.findPage(searchCriteria, pageable);
	}

	@DeleteMapping(path = "/entries/{entryId}")
	@Operation(security = { @SecurityRequirement(name = "basic") })
	public ResponseEntity<Void> deleteEntry(@PathVariable("entryId") Long entryId) {
		this.entryService.delete(entryId);
		return ResponseEntity.noContent().build();
	}

	@PutMapping(path = "/entries/{entryId}", consumes = MediaType.TEXT_MARKDOWN_VALUE)
	@Operation(security = { @SecurityRequirement(name = "basic") })
	@Transactional
	public ResponseEntity<?> putEntryFromMarkdown(@PathVariable("entryId") Long entryId,
			@RequestBody String markdown,
			@AuthenticationPrincipal UserDetails userDetails,
			UriComponentsBuilder builder) {
		return EntryBuilder.parseBody(entryId, markdown)
				.map(tpl -> {
					final EntryBuilder entryBuilder = tpl.getT1();
					final String username = userDetails.getUsername();
					final OffsetDateTime now = OffsetDateTime.ofInstant(this.clock.instant(), ZoneId.of("UTC"));
					final Author created = this.entryService.findOne(entryId, true)
							.map(Entry::getCreated)
							.map(author -> tpl.getT2().map(author::withDate).orElse(author))
							.orElseGet(() -> new Author(username, tpl.getT2().orElse(now)));
					final Author updated = new Author(username, tpl.getT3().orElse(now));
					final Entry entry = entryBuilder
							.withCreated(created)
							.withUpdated(updated)
							.build();
					this.entryService.save(entry);
					return entry;
				})
				.map(entry -> ResponseEntity.created(builder.path("/entries/{entryId}").build(entryId)).body(entry))
				.orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Can't parse the markdown file"));
	}

	@PutMapping(path = "/entries/{entryId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(security = { @SecurityRequirement(name = "basic") })
	@Transactional
	public ResponseEntity<?> putEntryFromJson(@PathVariable("entryId") Long entryId,
			@RequestBody EntryRequest request,
			@AuthenticationPrincipal UserDetails userDetails,
			UriComponentsBuilder builder) {
		final String username = userDetails.getUsername();
		final OffsetDateTime now = OffsetDateTime.ofInstant(this.clock.instant(), ZoneId.of("UTC"));
		final Author created = this.entryService.findOne(entryId, true)
				.map(Entry::getCreated)
				.map(author -> request.createdOrNullAuthor().setNameIfAbsent(author.getName()).setDateIfAbsent(author.getDate()))
				.orElseGet(() -> request.createdOrNullAuthor().setNameIfAbsent(username).setDateIfAbsent(now));
		final Author updated = request.updatedOrNullAuthor().setNameIfAbsent(username).setDateIfAbsent(now);
		final Entry entry = new EntryBuilder()
				.withEntryId(entryId)
				.withContent(request.content())
				.withFrontMatter(request.frontMatter())
				.withCreated(created)
				.withUpdated(updated)
				.build();
		this.entryService.save(entry);
		return ResponseEntity.created(builder.path("/entries/{entryId}").build(entryId)).body(entry);
	}
}

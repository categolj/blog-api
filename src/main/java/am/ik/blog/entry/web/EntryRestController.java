package am.ik.blog.entry.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import am.ik.blog.category.Category;
import am.ik.blog.entry.Author;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryBuilder;
import am.ik.blog.entry.EntryService;
import am.ik.blog.entry.FrontMatter;
import am.ik.blog.entry.search.SearchCriteria;
import am.ik.blog.tag.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
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

	private final Logger log = LoggerFactory.getLogger(EntryRestController.class);

	public EntryRestController(EntryService entryService, Clock clock) {
		this.entryService = entryService;
		this.clock = clock;
	}

	@GetMapping(path = "/entries/{entryId}")
	public Entry getEntry(@PathVariable("entryId") Long entryId, @RequestParam(defaultValue = "false") boolean excludeContent) {
		final Optional<Entry> entry = this.entryService.findOne(entryId, excludeContent);
		return entry.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, String.format("The requested entry is not found (entryId = %d)", entryId)));
	}

	@GetMapping(path = "/entries/{entryId}.md", produces = MediaType.TEXT_MARKDOWN_VALUE)
	public String getEntryAsMarkdown(@PathVariable("entryId") Long entryId, @RequestParam(defaultValue = "false") boolean excludeContent) {
		return this.getEntry(entryId, excludeContent).toMarkdown();
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
		final AtomicBoolean isUpdate = new AtomicBoolean(false);
		return EntryBuilder.parseBody(entryId, markdown)
				.map(tpl -> {
					final EntryBuilder entryBuilder = tpl.getT1();
					final String username = userDetails.getUsername();
					final OffsetDateTime now = OffsetDateTime.ofInstant(this.clock.instant(), ZoneId.of("UTC"));
					final Author created = this.entryService.findOne(entryId, true)
							.filter(e -> {
								isUpdate.set(true);
								return true;
							})
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
				.map(entry -> buildEntryResponse(entry, builder, isUpdate))
				.orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Can't parse the markdown file"));
	}

	@PutMapping(path = "/entries/{entryId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(security = { @SecurityRequirement(name = "basic") })
	@Transactional
	public ResponseEntity<?> putEntryFromJson(@PathVariable("entryId") Long entryId,
			@RequestBody EntryRequest request,
			@AuthenticationPrincipal UserDetails userDetails,
			UriComponentsBuilder builder) {
		final AtomicBoolean isUpdate = new AtomicBoolean(false);
		final String username = userDetails.getUsername();
		final OffsetDateTime now = OffsetDateTime.ofInstant(this.clock.instant(), ZoneId.of("UTC"));
		final Author created = this.entryService.findOne(entryId, true)
				.filter(e -> {
					isUpdate.set(true);
					return true;
				})
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
		return buildEntryResponse(entry, builder, isUpdate);
	}

	private static ResponseEntity<?> buildEntryResponse(Entry entry, UriComponentsBuilder builder, AtomicBoolean isUpdate) {
		return isUpdate.get() ? ResponseEntity.ok(entry) : ResponseEntity.created(builder.path("/entries/{entryId}").build(entry.getEntryId())).body(entry);
	}

	@GetMapping(path = "/entries/template.md", produces = MediaType.TEXT_MARKDOWN_VALUE)
	public String getTemplateMarkdown() {
		return new EntryBuilder()
				.withContent("""
						Welcome
						      
						**Hello world**, this is my first Categolj blog post.
						      
						I hope you like it!
						""")
				.withFrontMatter(new FrontMatter("Welcome to CategolJ!",
						List.of(new Category("Blog"), new Category("Posts"), new Category("Templates")),
						List.of(new Tag("Hello World"), new Tag("CategolJ"))))
				.withCreated(Author.NULL_AUTHOR)
				.withUpdated(Author.NULL_AUTHOR)
				.build()
				.toMarkdown();
	}

	@GetMapping(path = "/entries.zip", produces = "application/zip")
	@Operation(security = { @SecurityRequirement(name = "basic") })
	public ResponseEntity<?> exportEntries() {
		final Path zip = this.entryService.exportEntriesAsZip();
		try (final InputStream stream = Files.newInputStream(zip)) {
			final byte[] content = StreamUtils.copyToByteArray(stream);
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=entries.zip")
					.body(content);
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		finally {
			try {
				Files.deleteIfExists(zip);
			}
			catch (IOException e) {
				log.warn("Failed to delete file (" + zip + ")", e);
			}
		}
	}
}

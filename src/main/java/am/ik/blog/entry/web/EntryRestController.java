package am.ik.blog.entry.web;

import am.ik.blog.category.Category;
import am.ik.blog.entry.Author;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryBuilder;
import am.ik.blog.entry.EntryService;
import am.ik.blog.entry.FrontMatter;
import am.ik.blog.entry.search.SearchCriteria;
import am.ik.blog.proto.CursorPageEntryInstant;
import am.ik.blog.proto.OffsetPageEntry;
import am.ik.blog.proto.ProtoUtils;
import am.ik.blog.tag.Tag;
import am.ik.pagination.CursorPage;
import am.ik.pagination.CursorPageRequest;
import am.ik.pagination.OffsetPage;
import am.ik.pagination.OffsetPageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@io.swagger.v3.oas.annotations.tags.Tag(name = "entry")
public class EntryRestController {

	private final EntryService entryService;

	private final Clock clock;

	private final Logger log = LoggerFactory.getLogger(EntryRestController.class);

	public EntryRestController(EntryService entryService, Clock clock) {
		this.entryService = entryService;
		this.clock = clock;
	}

	private static <T> ResponseEntity<T> checkNotModified(Entry entry, NativeWebRequest webRequest,
			Function<Entry, T> mapper) {
		OffsetDateTime updated = entry.getUpdated().date();
		if (updated != null) {
			final long lastModifiedTimestamp = updated.toInstant().toEpochMilli();
			if (webRequest.checkNotModified(lastModifiedTimestamp)) {
				return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
					.cacheControl(CacheControl.maxAge(Duration.ofHours(1)))
					.build();
			}
		}
		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofHours(1))).body(mapper.apply(entry));
	}

	@GetMapping(path = "/entries/{entryId:\\d+}")
	@Parameters({ @Parameter(name = HttpHeaders.IF_MODIFIED_SINCE, in = ParameterIn.HEADER,
			schema = @Schema(type = "string")) })
	public ResponseEntity<?> getEntry(@PathVariable("entryId") Long entryId,
			@RequestParam(defaultValue = "false") boolean excludeContent, NativeWebRequest webRequest) {
		return this.getEntryForTenant(entryId, null, excludeContent, webRequest);
	}

	@GetMapping(path = "/tenants/{tenantId}/entries/{entryId:\\d+}")
	public ResponseEntity<?> getEntryForTenant(@PathVariable("entryId") Long entryId,
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId,
			@RequestParam(defaultValue = "false") boolean excludeContent, NativeWebRequest webRequest) {
		final Optional<Entry> entry = this.entryService.findOne(entryId, tenantId, excludeContent);
		return entry.<ResponseEntity<?>>map(e -> checkNotModified(e, webRequest, Function.identity()))
			.orElseGet(() -> buildNotFoundEntry(entryId));
	}

	@GetMapping(path = "/entries/{entryId:\\d+}", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
	@Parameters({ @Parameter(name = HttpHeaders.IF_MODIFIED_SINCE, in = ParameterIn.HEADER,
			schema = @Schema(type = "string")) })
	public ResponseEntity<?> getEntryAsProtobuf(@PathVariable("entryId") Long entryId,
			@RequestParam(defaultValue = "false") boolean excludeContent, NativeWebRequest webRequest) {
		return this.getEntryAsProtobufForTenant(entryId, null, excludeContent, webRequest);
	}

	@GetMapping(path = "/tenants/{tenantId}/entries/{entryId:\\d+}", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
	public ResponseEntity<?> getEntryAsProtobufForTenant(@PathVariable("entryId") Long entryId,
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId,
			@RequestParam(defaultValue = "false") boolean excludeContent, NativeWebRequest webRequest) {
		final Optional<Entry> entry = this.entryService.findOne(entryId, tenantId, excludeContent);
		return entry.<ResponseEntity<?>>map(e -> checkNotModified(e, webRequest, ProtoUtils::toProto))
			.orElseGet(() -> buildNotFoundEntry(entryId));
	}

	@GetMapping(path = "/entries/{entryId:\\d+}.md", produces = MediaType.TEXT_MARKDOWN_VALUE)
	public ResponseEntity<?> getEntryAsMarkdown(@PathVariable("entryId") Long entryId,
			@RequestParam(defaultValue = "false") boolean excludeContent) {
		return this.getEntryAsMarkdownForTenant(entryId, null, excludeContent);
	}

	@GetMapping(path = "/tenants/{tenantId}/entries/{entryId:\\d+}.md", produces = MediaType.TEXT_MARKDOWN_VALUE)
	public ResponseEntity<?> getEntryAsMarkdownForTenant(@PathVariable("entryId") Long entryId,
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId,
			@RequestParam(defaultValue = "false") boolean excludeContent) {
		final Optional<Entry> entry = this.entryService.findOne(entryId, tenantId, excludeContent);
		return entry
			.<ResponseEntity<?>>map(e -> ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s.md".formatted(e.formatId()))
				.body(e.toMarkdown()))
			.orElseGet(() -> buildNotFoundEntry(entryId));
	}

	@GetMapping(path = "/entries", produces = MediaType.APPLICATION_JSON_VALUE)
	@Parameters({
			@Parameter(name = "page",
					schema = @Schema(implementation = Integer.class, defaultValue = "0",
							requiredMode = RequiredMode.NOT_REQUIRED)),
			@Parameter(name = "size", schema = @Schema(implementation = Integer.class, defaultValue = "20",
					requiredMode = RequiredMode.NOT_REQUIRED)) })
	public OffsetPage<Entry> getEntries(@RequestParam(required = false) String query,
			@RequestParam(required = false) String tag, @RequestParam(required = false) List<String> categories,
			@RequestParam(required = false) String createdBy, @RequestParam(required = false) String updatedBy,
			@RequestParam(required = false) List<Long> entryIds,
			@RequestParam(defaultValue = "true") boolean excludeContent,
			@Parameter(hidden = true) OffsetPageRequest pageRequest) {
		return this.getEntriesForTenant(null, query, tag, categories, createdBy, updatedBy, entryIds, excludeContent,
				pageRequest);
	}

	@GetMapping(path = "/entries", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
	@Parameters({
			@Parameter(name = "page",
					schema = @Schema(implementation = Integer.class, defaultValue = "0",
							requiredMode = RequiredMode.NOT_REQUIRED)),
			@Parameter(name = "size", schema = @Schema(implementation = Integer.class, defaultValue = "20",
					requiredMode = RequiredMode.NOT_REQUIRED)) })
	public OffsetPageEntry getEntriesAsProtobuf(@RequestParam(required = false) String query,
			@RequestParam(required = false) String tag, @RequestParam(required = false) List<String> categories,
			@RequestParam(required = false) String createdBy, @RequestParam(required = false) String updatedBy,
			@RequestParam(required = false) List<Long> entryIds,
			@RequestParam(defaultValue = "true") boolean excludeContent,
			@Parameter(hidden = true) OffsetPageRequest pageRequest) {
		return this.getEntriesAsProtobufForTenant(null, query, tag, categories, createdBy, updatedBy, entryIds,
				excludeContent, pageRequest);
	}

	@GetMapping(path = "/tenants/{tenantId}/entries", produces = MediaType.APPLICATION_JSON_VALUE)
	@Parameters({
			@Parameter(name = "page",
					schema = @Schema(implementation = Integer.class, defaultValue = "0",
							requiredMode = RequiredMode.NOT_REQUIRED)),
			@Parameter(name = "size", schema = @Schema(implementation = Integer.class, defaultValue = "20",
					requiredMode = RequiredMode.NOT_REQUIRED)) })
	public OffsetPage<Entry> getEntriesForTenant(
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId,
			@RequestParam(required = false) String query, @RequestParam(required = false) String tag,
			@RequestParam(required = false) List<String> categories, @RequestParam(required = false) String createdBy,
			@RequestParam(required = false) String updatedBy, @RequestParam(required = false) List<Long> entryIds,
			@RequestParam(defaultValue = "true") boolean excludeContent,
			@Parameter(hidden = true) OffsetPageRequest pageRequest) {
		final SearchCriteria searchCriteria = SearchCriteria.builder()
			.keyword(query)
			.tag(tag)
			.stringCategories(categories)
			.createdBy(createdBy)
			.lastModifiedBy(updatedBy)
			.entryIds(entryIds)
			.excludeContent(excludeContent)
			.build();
		return this.entryService.findPage(searchCriteria, tenantId, pageRequest);
	}

	@GetMapping(path = "/tenants/{tenantId}/entries", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
	@Parameters({
			@Parameter(name = "page",
					schema = @Schema(implementation = Integer.class, defaultValue = "0",
							requiredMode = RequiredMode.NOT_REQUIRED)),
			@Parameter(name = "size", schema = @Schema(implementation = Integer.class, defaultValue = "20",
					requiredMode = RequiredMode.NOT_REQUIRED)) })
	public OffsetPageEntry getEntriesAsProtobufForTenant(
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId,
			@RequestParam(required = false) String query, @RequestParam(required = false) String tag,
			@RequestParam(required = false) List<String> categories, @RequestParam(required = false) String createdBy,
			@RequestParam(required = false) String updatedBy, @RequestParam(required = false) List<Long> entryIds,
			@RequestParam(defaultValue = "true") boolean excludeContent,
			@Parameter(hidden = true) OffsetPageRequest pageRequest) {
		return ProtoUtils.toProto(this.getEntriesForTenant(tenantId, query, tag, categories, createdBy, updatedBy,
				entryIds, excludeContent, pageRequest));
	}

	@GetMapping(path = "/entries", params = "cursor", produces = MediaType.APPLICATION_JSON_VALUE)
	@Parameters({
			@Parameter(name = "cursor",
					schema = @Schema(implementation = Instant.class, requiredMode = RequiredMode.NOT_REQUIRED)),
			@Parameter(name = "size", schema = @Schema(implementation = Integer.class, defaultValue = "20",
					requiredMode = RequiredMode.NOT_REQUIRED)) })
	public CursorPage<Entry, Instant> getEntriesByCursor(@RequestParam(required = false) String query,
			@RequestParam(required = false) String tag, @RequestParam(required = false) List<String> categories,
			@RequestParam(required = false) String createdBy, @RequestParam(required = false) String updatedBy,
			@RequestParam(required = false) List<Long> entryIds,
			@RequestParam(defaultValue = "true") boolean excludeContent,
			@Parameter(hidden = true) CursorPageRequest<Instant> pageRequest) {
		return this.getEntriesForTenantByCursor(null, query, tag, categories, createdBy, updatedBy, entryIds,
				excludeContent, pageRequest);
	}

	@GetMapping(path = "/tenants/{tenantId}/entries", params = "cursor", produces = MediaType.APPLICATION_JSON_VALUE)
	@Parameters({
			@Parameter(name = "cursor",
					schema = @Schema(implementation = Instant.class, requiredMode = RequiredMode.NOT_REQUIRED)),
			@Parameter(name = "size", schema = @Schema(implementation = Integer.class, defaultValue = "20",
					requiredMode = RequiredMode.NOT_REQUIRED)) })
	public CursorPage<Entry, Instant> getEntriesForTenantByCursor(
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId,
			@RequestParam(required = false) String query, @RequestParam(required = false) String tag,
			@RequestParam(required = false) List<String> categories, @RequestParam(required = false) String createdBy,
			@RequestParam(required = false) String updatedBy, @RequestParam(required = false) List<Long> entryIds,
			@RequestParam(defaultValue = "true") boolean excludeContent,
			@Parameter(hidden = true) CursorPageRequest<Instant> pageRequest) {
		final SearchCriteria searchCriteria = SearchCriteria.builder()
			.keyword(query)
			.tag(tag)
			.stringCategories(categories)
			.createdBy(createdBy)
			.lastModifiedBy(updatedBy)
			.entryIds(entryIds)
			.excludeContent(excludeContent)
			.build();
		return this.entryService.findPage(searchCriteria, tenantId, pageRequest);
	}

	@GetMapping(path = "/entries", params = "cursor", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
	@Parameters({
			@Parameter(name = "cursor",
					schema = @Schema(implementation = Instant.class, requiredMode = RequiredMode.NOT_REQUIRED)),
			@Parameter(name = "size", schema = @Schema(implementation = Integer.class, defaultValue = "20",
					requiredMode = RequiredMode.NOT_REQUIRED)) })
	public CursorPageEntryInstant getEntriesAsProtobufByCursor(@RequestParam(required = false) String query,
			@RequestParam(required = false) String tag, @RequestParam(required = false) List<String> categories,
			@RequestParam(required = false) String createdBy, @RequestParam(required = false) String updatedBy,
			@RequestParam(required = false) List<Long> entryIds,
			@RequestParam(defaultValue = "true") boolean excludeContent,
			@Parameter(hidden = true) CursorPageRequest<Instant> pageRequest) {
		return this.getEntriesAsProtobufForTenantByCursor(null, query, tag, categories, createdBy, updatedBy, entryIds,
				excludeContent, pageRequest);
	}

	@GetMapping(path = "/tenants/{tenantId}/entries", params = "cursor",
			produces = MediaType.APPLICATION_PROTOBUF_VALUE)
	@Parameters({
			@Parameter(name = "cursor",
					schema = @Schema(implementation = Instant.class, requiredMode = RequiredMode.NOT_REQUIRED)),
			@Parameter(name = "size", schema = @Schema(implementation = Integer.class, defaultValue = "20",
					requiredMode = RequiredMode.NOT_REQUIRED)) })
	public CursorPageEntryInstant getEntriesAsProtobufForTenantByCursor(
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId,
			@RequestParam(required = false) String query, @RequestParam(required = false) String tag,
			@RequestParam(required = false) List<String> categories, @RequestParam(required = false) String createdBy,
			@RequestParam(required = false) String updatedBy, @RequestParam(required = false) List<Long> entryIds,
			@RequestParam(defaultValue = "true") boolean excludeContent,
			@Parameter(hidden = true) CursorPageRequest<Instant> pageRequest) {
		return ProtoUtils.toProto(this.getEntriesForTenantByCursor(tenantId, query, tag, categories, createdBy,
				updatedBy, entryIds, excludeContent, pageRequest));
	}

	@DeleteMapping(path = "/entries/{entryId:\\d+}")
	@Operation(security = { @SecurityRequirement(name = "basic") })
	public ResponseEntity<Void> deleteEntry(@PathVariable("entryId") Long entryId) {
		return this.deleteEntryForTenant(entryId, null);
	}

	@DeleteMapping(path = "/tenants/{tenantId}/entries/{entryId:\\d+}")
	@Operation(security = { @SecurityRequirement(name = "basic") })
	public ResponseEntity<Void> deleteEntryForTenant(@PathVariable("entryId") Long entryId,
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId) {
		this.entryService.delete(entryId, tenantId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping(path = "/entries", consumes = MediaType.TEXT_MARKDOWN_VALUE)
	@Operation(security = { @SecurityRequirement(name = "basic") })
	@Transactional
	public ResponseEntity<?> postEntryFromMarkdown(@RequestBody String markdown,
			@AuthenticationPrincipal UserDetails userDetails, UriComponentsBuilder builder) {
		return this.postEntryFromMarkdownForTenant(null, markdown, userDetails, builder);
	}

	@PostMapping(path = "/tenants/{tenantId}/entries", consumes = MediaType.TEXT_MARKDOWN_VALUE)
	@Operation(security = { @SecurityRequirement(name = "basic") })
	@Transactional
	public ResponseEntity<?> postEntryFromMarkdownForTenant(
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId, @RequestBody String markdown,
			@AuthenticationPrincipal UserDetails userDetails, UriComponentsBuilder builder) {
		final Long entryId = this.entryService.nextId(tenantId);
		return EntryBuilder.parseBody(entryId, markdown.trim()).map(tpl -> {
			final EntryBuilder entryBuilder = tpl.getT1();
			final String username = userDetails.getUsername();
			final OffsetDateTime now = OffsetDateTime.ofInstant(this.clock.instant(), ZoneId.of("UTC"));
			final Author created = new Author(username, tpl.getT2().orElse(now));
			final Author updated = new Author(username, tpl.getT3().orElse(now));
			final Entry entry = entryBuilder.withCreated(created).withUpdated(updated).build();
			this.entryService.save(entry, tenantId);
			return entry;
		})
			.<ResponseEntity<?>>map(entry -> buildEntryResponse(entry, builder, false))
			.orElseGet(EntryRestController::buildBadRequestMarkdown);
	}

	@PostMapping(path = "/entries", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(security = { @SecurityRequirement(name = "basic") })
	@Transactional
	public ResponseEntity<?> postEntryFromJson(@RequestBody EntryRequest request,
			@AuthenticationPrincipal UserDetails userDetails, UriComponentsBuilder builder) {
		return this.postEntryFromJsonForTenant(request, null, userDetails, builder);
	}

	@PostMapping(path = "/tenants/{tenantId}/entries", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(security = { @SecurityRequirement(name = "basic") })
	@Transactional
	public ResponseEntity<?> postEntryFromJsonForTenant(@RequestBody EntryRequest request,
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId,
			@AuthenticationPrincipal UserDetails userDetails, UriComponentsBuilder builder) {
		final Long entryId = this.entryService.nextId(tenantId);
		final String username = userDetails.getUsername();
		final OffsetDateTime now = OffsetDateTime.ofInstant(this.clock.instant(), ZoneId.of("UTC"));
		final Author created = request.createdOrNullAuthor().setNameIfAbsent(username).setDateIfAbsent(now);
		final Author updated = request.updatedOrNullAuthor().setNameIfAbsent(username).setDateIfAbsent(now);
		final Entry entry = new EntryBuilder().withEntryId(entryId)
			.withContent(request.content())
			.withFrontMatter(request.frontMatter())
			.withCreated(created)
			.withUpdated(updated)
			.build();
		this.entryService.save(entry, tenantId);
		return buildEntryResponse(entry, builder, false);
	}

	@PutMapping(path = "/entries/{entryId:\\d+}", consumes = MediaType.TEXT_MARKDOWN_VALUE)
	@Operation(security = { @SecurityRequirement(name = "basic") })
	@Transactional
	public ResponseEntity<?> putEntryFromMarkdown(@PathVariable("entryId") Long entryId, @RequestBody String markdown,
			@AuthenticationPrincipal UserDetails userDetails, UriComponentsBuilder builder) {
		return this.putEntryFromMarkdownForTenant(entryId, null, markdown, userDetails, builder);
	}

	@PutMapping(path = "/tenants/{tenantId}/entries/{entryId:\\d+}", consumes = MediaType.TEXT_MARKDOWN_VALUE)
	@Operation(security = { @SecurityRequirement(name = "basic") })
	@Transactional
	public ResponseEntity<?> putEntryFromMarkdownForTenant(@PathVariable("entryId") Long entryId,
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId, @RequestBody String markdown,
			@AuthenticationPrincipal UserDetails userDetails, UriComponentsBuilder builder) {
		final AtomicBoolean isUpdate = new AtomicBoolean(false);
		return EntryBuilder.parseBody(entryId, markdown.trim()).map(tpl -> {
			final EntryBuilder entryBuilder = tpl.getT1();
			final String username = userDetails.getUsername();
			final OffsetDateTime now = OffsetDateTime.ofInstant(this.clock.instant(), ZoneId.of("UTC"));
			final Author created = this.entryService.findOne(entryId, tenantId, true).filter(e -> {
				isUpdate.set(true);
				return true;
			})
				.map(Entry::getCreated)
				.map(author -> tpl.getT2().map(author::withDate).orElse(author))
				.orElseGet(() -> new Author(username, tpl.getT2().orElse(now)));
			final Author updated = new Author(username, tpl.getT3().orElse(now));
			final Entry entry = entryBuilder.withCreated(created).withUpdated(updated).build();
			this.entryService.save(entry, tenantId);
			return entry;
		})
			.<ResponseEntity<?>>map(entry -> buildEntryResponse(entry, builder, isUpdate.get()))
			.orElseGet(EntryRestController::buildBadRequestMarkdown);
	}

	@PutMapping(path = "/entries/{entryId:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(security = { @SecurityRequirement(name = "basic") })
	@Transactional
	public ResponseEntity<?> putEntryFromJson(@PathVariable("entryId") Long entryId, @RequestBody EntryRequest request,
			@AuthenticationPrincipal UserDetails userDetails, UriComponentsBuilder builder) {
		return this.putEntryFromJsonForTenant(entryId, null, request, userDetails, builder);
	}

	@PutMapping(path = "/tenants/{tenantId}/entries/{entryId:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(security = { @SecurityRequirement(name = "basic") })
	@Transactional
	public ResponseEntity<?> putEntryFromJsonForTenant(@PathVariable("entryId") Long entryId,
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId,
			@RequestBody EntryRequest request, @AuthenticationPrincipal UserDetails userDetails,
			UriComponentsBuilder builder) {
		final AtomicBoolean isUpdate = new AtomicBoolean(false);
		final String username = userDetails.getUsername();
		final OffsetDateTime now = OffsetDateTime.ofInstant(this.clock.instant(), ZoneId.of("UTC"));
		final Author created = this.entryService.findOne(entryId, tenantId, true).filter(e -> {
			isUpdate.set(true);
			return true;
		})
			.map(Entry::getCreated)
			.map(author -> request.createdOrNullAuthor().setNameIfAbsent(author.name()).setDateIfAbsent(author.date()))
			.orElseGet(() -> request.createdOrNullAuthor().setNameIfAbsent(username).setDateIfAbsent(now));
		final Author updated = request.updatedOrNullAuthor().setNameIfAbsent(username).setDateIfAbsent(now);
		final Entry entry = new EntryBuilder().withEntryId(entryId)
			.withContent(request.content())
			.withFrontMatter(request.frontMatter())
			.withCreated(created)
			.withUpdated(updated)
			.build();
		this.entryService.save(entry, tenantId);
		return buildEntryResponse(entry, builder, isUpdate.get());
	}

	@GetMapping(path = "/entries/template.md", produces = MediaType.TEXT_MARKDOWN_VALUE)
	public String getTemplateMarkdown() {
		return new EntryBuilder().withContent("""
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
		return this.exportEntriesForTenant(null);
	}

	@GetMapping(path = "/tenants/{tenantId}/entries.zip", produces = "application/zip")
	@Operation(security = { @SecurityRequirement(name = "basic") })
	public ResponseEntity<?> exportEntriesForTenant(
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId) {
		final Path zip = this.entryService.exportEntriesAsZip(tenantId);
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
				log.info("Deleting {}", zip);
				Files.deleteIfExists(zip);
			}
			catch (IOException e) {
				log.warn("Failed to delete file (" + zip + ")", e);
			}
		}
	}

	private static ResponseEntity<?> buildEntryResponse(Entry entry, UriComponentsBuilder builder, boolean isUpdate) {
		return isUpdate ? ResponseEntity.ok(entry)
				: ResponseEntity.created(builder.path("/entries/{entryId:\\d+}").build(entry.getEntryId())).body(entry);
	}

	private static ResponseEntity<ProblemDetail> buildNotFoundEntry(Long entryId) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ProblemDetail.forStatusAndDetail(NOT_FOUND,
					String.format("The requested entry is not found (entryId = %d)", entryId)));
	}

	private static ResponseEntity<ProblemDetail> buildBadRequestMarkdown() {
		return ResponseEntity.badRequest()
			.body(ProblemDetail.forStatusAndDetail(BAD_REQUEST, "Can't parse the markdown file"));
	}

}

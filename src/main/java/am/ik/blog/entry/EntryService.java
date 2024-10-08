package am.ik.blog.entry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import am.ik.blog.entry.search.SearchCriteria;
import am.ik.pagination.CursorPage;
import am.ik.pagination.CursorPageRequest;
import am.ik.pagination.OffsetPage;
import am.ik.pagination.OffsetPageRequest;
import am.ik.yavi.core.ConstraintViolationsException;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class EntryService {

	private final Logger log = LoggerFactory.getLogger(EntryService.class);

	private final EntryMapper entryMapper;

	public final ObservationRegistry observationRegistry;

	public EntryService(EntryMapper entryMapper, Optional<ObservationRegistry> observationRegistry) {
		this.entryMapper = entryMapper;
		this.observationRegistry = observationRegistry.orElseGet(() -> {
			log.warn("ObservationRegistry is not found. NOOP ObservationRegistry is used instead.");
			return ObservationRegistry.NOOP; /* for test */
		});
	}

	public Long nextId(@Nullable String tenantId) {
		return this.entryMapper.nextId(tenantId);
	}

	@Retry(name = "blog-db")
	public CursorPage<Entry, Instant> findPage(SearchCriteria criteria, @Nullable String tenantId,
			CursorPageRequest<Instant> pageRequest) {
		Supplier<CursorPage<Entry, Instant>> supplier = () -> this.entryMapper.findPage(criteria, tenantId,
				pageRequest);
		if (StringUtils.hasText(criteria.getKeyword())) {
			Supplier<CursorPage<Entry, Instant>> findPage = supplier;
			supplier = () -> Observation.createNotStarted("searchEntries", this.observationRegistry)
				.highCardinalityKeyValue("keyword", criteria.getKeyword())
				.observe(findPage);
		}
		return supplier.get();
	}

	@Retry(name = "blog-db")
	public OffsetPage<Entry> findPage(SearchCriteria criteria, @Nullable String tenantId,
			OffsetPageRequest pageRequest) {
		return this.entryMapper.findPage(criteria, tenantId, pageRequest);
	}

	@Retry(name = "blog-db")
	public Optional<Entry> findOne(Long entryId, @Nullable String tenantId, boolean excludeContent) {
		return this.entryMapper.findOne(entryId, tenantId, excludeContent);
	}

	public Path exportEntriesAsZip(@Nullable String tenantId) {
		try {
			final Path zip = Files.createTempFile("entries", ".zip");
			log.info("Exporting entries to {}", zip);
			try (ZipOutputStream outputStream = new ZipOutputStream(
					Files.newOutputStream(zip, StandardOpenOption.CREATE, StandardOpenOption.WRITE))) {
				final List<Entry> entries = this.entryMapper.findAll(SearchCriteria.builder().includeContent().build(),
						tenantId, new OffsetPageRequest(0, 10_0000));
				for (Entry entry : entries) {
					final ZipEntry zipEntry = new ZipEntry("content/%s.md".formatted(entry.formatId()));
					OffsetDateTime created = entry.getCreated().date();
					if (created != null) {
						zipEntry.setCreationTime(FileTime.from(created.toInstant()));
					}
					OffsetDateTime updated = entry.getUpdated().date();
					if (updated != null) {
						zipEntry.setLastModifiedTime(FileTime.from(updated.toInstant()));
					}
					outputStream.putNextEntry(zipEntry);
					outputStream.write(entry.toMarkdown().getBytes(StandardCharsets.UTF_8));
					outputStream.closeEntry();
				}
				final Map<String, String> additionalContents = Map.of("README.md", """
						# Blog Entries
						""", "HELP.md", """
						# Create a new git repository on the command line

						```
						GIT_URL=...
						git init
						git add -A
						git commit -m "first commit"
						git branch -M main
						git remote add origin ${GIT_URL}
						git push -u origin main
						```
						""", ".gitignore", """
						.DS_Store
						HELP.md
						*.iml
						.idea
						""");
				for (Map.Entry<String, String> kv : additionalContents.entrySet()) {
					final ZipEntry zipEntry = new ZipEntry(kv.getKey());
					outputStream.putNextEntry(zipEntry);
					outputStream.write(kv.getValue().getBytes(StandardCharsets.UTF_8));
					outputStream.closeEntry();
				}
			}
			return zip;
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Retry(name = "blog-db")
	public List<Entry> findAll(SearchCriteria criteria, @Nullable String tenantId, OffsetPageRequest pageRequest) {
		return this.entryMapper.findAll(criteria, tenantId, pageRequest);
	}

	@Transactional
	public Map<String, Integer> save(Entry entry, @Nullable String tenantId) {
		log.info("Saving tenantId={}, entry={}", tenantId, entry);
		Entry.validator.validate(entry).throwIfInvalid(violations -> {
			log.info("Violated constraints {}", violations);
			return new ConstraintViolationsException(violations);
		});
		return this.entryMapper.save(entry, tenantId);
	}

	@Transactional
	public int delete(Long entryId, @Nullable String tenantId) {
		return this.entryMapper.delete(entryId, tenantId);
	}

}

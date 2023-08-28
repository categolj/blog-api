package am.ik.blog.entry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import am.ik.blog.entry.search.SearchCriteria;
import am.ik.pagination.CursorPage;
import am.ik.pagination.CursorPageRequest;
import am.ik.pagination.OffsetPage;
import am.ik.pagination.OffsetPageRequest;
import am.ik.yavi.core.ConstraintViolationsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntryService {
	private final Logger log = LoggerFactory.getLogger(EntryService.class);

	private final EntryMapper entryMapper;

	public EntryService(EntryMapper entryMapper) {
		this.entryMapper = entryMapper;
	}

	public Long nextId(String tenantId) {
		return this.entryMapper.nextId(tenantId);
	}

	public CursorPage<Entry, Instant> findPage(SearchCriteria criteria,
			@P("tenantId") String tenantId, CursorPageRequest<Instant> pageRequest) {
		return this.entryMapper.findPage(criteria, tenantId, pageRequest);
	}

	public OffsetPage<Entry> findPage(SearchCriteria criteria,
			@P("tenantId") String tenantId, OffsetPageRequest pageRequest) {
		return this.entryMapper.findPage(criteria, tenantId, pageRequest);
	}

	public Optional<Entry> findOne(Long entryId, @P("tenantId") String tenantId,
			boolean excludeContent) {
		return this.entryMapper.findOne(entryId, tenantId, excludeContent);
	}

	public Path exportEntriesAsZip(@P("tenantId") String tenantId) {
		try {
			final Path zip = Files.createTempFile("entries", ".zip");
			log.info("Exporting entries to {}", zip);
			try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(
					zip, StandardOpenOption.CREATE, StandardOpenOption.WRITE))) {
				final List<Entry> entries = this.entryMapper.findAll(
						SearchCriteria.builder().includeContent().build(), tenantId,
						new OffsetPageRequest(0, 10_0000));
				for (Entry entry : entries) {
					final ZipEntry zipEntry = new ZipEntry(
							"content/%s.md".formatted(entry.formatId()));
					zipEntry.setCreationTime(
							FileTime.from(entry.getCreated().getDate().toInstant()));
					zipEntry.setLastModifiedTime(
							FileTime.from(entry.getUpdated().getDate().toInstant()));
					outputStream.putNextEntry(zipEntry);
					outputStream
							.write(entry.toMarkdown().getBytes(StandardCharsets.UTF_8));
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

	public List<Entry> findAll(SearchCriteria criteria, @P("tenantId") String tenantId,
			OffsetPageRequest pageRequest) {
		return this.entryMapper.findAll(criteria, tenantId, pageRequest);
	}

	@Transactional
	public Map<String, Integer> save(Entry entry, @P("tenantId") String tenantId) {
		log.info("Saving tenantId={}, entry={}", tenantId, entry);
		Entry.validator.validate(entry).throwIfInvalid(violations -> {
			log.info("Violated constraints {}", violations);
			return new ConstraintViolationsException(violations);
		});
		return this.entryMapper.save(entry, tenantId);
	}

	@Transactional
	public int delete(Long entryId, @P("tenantId") String tenantId) {
		return this.entryMapper.delete(entryId, tenantId);
	}
}

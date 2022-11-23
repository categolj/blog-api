package am.ik.blog.entry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import am.ik.blog.entry.search.SearchCriteria;
import am.ik.blog.github.GitHubUserContentClient;
import am.ik.yavi.core.ConstraintViolationsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntryService {
	private final Logger log = LoggerFactory.getLogger(EntryService.class);

	private final EntryMapper entryMapper;

	private final GitHubUserContentClient gitHubUserContentClient;

	public EntryService(EntryMapper entryMapper, GitHubUserContentClient gitHubUserContentClient) {
		this.entryMapper = entryMapper;
		this.gitHubUserContentClient = gitHubUserContentClient;
	}


	public Page<Entry> findPage(SearchCriteria criteria, Pageable pageable) {
		return this.entryMapper.findPage(criteria, pageable);
	}


	public Optional<Entry> findOne(Long entryId, boolean excludeContent) {
		return this.entryMapper.findOne(entryId, excludeContent);
	}

	public Long nextId() {
		return this.entryMapper.nextId();
	}

	public Path exportEntriesAsZip() {
		try {
			final Path zip = Files.createTempFile("entries", ".zip");
			log.info("Exporting entries to {}", zip);
			try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(zip, StandardOpenOption.CREATE, StandardOpenOption.WRITE))) {
				final List<Entry> entries = this.entryMapper.findAll(SearchCriteria.builder().includeContent().build(), PageRequest.of(0, 10_0000));
				for (Entry entry : entries) {
					final ZipEntry zipEntry = new ZipEntry("content/%s.md".formatted(entry.formatId()));
					zipEntry.setCreationTime(FileTime.from(entry.getCreated().getDate().toInstant()));
					zipEntry.setLastModifiedTime(FileTime.from(entry.getUpdated().getDate().toInstant()));
					outputStream.putNextEntry(zipEntry);
					outputStream.write(entry.toMarkdown().getBytes(StandardCharsets.UTF_8));
					outputStream.closeEntry();
				}
				final Map<String, String> additionalContents = Map.of(
						"README.md", """
								# Blog Entries
								""",
						"HELP.md", """
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
								""",
						".gitignore", """
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

	/* TODO */
	public Entry translate(Long entryId, String lang) {
		throw new UnsupportedOperationException();
	}

	Optional<Entry> fallbackFromGithub(Long entryId, boolean excludeContent, Throwable throwable) {
		return this.findOneFromGithub(entryId);
	}

	Optional<Entry> findOneFromGithub(Long entryId) {
		final Mono<String> markdown = this.gitHubUserContentClient.getContent("making", "blog.ik.am", "master", "content/%05d.md".formatted(entryId));
		return this.parseMarkdown(entryId, markdown).blockOptional();
	}

	Mono<Entry> parseMarkdown(Long entryId, Mono<String> markdown) {
		return markdown.flatMap(body -> Mono.justOrEmpty(EntryBuilder.parseBody(entryId, body)))
				.map(tpl -> tpl.getT1()
						.withCreated(new Author("system", tpl.getT2().orElse(OffsetDateTime.parse("1970-01-01T00:00:00Z"))))
						.withUpdated(new Author("system", tpl.getT3().orElse(OffsetDateTime.parse("1970-01-01T00:00:00Z"))))
						.build());
	}

	public List<Entry> findAll(SearchCriteria criteria, Pageable pageable) {
		return entryMapper.findAll(criteria, pageable);
	}

	@Transactional
	public Map<String, Integer> save(Entry entry) {
		log.info("Saving {}", entry);
		Entry.validator.validate(entry)
				.throwIfInvalid(violations -> {
					log.info("Violated constraints {}", violations);
					return new ConstraintViolationsException(violations);
				});
		return this.entryMapper.save(entry);
	}

	@Transactional
	public int delete(Long entryId) {
		return this.entryMapper.delete(entryId);
	}
}

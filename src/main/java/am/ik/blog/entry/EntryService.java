package am.ik.blog.entry;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import am.ik.blog.entry.search.SearchCriteria;
import am.ik.blog.github.GitHubUserContentClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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


	@CircuitBreaker(name = "entry", fallbackMethod = "fallbackFromGithub")
	public Mono<Entry> findOne(Long entryId, boolean excludeContent) {
		return Mono.justOrEmpty(this.entryMapper.findOne(entryId, excludeContent));
	}

	/* TODO */
	public Entry translate(Long entryId, String lang) {
		throw new UnsupportedOperationException();
	}

	Mono<Entry> fallbackFromGithub(Long entryId, boolean excludeContent, Throwable throwable) {
		return this.findOneFromGithub(entryId);
	}

	Mono<Entry> findOneFromGithub(Long entryId) {
		final Mono<String> markdown = this.gitHubUserContentClient.getContent("making", "blog.ik.am", "master", "content/%05d.md".formatted(entryId));
		return this.parseMarkdown(entryId, markdown);
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
}

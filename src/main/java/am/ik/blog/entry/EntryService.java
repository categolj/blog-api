package am.ik.blog.entry;

import java.time.OffsetDateTime;

import am.ik.blog.entry.search.SearchCriteria;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EntryService {
	private final Logger log = LoggerFactory.getLogger(EntryService.class);

	private final EntryMapper entryMapper;

	private final WebClient webClient;

	private final CircuitBreaker circuitBreaker;

	public EntryService(EntryMapper entryMapper, WebClient.Builder builder, CircuitBreakerRegistry circuitBreakerRegistry) {
		this.entryMapper = entryMapper;
		this.webClient = builder.build();
		this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("entry");
	}


	public Mono<Page<Entry>> findPage(SearchCriteria criteria, Pageable pageable) {
		return this.entryMapper.findPage(criteria, pageable);
	}


	public Mono<Entry> findOne(Long entryId, boolean excludeContent) {
		return this.entryMapper.findOne(entryId, excludeContent)
				.transformDeferred(CircuitBreakerOperator.of(this.circuitBreaker))
				.onErrorResume(e -> {
					log.warn("Failed to read entry (id = " + entryId + ") from database", e);
					return this.fallbackFromGithub(entryId, excludeContent, e);
				});
	}

	/* TODO */
	public Mono<Entry> translate(Long entryId, String lang) {
		return Mono.error(new UnsupportedOperationException());
	}

	Mono<Entry> fallbackFromGithub(Long entryId, boolean excludeContent, Throwable throwable) {
		return this.findOneFromGithub(entryId, "https://raw.githubusercontent.com/making/blog.ik.am/master/content/%05d.md");
	}

	Mono<Entry> findOneFromGithub(Long entryId, String urlTemplate) {
		final Mono<String> markdown = this.webClient.get()
				.uri(urlTemplate.formatted(entryId))
				.retrieve()
				.bodyToMono(String.class);
		return this.parseMarkdown(entryId, markdown);
	}

	Mono<Entry> parseMarkdown(Long entryId, Mono<String> markdown) {
		return markdown.flatMap(body -> Mono.justOrEmpty(EntryBuilder.parseBody(entryId, body)))
				.map(tpl -> tpl.getT1()
						.withCreated(new Author("system", tpl.getT2().orElse(OffsetDateTime.parse("1970-01-01T00:00:00Z"))))
						.withUpdated(new Author("system", tpl.getT3().orElse(OffsetDateTime.parse("1970-01-01T00:00:00Z"))))
						.build());
	}

	public Flux<Entry> findAll(SearchCriteria criteria, Pageable pageable) {
		return entryMapper.findAll(criteria, pageable);
	}
}

package am.ik.blog.entry;

import java.time.OffsetDateTime;

import am.ik.blog.entry.search.SearchCriteria;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EntryService {
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
				.onErrorResume(e -> this.findOneFromGithub(entryId, excludeContent, e));
	}

	Mono<Entry> findOneFromGithub(Long entryId, boolean excludeContent, Throwable throwable) {
		final String fileName = String.format("%05d.md", entryId);
		final Mono<String> markdown = this.webClient.get()
				.uri("https://raw.githubusercontent.com/making/blog.ik.am/master/content/" + fileName)
				.retrieve()
				.bodyToMono(String.class);
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

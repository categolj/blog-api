package am.ik.blog.entry;

import java.time.OffsetDateTime;

import am.ik.blog.entry.search.SearchCriteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EntryMapper {
	Mono<Long> nextId();

	Mono<Long> count(SearchCriteria criteria);

	Mono<Page<Entry>> findPage(SearchCriteria criteria, Pageable pageable);

	Mono<Entry> findOne(@SpanTag("entryId") Long entryId, boolean excludeContent);

	Mono<OffsetDateTime> findLastModifiedDate(@SpanTag("entryId") Long entryId);

	Mono<OffsetDateTime> findLatestModifiedDate();

	Flux<Entry> findAll(SearchCriteria criteria, Pageable pageable);

	Mono<Entry> save(Entry entry);

	Mono<Long> delete(@SpanTag("entryId") Long entryId);
}

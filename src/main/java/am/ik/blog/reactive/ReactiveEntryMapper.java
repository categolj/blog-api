package am.ik.blog.reactive;

import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryId;
import am.ik.blog.entry.EntryMapper;
import am.ik.blog.entry.criteria.SearchCriteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ReactiveEntryMapper {
	private final EntryMapper entryMapper;
	private final Scheduler scheduler;

	public ReactiveEntryMapper(EntryMapper entryMapper, Scheduler scheduler) {
		this.entryMapper = entryMapper;
		this.scheduler = scheduler;
	}

	public Mono<Page<Entry>> findPage(SearchCriteria criteria, Pageable pageable) {
		return Mono.fromCallable(() -> this.entryMapper.findPage(criteria, pageable))
				.subscribeOn(this.scheduler);
	}

	public Mono<Entry> findOne(EntryId entryId, boolean excludeContent) {
		return Mono.fromCallable(() -> this.entryMapper.findOne(entryId, excludeContent))
				.subscribeOn(this.scheduler);
	}

	public Flux<Entry> collectAll(SearchCriteria criteria, Pageable pageable) {
		return this.entryMapper.collectAll(criteria, pageable)
				.subscribeOn(this.scheduler);
	}
}

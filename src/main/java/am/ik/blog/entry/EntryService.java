package am.ik.blog.entry;

import am.ik.blog.entry.search.SearchCriteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EntryService {
	private final EntryMapper entryMapper;

	public EntryService(EntryMapper entryMapper) {
		this.entryMapper = entryMapper;
	}

	public Mono<Page<Entry>> findPage(SearchCriteria criteria, Pageable pageable) {
		return entryMapper.findPage(criteria, pageable);
	}

	public Mono<Entry> findOne(Long entryId, boolean excludeContent) {
		return entryMapper.findOne(entryId, excludeContent);
	}

	public Flux<Entry> findAll(SearchCriteria criteria, Pageable pageable) {
		return entryMapper.findAll(criteria, pageable);
	}
}

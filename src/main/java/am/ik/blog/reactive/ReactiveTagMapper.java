package am.ik.blog.reactive;

import java.util.List;

import am.ik.blog.entry.Tag;
import am.ik.blog.entry.TagMapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.stereotype.Component;

@Component
public class ReactiveTagMapper {
	private final TagMapper tagMapper;

	public ReactiveTagMapper(TagMapper tagMapper) {
		this.tagMapper = tagMapper;
	}

	public Mono<List<Tag>> findOrderByTagNameAsc() {
		return Mono.fromCallable(this.tagMapper::findOrderByTagNameAsc)
				.subscribeOn(Schedulers.elastic());
	}
}

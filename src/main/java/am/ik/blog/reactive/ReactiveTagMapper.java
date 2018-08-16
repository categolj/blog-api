package am.ik.blog.reactive;

import java.util.List;

import am.ik.blog.entry.Tag;
import am.ik.blog.entry.TagMapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import org.springframework.stereotype.Component;

@Component
public class ReactiveTagMapper {
	private final TagMapper tagMapper;
	private final Scheduler scheduler;

	public ReactiveTagMapper(TagMapper tagMapper, Scheduler scheduler) {
		this.tagMapper = tagMapper;
		this.scheduler = scheduler;
	}

	public Mono<List<Tag>> findOrderByTagNameAsc() {
		return Mono.fromCallable(this.tagMapper::findOrderByTagNameAsc)
				.subscribeOn(this.scheduler);
	}
}

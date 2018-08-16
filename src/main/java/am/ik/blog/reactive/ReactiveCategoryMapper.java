package am.ik.blog.reactive;

import java.util.List;

import am.ik.blog.entry.Categories;
import am.ik.blog.entry.CategoryMapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import org.springframework.stereotype.Component;

@Component
public class ReactiveCategoryMapper {
	private final CategoryMapper categoryMapper;
	private final Scheduler scheduler;

	public ReactiveCategoryMapper(CategoryMapper categoryMapper, Scheduler scheduler) {
		this.categoryMapper = categoryMapper;
		this.scheduler = scheduler;
	}

	public Mono<List<Categories>> findAll() {
		return Mono.fromCallable(this.categoryMapper::findAll)
				.subscribeOn(this.scheduler);
	}
}

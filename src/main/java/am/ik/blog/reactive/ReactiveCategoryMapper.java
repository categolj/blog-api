package am.ik.blog.reactive;

import java.util.List;

import am.ik.blog.entry.Categories;
import am.ik.blog.entry.CategoryMapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.stereotype.Component;

@Component
public class ReactiveCategoryMapper {
	private final CategoryMapper categoryMapper;

	public ReactiveCategoryMapper(CategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
	}

	public Mono<List<Categories>> findAll() {
		return Mono.fromCallable(this.categoryMapper::findAll)
				.subscribeOn(Schedulers.elastic());
	}
}

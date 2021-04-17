package am.ik.blog.category;

import java.util.List;

import reactor.core.publisher.Flux;

public interface CategoryMapper {
	Flux<List<Category>> findAll();
}

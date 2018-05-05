package am.ik.blog.rsocket.router;

import java.util.Collections;
import java.util.List;

import am.ik.blog.entry.Categories;
import am.ik.blog.reactive.ReactiveCategoryMapper;
import am.ik.blog.rsocket.RSocketRequest;
import am.ik.blog.rsocket.RSocketResponse;
import am.ik.blog.rsocket.RSocketRoute;
import am.ik.blog.rsocket.RSocketRouter;
import reactor.core.publisher.Mono;

import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

@Component
public class CategoryRouter implements RSocketRouter {
	private final ReactiveCategoryMapper categoryMapper;

	public CategoryRouter(ReactiveCategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
	}

	Mono<RSocketResponse> getCategories(RSocketRequest req) {
		return RSocketResponse.body(this.categoryMapper.findAll(),
				ResolvableType.forClassWithGenerics(List.class, Categories.class));
	}

	@Override
	public List<RSocketRoute> routes() {
		return Collections
				.singletonList(new RSocketRoute("/categories", this::getCategories));
	}
}

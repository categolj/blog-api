package am.ik.blog.entry;

import java.util.List;

import am.ik.blog.reactive.CategoryMapper;
import reactor.core.publisher.Mono;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Component
public class CategoryHandler {
	private final CategoryMapper categoryMapper;
	private final ParameterizedTypeReference<List<List<String>>> typeReference = new ParameterizedTypeReference<List<List<String>>>() {
	};

	public CategoryHandler(CategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
	}

	public RouterFunction<ServerResponse> routes() {
		return route() //
				.GET("/api/categories", this::getCategories) //
				.build();
	}

	public Mono<ServerResponse> getCategories(ServerRequest request) {
		Mono<List<List<String>>> categories = categoryMapper.findAll().map(x -> x.stream()
				.map(c -> c.getValue().stream().map(Category::getValue).collect(toList()))
				.collect(toList()));
		return ServerResponse.ok().body(categories, typeReference);
	}
}

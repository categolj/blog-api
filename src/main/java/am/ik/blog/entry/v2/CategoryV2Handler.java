package am.ik.blog.entry.v2;

import java.util.List;
import java.util.Map;

import am.ik.blog.entry.CategoryMapper;
import reactor.core.publisher.Mono;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Component
public class CategoryV2Handler {
	private final CategoryMapper categoryMapper;
	private final ParameterizedTypeReference<List<Map<String, List<Map<String, String>>>>> typeReference = new ParameterizedTypeReference<>() {
	};

	public CategoryV2Handler(CategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
	}

	public RouterFunction<ServerResponse> routes() {
		return route() //
				.GET("/categories", this::getCategories) //
				.build();
	}

	public Mono<ServerResponse> getCategories(ServerRequest request) {
		Mono<List<Map<String, List<Map<String, String>>>>> categories = categoryMapper
				.findAll().map(x -> x.stream().map(c -> {
					List<Map<String, String>> category = c.getValue().stream()
							.map(a -> Map.of("name", a.getValue())).collect(toList());
					return Map.of("categories", category);
				}).collect(toList()));
		return ServerResponse.ok().body(categories, typeReference);
	}
}

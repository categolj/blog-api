package am.ik.blog.entry.v2;

import java.util.List;
import java.util.Map;

import am.ik.blog.entry.TagMapper;
import reactor.core.publisher.Mono;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Component
public class TagV2Handler {
	private final TagMapper tagMapper;
	private final ParameterizedTypeReference<List<Map<String, String>>> typeReference = new ParameterizedTypeReference<>() {

	};

	public TagV2Handler(TagMapper tagMapper) {
		this.tagMapper = tagMapper;
	}

	public RouterFunction<ServerResponse> routes() {
		return route() //
				.GET("/tags", this::getTags) //
				.build();
	}

	public Mono<ServerResponse> getTags(ServerRequest request) {
		Mono<List<Map<String, String>>> tags = tagMapper.findOrderByTagNameAsc()
				.map(x -> x.stream().map(tag -> Map.of("name", tag.getValue()))
						.collect(toList()));
		return ServerResponse.ok().body(tags, typeReference);
	}
}

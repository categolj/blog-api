package am.ik.blog.entry;

import java.util.List;

import reactor.core.publisher.Mono;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Component
public class TagHandler {
	private final TagMapper tagMapper;
	private final ParameterizedTypeReference<List<String>> typeReference = new ParameterizedTypeReference<List<String>>() {
	};

	public TagHandler(TagMapper tagMapper) {
		this.tagMapper = tagMapper;
	}

	public RouterFunction<ServerResponse> routes() {
		return route() //
				.GET("/api/tags", this::getTags) //
				.build();
	}

	public Mono<ServerResponse> getTags(ServerRequest request) {
		Mono<List<String>> tags = tagMapper.findOrderByTagNameAsc()
				.map(x -> x.stream().map(Tag::getValue).collect(toList()));
		return ServerResponse.ok().body(tags, typeReference);
	}
}

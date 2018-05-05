package am.ik.blog.rsocket.router;

import java.util.Collections;
import java.util.List;

import am.ik.blog.entry.Tag;
import am.ik.blog.reactive.ReactiveTagMapper;
import am.ik.blog.rsocket.RSocketRequest;
import am.ik.blog.rsocket.RSocketResponse;
import am.ik.blog.rsocket.RSocketRoute;
import am.ik.blog.rsocket.RSocketRouter;
import reactor.core.publisher.Mono;

import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

@Component
public class TagRouter implements RSocketRouter {
	private final ReactiveTagMapper tagMapper;

	public TagRouter(ReactiveTagMapper tagMapper) {
		this.tagMapper = tagMapper;
	}

	Mono<RSocketResponse> getTags(RSocketRequest req) {
		return RSocketResponse.body(this.tagMapper.findOrderByTagNameAsc(),
				ResolvableType.forClassWithGenerics(List.class, Tag.class));
	}

	@Override
	public List<RSocketRoute> routes() {
		return Collections.singletonList(new RSocketRoute("/tags", this::getTags));
	}
}

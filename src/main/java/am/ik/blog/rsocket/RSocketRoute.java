package am.ik.blog.rsocket;

import java.util.function.Function;

import reactor.core.publisher.Mono;

import org.springframework.web.util.UriTemplate;

public class RSocketRoute {
	private final UriTemplate uriTemplate;
	private final Function<RSocketRequest, Mono<RSocketResponse>> handler;

	public RSocketRoute(String uriTemplate,
			Function<RSocketRequest, Mono<RSocketResponse>> handler) {
		this.uriTemplate = new UriTemplate(
				uriTemplate.startsWith("/") ? uriTemplate : "/" + uriTemplate);
		this.handler = handler;
	}

	public Mono<RSocketResponse> invoke(String path, RSocketQueryParams query) {
		RSocketRequest request = new RSocketRequest(this.uriTemplate.match(path), query);
		return this.handler.apply(request);
	}

	public boolean matches(String path) {
		return this.uriTemplate.matches(path);
	}

	@Override
	public String toString() {
		return uriTemplate + " -> " + handler;
	}
}

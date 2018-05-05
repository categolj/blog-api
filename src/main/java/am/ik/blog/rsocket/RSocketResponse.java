package am.ik.blog.rsocket;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import org.springframework.core.ResolvableType;

public class RSocketResponse<T extends Publisher> {
	private final T body;
	private final ResolvableType resolvableType;

	public RSocketResponse(T body, ResolvableType resolvableType) {
		this.body = body;
		this.resolvableType = resolvableType;
	}

	public static <T extends Publisher> Mono<RSocketResponse> body(T body,
			ResolvableType resolvableType) {
		return Mono.just(new RSocketResponse<>(body, resolvableType));
	}

	public static <T extends Publisher<S>, S> Mono<RSocketResponse> body(T body,
			Class<S> clazz) {
		return Mono.just(new RSocketResponse<>(body, ResolvableType.forClass(clazz)));
	}

	public static <S> Mono<RSocketResponse> syncBody(S body) {
		return Mono.just(new RSocketResponse<>(Mono.just(body),
				ResolvableType.forClass(body.getClass())));
	}

	public T body() {
		return body;
	}

	public ResolvableType type() {
		return resolvableType;
	}
}

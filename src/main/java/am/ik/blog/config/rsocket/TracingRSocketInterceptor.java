package am.ik.blog.config.rsocket;

import brave.Span.Kind;
import brave.Tracing;
import io.rsocket.RSocket;
import io.rsocket.plugins.RSocketInterceptor;

import org.springframework.cloud.sleuth.instrument.reactor.TracingRSocketProxy;

public class TracingRSocketInterceptor implements RSocketInterceptor {
	private final Tracing tracing;

	private final Kind kind;

	public TracingRSocketInterceptor(Tracing tracing, Kind kind) {
		this.tracing = tracing;
		this.kind = kind;
	}

	@Override
	public RSocket apply(RSocket rSocket) {
		return new TracingRSocketProxy(rSocket, this.tracing, this.kind);
	}
}
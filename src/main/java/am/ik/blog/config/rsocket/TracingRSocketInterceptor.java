package am.ik.blog.config.rsocket;

import brave.Tracing;
import io.rsocket.RSocket;
import io.rsocket.plugins.RSocketInterceptor;

import org.springframework.cloud.sleuth.instrument.reactor.TracingRSocketProxy;

public class TracingRSocketInterceptor implements RSocketInterceptor {
	private final Tracing tracing;

	public TracingRSocketInterceptor(Tracing tracing) {
		this.tracing = tracing;
	}

	@Override
	public RSocket apply(RSocket rSocket) {
		return new TracingRSocketProxy(rSocket, this.tracing);
	}
}
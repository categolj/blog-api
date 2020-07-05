package am.ik.blog.config.rsocket;

import brave.Tracer;
import io.rsocket.RSocket;
import io.rsocket.plugins.RSocketInterceptor;

public class TracingRSocketInterceptor implements RSocketInterceptor {
	private final Tracer tracer;

	public TracingRSocketInterceptor(Tracer tracer) {
		this.tracer = tracer;
	}

	@Override
	public RSocket apply(RSocket rSocket) {
		return new TracingRSocketProxy(rSocket, this.tracer);
	}
}
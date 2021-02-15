package am.ik.blog.config.rsocket;

import io.rsocket.SocketAcceptor;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.plugins.SocketAcceptorInterceptor;

import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.Span.Kind;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.reactor.TracingRSocketProxy;

public class TracingSocketAcceptorInterceptor implements SocketAcceptorInterceptor {
	private final Tracer tracer;

	private final CurrentTraceContext currentTraceContext;

	public TracingSocketAcceptorInterceptor(Tracer tracer, CurrentTraceContext currentTraceContext) {
		this.tracer = tracer;
		this.currentTraceContext = currentTraceContext;
	}

	@Override
	public SocketAcceptor apply(SocketAcceptor socketAcceptor) {
		return (setup, sendingSocket) -> socketAcceptor.accept(setup, sendingSocket)
				.map(rSocket -> {
					final WellKnownMimeType metadataMimeType = WellKnownMimeType.fromString(setup.metadataMimeType());
					return new TracingRSocketProxy(rSocket, metadataMimeType, this.tracer, this.currentTraceContext, Kind.SERVER);
				});
	}
}
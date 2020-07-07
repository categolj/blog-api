package am.ik.blog.config.rsocket;

import brave.Span.Kind;
import brave.Tracing;
import io.rsocket.SocketAcceptor;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.plugins.SocketAcceptorInterceptor;

import org.springframework.cloud.sleuth.instrument.reactor.TracingRSocketProxy;

public class TracingSocketAcceptorInterceptor implements SocketAcceptorInterceptor {
	private final Tracing tracing;

	public TracingSocketAcceptorInterceptor(Tracing tracing) {
		this.tracing = tracing;
	}

	@Override
	public SocketAcceptor apply(SocketAcceptor socketAcceptor) {
		return (setup, sendingSocket) -> socketAcceptor.accept(setup, sendingSocket)
				.map(rSocket -> {
					final WellKnownMimeType metadataMimeType = WellKnownMimeType.fromString(setup.metadataMimeType());
					return new TracingRSocketProxy(rSocket, metadataMimeType, this.tracing, Kind.SERVER);
				});
	}
}
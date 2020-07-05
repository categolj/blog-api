package am.ik.blog.config.rsocket;

import java.util.Objects;
import java.util.Optional;

import brave.Span;
import brave.Span.Kind;
import brave.Tracer;
import brave.propagation.TraceContext;
import io.netty.buffer.ByteBuf;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.metadata.CompositeMetadata;
import io.rsocket.metadata.CompositeMetadata.Entry;
import io.rsocket.metadata.TracingMetadata;
import io.rsocket.metadata.TracingMetadataCodec;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.util.RSocketProxy;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static io.rsocket.metadata.WellKnownMimeType.MESSAGE_RSOCKET_TRACING_ZIPKIN;

class TracingRSocketProxy extends RSocketProxy {
	private final Tracer tracer;

	private final Logger log = LoggerFactory.getLogger(TracingRSocketProxy.class);

	public TracingRSocketProxy(RSocket source, Tracer tracer) {
		super(source);
		this.tracer = tracer;
	}

	@Override
	public Mono<Void> fireAndForget(Payload payload) {
		final Span span = this.createSpan(payload, "fire-and-forget");
		span.start();
		return super.fireAndForget(payload)
				.doOnError(span::error)
				.doFinally(__ -> span.finish());
	}

	@Override
	public Mono<Payload> requestResponse(Payload payload) {
		final Span span = this.createSpan(payload, "request-response");
		span.start();
		return super.requestResponse(payload)
				.doOnError(span::error)
				.doFinally(__ -> span.finish());
	}

	@Override
	public Flux<Payload> requestStream(Payload payload) {
		final Span span = this.createSpan(payload, "request-stream");
		span.start();
		return super.requestStream(payload)
				.doOnError(span::error)
				.doFinally(__ -> span.finish());
	}

	@Override
	public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
		return Flux.from(payloads)
				.switchOnFirst((signal, payloadFlux) -> {
					final Span span = this.createSpan(signal.get(), "request-channel");
					span.start();
					return TracingRSocketProxy.super.requestChannel(payloadFlux)
							.doOnError(span::error)
							.doFinally(__ -> span.finish());
				});
	}

	private Span createSpan(Payload payload, String method) {
		final CompositeMetadata compositeMetadata = new CompositeMetadata(payload.metadata(), false);
		Optional<TracingMetadata> tracingMetadata = Optional.empty();
		if (isProbablyCompositeMetadata(payload.metadata())) {
			try {
				tracingMetadata = compositeMetadata.stream()
						.filter(e -> Objects.equals(e.getMimeType(), MESSAGE_RSOCKET_TRACING_ZIPKIN.getString()))
						.findFirst()
						.map(Entry::getContent)
						.map(TracingMetadataCodec::decode);
			}
			catch (IllegalStateException e) {
				log.warn(e.getMessage(), e);
			}
		}
		return tracingMetadata.map(metadata -> TraceContext.newBuilder()
				.spanId(metadata.spanId())
				.traceId(metadata.traceId())
				.traceIdHigh(metadata.traceIdHigh())
				.sampled(metadata.isSampled())
				.debug(metadata.isDebug())
				.build())
				.map(this.tracer::newChild)
				.orElseGet(this.tracer::nextSpan)
				.kind(Kind.SERVER)
				.name(method)
				.tag("rsocket.method", method);
	}


	/**
	 * Check first 1 byte and determine if the metadata is CompositeMetadata
	 * https://github.com/rsocket/rsocket/blob/master/Extensions/CompositeMetadata.md#metadata-contents
	 */
	public static boolean isProbablyCompositeMetadata(ByteBuf metadata) {
		final byte mimeTypeInfo = metadata.readByte();
		final int isWellKnown = (mimeTypeInfo & 0b10000000) >> 7;
		final int mimeTypeIdentifier = mimeTypeInfo & 0b01111111;
		return isWellKnown == 1 && WellKnownMimeType.fromIdentifier(mimeTypeIdentifier).getIdentifier() >= 0;
	}

}
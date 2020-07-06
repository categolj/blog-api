package org.springframework.cloud.sleuth.instrument.reactor;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import brave.Span;
import brave.Span.Kind;
import brave.Tracer;
import brave.Tracing;
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
import reactor.core.publisher.Operators;

import static io.rsocket.metadata.WellKnownMimeType.MESSAGE_RSOCKET_TRACING_ZIPKIN;

public class TracingRSocketProxy extends RSocketProxy {
	private final Tracing tracing;

	private final Logger log = LoggerFactory.getLogger(TracingRSocketProxy.class);

	public TracingRSocketProxy(RSocket source, Tracing tracing) {
		super(source);
		this.tracing = tracing;
	}

	@Override
	public Mono<Void> fireAndForget(Payload payload) {
		return super.fireAndForget(payload)
				.transform(this.<Void>scopePassingSpanSubscriberOperator("fire-and-forget", payload))
				.doFinally(__ -> this.finishSpan());
	}

	@Override
	public Mono<Payload> requestResponse(Payload payload) {
		return super.requestResponse(payload)
				.transform(this.<Payload>scopePassingSpanSubscriberOperator("request-response", payload))
				.doFinally(__ -> this.finishSpan());
	}

	@Override
	public Flux<Payload> requestStream(Payload payload) {
		return super.requestStream(payload)
				.transform(this.<Payload>scopePassingSpanSubscriberOperator("request-stream", payload))
				.doFinally(__ -> this.finishSpan());
	}

	@Override
	public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
		return Flux.from(payloads)
				.switchOnFirst((signal, payloadFlux) ->
						TracingRSocketProxy.super.requestChannel(payloadFlux)
								.transform(this.<Payload>scopePassingSpanSubscriberOperator("request-channel", signal.get()))
								.doFinally(__ -> this.finishSpan()));
	}

	private <T> Function<? super Publisher<T>, ? extends Publisher<T>> scopePassingSpanSubscriberOperator(String method, Payload payload) {
		return Operators.lift((__, subscriber) -> {
			final TraceContext traceContext = this.traceContext(payload).orElse(null);
			final Tracer tracer = this.tracing.tracer();
			final Span span = (traceContext == null ? tracer.newTrace() : tracer.newChild(traceContext))
					.name(method)
					.kind(Kind.SERVER)
					.tag("rsocket.method", method)
					.start();
			log.debug("Start span {}", span);
			return new ScopePassingSpanSubscriber<>(
					subscriber,
					subscriber.currentContext(),
					this.tracing.currentTraceContext(),
					span.context());
		});
	}

	private void finishSpan() {
		final Span currentSpan = this.tracing.tracer().currentSpan();
		log.debug("Finish span {}", currentSpan);
		currentSpan.finish();
	}

	private Optional<TraceContext> traceContext(Payload payload) {
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
				.build());
	}

	/**
	 * Check first 1 byte and determine if the metadata is probably CompositeMetadata
	 * https://github.com/rsocket/rsocket/blob/master/Extensions/CompositeMetadata.md#metadata-contents
	 */
	public static boolean isProbablyCompositeMetadata(ByteBuf metadata) {
		final byte mimeTypeInfo = metadata.readByte();
		final int isWellKnown = (mimeTypeInfo & 0b10000000) >> 7;
		final int mimeTypeIdentifier = mimeTypeInfo & 0b01111111;
		return isWellKnown == 1 && WellKnownMimeType.fromIdentifier(mimeTypeIdentifier).getIdentifier() >= 0;
	}

}
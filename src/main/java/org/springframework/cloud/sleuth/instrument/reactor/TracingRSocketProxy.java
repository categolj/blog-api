package org.springframework.cloud.sleuth.instrument.reactor;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

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
import reactor.util.context.Context;

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
				.transform(this.<Void>spanSubscriber("fire-and-forget"))
				.subscriberContext(this.propagateContext(payload));
	}

	@Override
	public Mono<Payload> requestResponse(Payload payload) {
		return super.requestResponse(payload)
				.transform(this.<Payload>spanSubscriber("request-response"))
				.subscriberContext(this.propagateContext(payload));
	}

	@Override
	public Flux<Payload> requestStream(Payload payload) {
		return super.requestStream(payload)
				.transform(this.<Payload>spanSubscriber("request-stream"))
				.subscriberContext(this.propagateContext(payload));
	}

	@Override
	public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
		return Flux.from(payloads)
				.switchOnFirst((signal, payloadFlux) ->
						TracingRSocketProxy.super.requestChannel(payloadFlux)
								.transform(this.<Payload>spanSubscriber("request-channel"))
								.subscriberContext(this.propagateContext(signal.get())));
	}

	private <T> Function<? super Publisher<T>, ? extends Publisher<T>> spanSubscriber(String method) {
		return Operators.lift((__, subscriber) -> new SpanSubscriber<>(subscriber, subscriber.currentContext(), this.tracing, method));
	}

	private Function<Context, Context> propagateContext(Payload payload) {
		return context -> traceContext(payload)
				.map(traceContext -> context.put(TraceContext.class, traceContext))
				.orElse(context);
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
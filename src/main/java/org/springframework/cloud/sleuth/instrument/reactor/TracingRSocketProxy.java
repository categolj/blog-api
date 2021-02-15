package org.springframework.cloud.sleuth.instrument.reactor;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

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

import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Span.Kind;
import org.springframework.cloud.sleuth.TraceContext;
import org.springframework.cloud.sleuth.Tracer;

import static io.rsocket.metadata.WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA;
import static io.rsocket.metadata.WellKnownMimeType.MESSAGE_RSOCKET_TRACING_ZIPKIN;

public class TracingRSocketProxy extends RSocketProxy {
	private final WellKnownMimeType metadataMimeType;

	private final Tracer tracer;

	private final CurrentTraceContext currentTraceContext;

	private final Kind kind;

	private final Logger log = LoggerFactory.getLogger(TracingRSocketProxy.class);

	public TracingRSocketProxy(RSocket source, WellKnownMimeType metadataMimeType, Tracer tracer, CurrentTraceContext currentTraceContext, Kind kind) {
		super(source);
		this.metadataMimeType = metadataMimeType;
		this.tracer = tracer;
		this.currentTraceContext = currentTraceContext;
		this.kind = kind;
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
				.doOnNext(this::annotateOnNext)
				.doFinally(__ -> this.finishSpan());
	}

	@Override
	public Flux<Payload> requestStream(Payload payload) {
		return super.requestStream(payload)
				.transform(this.<Payload>scopePassingSpanSubscriberOperator("request-stream", payload))
				.doOnNext(this::annotateOnNext)
				.doFinally(__ -> this.finishSpan());
	}

	@Override
	public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
		return Flux.from(payloads)
				.switchOnFirst((signal, payloadFlux) ->
						TracingRSocketProxy.super.requestChannel(payloadFlux)
								.transform(this.<Payload>scopePassingSpanSubscriberOperator("request-channel", signal.get()))
								.doOnNext(this::annotateOnNext)
								.doFinally(__ -> this.finishSpan()));
	}

	private <T> Function<? super Publisher<T>, ? extends Publisher<T>> scopePassingSpanSubscriberOperator(String method, Payload payload) {
		return Operators.lift((__, subscriber) -> {
			final TraceContext traceContext = this.traceContext(payload).orElse(null);
			final Span span = (traceContext == null ? this.tracer.spanBuilder() : this.tracer.spanBuilder().setParent(traceContext))
					.name(method)
					.kind(this.kind)
					.tag("rsocket.method", method)
					.start();
			log.debug("Start span {}", span);
			return new ScopePassingSpanSubscriber<>(
					subscriber,
					subscriber.currentContext(),
					this.currentTraceContext,
					span.context());
		});
	}

	private void annotateOnNext(Payload payload) {
		final Span currentSpan = this.tracer.currentSpan();
		if (currentSpan != null) {
			currentSpan.event("onNext");
		}
	}

	private void finishSpan() {
		final Span currentSpan = this.tracer.currentSpan();
		if (currentSpan != null) {
			log.debug("Finish span {}", currentSpan);
			currentSpan.end();
		}
	}

	private Optional<TraceContext> traceContext(Payload payload) {
		Optional<TracingMetadata> tracingMetadata = Optional.empty();
		if (metadataMimeType == MESSAGE_RSOCKET_TRACING_ZIPKIN) {
			tracingMetadata = Optional.of(TracingMetadataCodec.decode(payload.metadata()));
		}
		else if (metadataMimeType == MESSAGE_RSOCKET_COMPOSITE_METADATA) {
			tracingMetadata = new CompositeMetadata(payload.metadata(), false)
					.stream()
					.filter(e -> Objects.equals(e.getMimeType(), MESSAGE_RSOCKET_TRACING_ZIPKIN.getString()))
					.findFirst()
					.map(Entry::getContent)
					.map(TracingMetadataCodec::decode);
		}
		return tracingMetadata.map(metadata ->
				new TraceContext() {

					@Override
					public String traceId() {
						return Long.toHexString(metadata.traceId()) + Long.toHexString(metadata.traceIdHigh());
					}

					@Override
					public String parentId() {
						return Long.toHexString(metadata.parentId());
					}

					@Override
					public String spanId() {
						return Long.toHexString(metadata.spanId());
					}

					@Override
					public Boolean sampled() {
						return metadata.isSampled();
					}
				});
	}
}

package am.ik.blog.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class HttpUrlFilteringSpanExporter implements SpanExporter {
	private final SpanExporter delegate;

	private final Predicate<String> uriFilter;


	HttpUrlFilteringSpanExporter(SpanExporter delegate, Predicate<String> uriFilter) {
		this.delegate = delegate;
		this.uriFilter = uriFilter;
	}

	@Override
	public CompletableResultCode export(Collection<SpanData> spans) {
		final Set<String> blockedTraces = new HashSet<>();
		for (SpanData spanData : spans) {
			final String traceId = spanData.getTraceId();
			if (blockedTraces.contains(traceId)) {
				continue;
			}
			final String requestLine = spanData.getAttributes().get(AttributeKey.stringKey("request.line"));
			if (requestLine != null && requestLine.contains(" ") && !uriFilter.test(requestLine.split(" ", 2)[1])) {
				blockedTraces.add(traceId);
				continue;
			}
			final String uri = spanData.getAttributes().get(AttributeKey.stringKey("http.url"));
			if (uri != null && !uriFilter.test(uri)) {
				blockedTraces.add(traceId);
				continue;
			}
		}
		return delegate.export(spans.stream().filter(spanData -> !blockedTraces.contains(spanData.getTraceId())).toList());
	}

	@Override
	public CompletableResultCode flush() {
		return delegate.flush();
	}

	@Override
	public CompletableResultCode shutdown() {
		return delegate.shutdown();
	}
}

package am.ik.blog.exception;

import java.util.Map;
import java.util.Objects;

import brave.Span;
import brave.Tracer;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

@Component
public class BlogErrorAttributes extends DefaultErrorAttributes {
	private final Tracer tracer;

	public BlogErrorAttributes(ServerProperties props, Tracer tracer) {
		super(props.getError().isIncludeException());
		this.tracer = tracer;
	}

	@Override
	public Map<String, Object> getErrorAttributes(ServerRequest request,
			boolean includeStackTrace) {
		Map<String, Object> attributes = super.getErrorAttributes(request,
				includeStackTrace);
		Span span = this.tracer.currentSpan();
		attributes.put("b3", span == null ? null : Objects.toString(span.context(), ""));
		return attributes;
	}
}

package am.ik.blog.config;

import java.util.OptionalInt;

import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolverSupport;
import org.springframework.web.server.ServerWebExchange;

@Component
@Lazy
public class PageableHandlerMethodArgumentResolver
		extends HandlerMethodArgumentResolverSupport {

	public PageableHandlerMethodArgumentResolver(
			ReactiveAdapterRegistry adapterRegistry) {
		super(adapterRegistry);
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return checkParameterType(parameter, Pageable.class::isAssignableFrom);
	}

	@Override
	public Mono<Object> resolveArgument(MethodParameter parameter,
			BindingContext bindingContext, ServerWebExchange exchange) {
		MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
		PageableDefault d = parameter.getParameterAnnotation(PageableDefault.class);
		int page = this.getInt(params, "page").orElseGet(() -> this.defaultPage(d));
		int size = this.getInt(params, "size").orElseGet(() -> this.defaultSize(d));
		return Mono.just(PageRequest.of(page, size));
	}

	private OptionalInt getInt(MultiValueMap<String, String> params, String name) {
		String s = params.getFirst(name);
		if (StringUtils.isEmpty(s)) {
			return OptionalInt.empty();
		}
		try {
			return OptionalInt.of(Integer.parseInt(s));
		}
		catch (NumberFormatException e) {
			return OptionalInt.empty();
		}
	}

	private int defaultPage(PageableDefault d) {
		return d != null ? d.page() : 0;
	}

	private int defaultSize(PageableDefault d) {
		return d != null ? d.size() : 20;
	}
}

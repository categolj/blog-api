package am.ik.blog.pagination.web;

import java.util.Objects;

import am.ik.blog.pagination.OffsetPageRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class OffsetPageRequestHandlerMethodArgumentResolver
		implements HandlerMethodArgumentResolver {
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return OffsetPageRequest.class.equals(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) throws Exception {
		final int page = Integer.parseInt(
				Objects.requireNonNullElse(webRequest.getParameter("page"), "0"));
		final int size = Integer.parseInt(
				Objects.requireNonNullElse(webRequest.getParameter("size"), "20"));
		return new OffsetPageRequest(page, Math.min(size, 200));
	}
}

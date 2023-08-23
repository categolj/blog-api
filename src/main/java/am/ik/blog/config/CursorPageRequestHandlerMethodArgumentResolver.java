package am.ik.blog.config;

import java.time.Instant;

import am.ik.pagination.CursorPageRequest;

import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CursorPageRequestHandlerMethodArgumentResolver
		implements HandlerMethodArgumentResolver {
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return CursorPageRequest.class.equals(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) throws Exception {
		final String cursorString = webRequest.getParameter("cursor");
		final Instant cursor = StringUtils.hasText(cursorString)
				? Instant.parse(cursorString)
				: null;
		final String sizeString = webRequest.getParameter("size");
		final int size = sizeString == null ? 20 : Integer.parseInt(sizeString);
		final String navigationString = webRequest.getParameter("navigation");
		final CursorPageRequest.Navigation navigation = StringUtils
				.hasText(navigationString)
						? CursorPageRequest.Navigation
								.valueOf(navigationString.toUpperCase())
						: CursorPageRequest.Navigation.NEXT;
		return new CursorPageRequest<>(cursor, size, navigation);
	}
}

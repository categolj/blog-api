package am.ik.blog.config;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.filter.CommonsRequestLoggingFilter;

public class RequestLoggingFilter extends CommonsRequestLoggingFilter {
	private final UriFilter uriFilter;

	public RequestLoggingFilter(UriFilter uriFilter) {
		this.uriFilter = uriFilter;
		super.setIncludeQueryString(true);
		super.setIncludeHeaders(true);
		super.setIncludeClientInfo(true);
		super.setHeaderPredicate(s -> !s.equalsIgnoreCase("authorization") && !s.equalsIgnoreCase("cookie"));
	}

	@Override
	protected boolean shouldLog(HttpServletRequest request) {
		final String uri = request.getRequestURI();
		return uriFilter.test(uri);
	}
}

package am.ik.blog.config;

import java.util.function.Predicate;

public class UriFilter implements Predicate<String> {

	@Override
	public boolean test(String uri) {
		final boolean deny = uri != null && (uri.equals("/readyz") || uri.equals("/livez") || uri.startsWith("/actuator") || uri.startsWith("/_static"));
		return !deny;
	}
}

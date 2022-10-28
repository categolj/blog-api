package am.ik.blog.admin.web;

import java.util.Set;

import org.springframework.boot.actuate.endpoint.SanitizableData;
import org.springframework.boot.actuate.endpoint.SanitizingFunction;
import org.springframework.stereotype.Component;

@Component
public class SanitizingFunctionImpl implements SanitizingFunction {

	private final Set<String> keywords = Set.of("pass", "token", "secret", "key");

	@Override
	public SanitizableData apply(SanitizableData data) {
		final String key = data.getKey().toLowerCase();
		final String value = data.getValue().toString().toLowerCase();
		for (String keyword : keywords) {
			if (key.contains(keyword) || value.contains(keyword)) {
				return data.withValue("[REDACTED]");
			}
		}
		return data;
	}
}

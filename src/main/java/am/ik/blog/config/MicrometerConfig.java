package am.ik.blog.config;

import java.util.Arrays;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.metrics.web.reactive.server.WebFluxTags;
import org.springframework.boot.actuate.metrics.web.reactive.server.WebFluxTagsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;

@Configuration
public class MicrometerConfig {
	@Bean
	public MeterRegistryCustomizer meterRegistryCustomizer() {
		return registry -> registry.config() //
				.meterFilter(MeterFilter.deny(id -> {
					String uri = id.getTag("uri");
					return uri != null && uri.startsWith("/actuator");
				}));
	}

	// workaround for SPR-17395
	// https://github.com/spring-projects/spring-boot/issues/14876#issuecomment-430730152
	@Bean
	public WebFluxTagsProvider customTagsProvider() {
		return new WebFluxTagsProvider() {

			private final Tag URI_NOT_FOUND = Tag.of("uri", "NOT_FOUND");
			private final Tag URI_REDIRECTION = Tag.of("uri", "REDIRECTION");
			private final Tag URI_ROOT = Tag.of("uri", "root");

			@Override
			public Iterable<Tag> httpRequestTags(ServerWebExchange exchange,
					Throwable exception) {
				return Arrays.asList(WebFluxTags.method(exchange), uriTag(exchange),
						WebFluxTags.exception(exception), WebFluxTags.status(exchange),
						WebFluxTags.outcome(exchange));
			}

			Tag uriTag(ServerWebExchange exchange) {
				String matchingPattern = exchange
						.getAttribute(RouterFunctions.MATCHING_PATTERN_ATTRIBUTE);
				if (matchingPattern != null) {
					return Tag.of("uri", matchingPattern);
				}
				PathPattern pathPattern = exchange
						.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
				if (pathPattern != null) {
					return Tag.of("uri", pathPattern.getPatternString());
				}
				HttpStatus status = exchange.getResponse().getStatusCode();
				if (status != null) {
					if (status.is3xxRedirection()) {
						return URI_REDIRECTION;
					}
					if (status == HttpStatus.NOT_FOUND) {
						return URI_NOT_FOUND;
					}
				}
				String path = exchange.getRequest().getPath().value();
				if (path.isEmpty()) {
					return URI_ROOT;
				}
				return Tag.of("uri", path);
			}
		};
	}
}

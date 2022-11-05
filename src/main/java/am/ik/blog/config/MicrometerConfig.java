package am.ik.blog.config;

import java.util.function.Predicate;

import io.micrometer.core.instrument.config.MeterFilter;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MicrometerConfig {

	@Bean
	public MeterRegistryCustomizer meterRegistryCustomizer(UriFilter uriFilter) {
		final Predicate<String> negate = uriFilter.negate();
		return registry -> registry.config() //
				.meterFilter(MeterFilter.deny(id -> {
					final String uri = id.getTag("uri");
					return negate.test(uri);
				}));
	}
}

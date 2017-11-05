package am.ik.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;

import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.dropwizard.DropwizardMeterRegistry;

@Configuration
public class MetricsConfig {
	@Bean
	public MetricRegistry metricRegistry(CompositeMeterRegistry registry) {
		return registry.getRegistries().stream()
				.filter(x -> x instanceof DropwizardMeterRegistry)
				.map(DropwizardMeterRegistry.class::cast)
				.map(DropwizardMeterRegistry::getDropwizardRegistry).findAny()
				.orElseGet(MetricRegistry::new);
	}
}

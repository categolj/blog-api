package am.ik.blog.config;

import java.time.Duration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;

@Configuration
public class Resilience4jConfig {
	@Bean
	public CircuitBreakerRegistry circuitBreakerRegistry() {
		final CircuitBreakerConfig config = CircuitBreakerConfig.custom() //
				.failureRateThreshold(30) //
				.slidingWindow(20, 10, COUNT_BASED) //
				.waitDurationInOpenState(Duration.ofSeconds(10)) //
				.permittedNumberOfCallsInHalfOpenState(2) //
				.build();
		return CircuitBreakerRegistry.of(config);
	}

	@Bean
	public InitializingBean init(MeterRegistry meterRegistry, CircuitBreakerRegistry circuitBreakerRegistry) {
		return () -> TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerRegistry).bindTo(meterRegistry);
	}
}

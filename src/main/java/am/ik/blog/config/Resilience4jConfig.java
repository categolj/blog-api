package am.ik.blog.config;

import java.time.Duration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.EventProcessor;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		final Logger log = LoggerFactory.getLogger(Resilience4jConfig.class);
		circuitBreakerRegistry.getEventPublisher().onEvent(event -> log.warn("Event({})", event));
		for (CircuitBreaker circuitBreaker : circuitBreakerRegistry.getAllCircuitBreakers()) {
			final CircuitBreaker.EventPublisher eventPublisher = circuitBreaker.getEventPublisher();
			if (!((EventProcessor) eventPublisher).hasConsumers()) {
				eventPublisher.onError(event -> log.error("[onError] {}", event));
				eventPublisher.onReset(event -> log.info("[onReset] {}", event));
				eventPublisher.onStateTransition(event -> log.info("[onStateTransition] {}", event));
			}
		}
		return () -> TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerRegistry).bindTo(meterRegistry);
	}
}

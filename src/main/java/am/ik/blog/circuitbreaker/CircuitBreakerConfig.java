package am.ik.blog.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerConfig {
	private final CircuitBreakerProps props;

	public CircuitBreakerConfig(CircuitBreakerProps props) {
		this.props = props;
	}

	@Bean
	public CircuitBreakerRegistry circuitBreakerRegistry() {
		final io.github.resilience4j.circuitbreaker.CircuitBreakerConfig circuitBreakerConfig = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
				.failureRateThreshold(this.props.getFailureRateThreshold())
				.slowCallDurationThreshold(this.props.getSlowCallDurationThreshold())
				.slowCallRateThreshold(this.props.getSlowCallRateThreshold())
				.permittedNumberOfCallsInHalfOpenState(this.props.getPermittedNumberOfCallsInHalfOpenState())
				.waitDurationInOpenState(this.props.getWaitDurationInOpenState())
				.minimumNumberOfCalls(this.props.getMinimumNumberOfCalls())
				.recordException(new RecordFailurePredicate())
				.build();
		return CircuitBreakerRegistry.custom()
				.withCircuitBreakerConfig(circuitBreakerConfig)
				.addRegistryEventConsumer(new LoggingCircuitBreakerRegistryEventConsumer())
				.build();
	}
}

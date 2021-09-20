package am.ik.blog.circuitbreaker;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "circuitbreaker")
@Component
public class CircuitBreakerProps {
	private int failureRateThreshold;

	private Duration slowCallDurationThreshold;

	private int slowCallRateThreshold;

	private int permittedNumberOfCallsInHalfOpenState;

	private Duration waitDurationInOpenState;

	private int minimumNumberOfCalls;

	public int getFailureRateThreshold() {
		return failureRateThreshold;
	}

	public void setFailureRateThreshold(int failureRateThreshold) {
		this.failureRateThreshold = failureRateThreshold;
	}

	public Duration getSlowCallDurationThreshold() {
		return slowCallDurationThreshold;
	}

	public void setSlowCallDurationThreshold(Duration slowCallDurationThreshold) {
		this.slowCallDurationThreshold = slowCallDurationThreshold;
	}

	public int getSlowCallRateThreshold() {
		return slowCallRateThreshold;
	}

	public void setSlowCallRateThreshold(int slowCallRateThreshold) {
		this.slowCallRateThreshold = slowCallRateThreshold;
	}

	public int getPermittedNumberOfCallsInHalfOpenState() {
		return permittedNumberOfCallsInHalfOpenState;
	}

	public void setPermittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
		this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
	}

	public Duration getWaitDurationInOpenState() {
		return waitDurationInOpenState;
	}

	public void setWaitDurationInOpenState(Duration waitDurationInOpenState) {
		this.waitDurationInOpenState = waitDurationInOpenState;
	}

	public int getMinimumNumberOfCalls() {
		return minimumNumberOfCalls;
	}

	public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
		this.minimumNumberOfCalls = minimumNumberOfCalls;
	}
}

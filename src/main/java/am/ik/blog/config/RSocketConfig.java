package am.ik.blog.config;

import am.ik.blog.config.rsocket.TracingRSocketInterceptor;
import brave.Span.Kind;
import brave.Tracing;
import io.micrometer.core.instrument.MeterRegistry;
import io.rsocket.micrometer.MicrometerRSocketInterceptor;

import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RSocketConfig {

	@Bean
	public RSocketServerCustomizer serverRSocketFactoryProcessor(Tracing tracing, MeterRegistry meterRegistry) {
		return server -> server.interceptors(interceptorRegistry -> {
			interceptorRegistry.forResponder(new TracingRSocketInterceptor(tracing, Kind.SERVER));
			interceptorRegistry.forResponder(new MicrometerRSocketInterceptor(meterRegistry));
		});
	}
}

package am.ik.blog.config;

import am.ik.blog.config.rsocket.TracingSocketAcceptorInterceptor;
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
			interceptorRegistry.forSocketAcceptor(new TracingSocketAcceptorInterceptor(tracing));
			interceptorRegistry.forResponder(new MicrometerRSocketInterceptor(meterRegistry));
		});
	}
}

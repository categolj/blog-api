package am.ik.blog.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.rsocket.micrometer.MicrometerRSocketInterceptor;

import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RSocketConfig {

	@Bean
	public RSocketServerCustomizer serverRSocketFactoryProcessor(Tracer tracer, CurrentTraceContext currentTraceContext, MeterRegistry meterRegistry) {
		return server -> server.interceptors(interceptorRegistry -> {
			interceptorRegistry.forResponder(new MicrometerRSocketInterceptor(meterRegistry));
		});
	}
}

package am.ik.blog.config;

import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.hystrix.HystrixMetricsBinder;

@Configuration
public class MicrometerConfig {

	public MicrometerConfig(MeterRegistry registry) {
		new HystrixMetricsBinder().bindTo(registry);
	}
}

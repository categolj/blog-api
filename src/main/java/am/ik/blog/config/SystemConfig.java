package am.ik.blog.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;

@Configuration(proxyBeanMethods = false)
public class SystemConfig {

	@Bean
	public Clock systemClock() {
		return Clock.systemDefaultZone();
	}

	@Bean
	public IdGenerator idGenerator() {
		return new AlternativeJdkIdGenerator();
	}

}
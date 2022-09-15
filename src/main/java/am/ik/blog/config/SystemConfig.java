package am.ik.blog.config;

import java.time.Clock;

import com.github.f4b6a3.uuid.UuidCreator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.IdGenerator;

@Configuration
public class SystemConfig {

	@Bean
	public Clock systemClock() {
		return Clock.systemDefaultZone();
	}

	@Bean
	public IdGenerator idGenerator() {
		return UuidCreator::getTimeOrdered;
	}
}
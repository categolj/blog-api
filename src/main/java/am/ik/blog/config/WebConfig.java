package am.ik.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;

@Configuration
public class WebConfig {
	@Bean
	public ReactivePageableHandlerMethodArgumentResolver pageableArgumentResolver() {
		return new ReactivePageableHandlerMethodArgumentResolver();
	}
}

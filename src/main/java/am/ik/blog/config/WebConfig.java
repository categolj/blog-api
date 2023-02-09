package am.ik.blog.config;

import java.util.List;

import am.ik.blog.pagination.web.OffsetPageRequestHandlerMethodArgumentResolver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class WebConfig implements WebMvcConfigurer {
	@Bean
	public UriFilter uriFilter() {
		return new UriFilter();
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOrigins("*")
				.allowedMethods("GET", "POST", "OPTIONS").allowedHeaders("*")
				.maxAge(3600);
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new OffsetPageRequestHandlerMethodArgumentResolver());
	}
}

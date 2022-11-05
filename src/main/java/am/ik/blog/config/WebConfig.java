package am.ik.blog.config;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class WebConfig implements WebMvcConfigurer {
	@Bean
	public CommonsRequestLoggingFilter logFilter(UriFilter uriFilter) {
		final CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter() {
			@Override
			protected boolean shouldLog(HttpServletRequest request) {
				final String uri = request.getRequestURI();
				return uriFilter.test(uri);
			}
		};
		filter.setIncludeQueryString(true);
		filter.setIncludeHeaders(true);
		filter.setIncludeClientInfo(true);
		return filter;
	}

	@Bean
	public UriFilter uriFilter() {
		return new UriFilter();
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("*")
				.allowedMethods("GET", "POST")
				.maxAge(3600);
	}
}

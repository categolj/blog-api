package am.ik.blog.config;

import java.time.Instant;
import java.util.List;

import am.ik.blog.github.GitHubProps;
import am.ik.pagination.web.CursorPageRequestHandlerMethodArgumentResolver;
import am.ik.pagination.web.OffsetPageRequestHandlerMethodArgumentResolver;
import am.ik.webhook.spring.WebhookVerifierRequestBodyAdvice;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CorsProps.class)
public class WebConfig implements WebMvcConfigurer {
	private final GitHubProps githubProps;
	private final CorsProps corsProps;

	public WebConfig(GitHubProps githubProps, CorsProps corsProps) {
		this.githubProps = githubProps;
		this.corsProps = corsProps;
	}

	@Bean
	public WebhookVerifierRequestBodyAdvice webhookVerifierRequestBodyAdvice() {
		return WebhookVerifierRequestBodyAdvice
				.githubSha256(this.githubProps.getWebhookSecret());
	}

	@Bean
	public UriFilter uriFilter() {
		return new UriFilter();
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOrigins(this.corsProps.allowedOrigins())
				.allowedMethods("*").allowedHeaders("*").maxAge(3600);
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new OffsetPageRequestHandlerMethodArgumentResolver());
		resolvers.add(
				new CursorPageRequestHandlerMethodArgumentResolver<>(Instant::parse));
	}
}

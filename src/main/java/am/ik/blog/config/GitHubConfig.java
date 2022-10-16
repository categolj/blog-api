package am.ik.blog.config;

import am.ik.blog.github.GitHubClient;
import am.ik.blog.github.GitHubProps;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class GitHubConfig {
	@Bean
	public HttpServiceProxyFactory httpServiceProxyFactory(GitHubProps props, WebClient.Builder builder) {
		return WebClientAdapter.createHttpServiceProxyFactory(builder
				.baseUrl("https://api.github.com")
				.defaultHeader(HttpHeaders.AUTHORIZATION, "token " + props.getAccessToken()));
	}

	@Bean
	public GitHubClient gitHubClient(HttpServiceProxyFactory proxyFactory) {
		return proxyFactory.createClient(GitHubClient.class);
	}
}

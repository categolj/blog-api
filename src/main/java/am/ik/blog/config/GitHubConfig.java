package am.ik.blog.config;

import am.ik.blog.github.GitHubClient;
import am.ik.blog.github.GitHubProps;
import am.ik.blog.github.GitHubUserContentClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class GitHubConfig {
	@Bean
	public GitHubClient gitHubClient(GitHubProps props, WebClient.Builder builder) {
		final WebClientAdapter adapter = WebClientAdapter.forClient(builder
				.baseUrl("https://api.github.com")
				.defaultHeader(HttpHeaders.AUTHORIZATION, "token " + props.getAccessToken()).build());
		final HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder(adapter).build();
		return factory.createClient(GitHubClient.class);
	}

	@Bean
	public GitHubUserContentClient gitHubUserContentClient(GitHubProps props, WebClient.Builder builder) {
		final WebClientAdapter adapter = WebClientAdapter.forClient(builder
				.baseUrl("https://raw.githubusercontent.com")
				.defaultHeader(HttpHeaders.AUTHORIZATION, "token " + props.getAccessToken()).build());
		final HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder(adapter).build();
		return factory.createClient(GitHubUserContentClient.class);
	}
}

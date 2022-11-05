package am.ik.blog.config;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.time.Duration;

import am.ik.blog.github.GitHubClient;
import am.ik.blog.github.GitHubProps;
import am.ik.blog.github.GitHubUserContentClient;

import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration(proxyBeanMethods = false)
public class GitHubConfig {

	@Bean
	public WebClientCustomizer webClientCustomizer() {
		return builder -> {
			final HttpClient httpClient = HttpClient.newBuilder()
					.followRedirects(Redirect.NORMAL)
					.connectTimeout(Duration.ofSeconds(3))
					.build();
			final ClientHttpConnector connector = new JdkClientHttpConnector(httpClient);
			builder.clientConnector(connector);
		};
	}

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

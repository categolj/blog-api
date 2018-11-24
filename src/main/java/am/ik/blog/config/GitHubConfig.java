package am.ik.blog.config;

import am.ik.blog.github.GitHubProps;
import am.ik.github.AccessToken;
import am.ik.github.GitHubClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GitHubConfig {
	@Bean
	public GitHubClient gitHubClient(GitHubProps props, WebClient.Builder builder) {
		return new GitHubClient(builder, new AccessToken(props.getAccessToken()));
	}
}

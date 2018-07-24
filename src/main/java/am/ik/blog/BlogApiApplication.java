package am.ik.blog;

import am.ik.blog.github.GitHubProps;
import am.ik.github.AccessToken;
import am.ik.github.GitHubClient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class BlogApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogApiApplication.class, args);
	}

	@Bean
	public GitHubClient gitHubClient(GitHubProps props, WebClient.Builder builder) {
		return new GitHubClient(builder, new AccessToken(props.getAccessToken()));
	}
}

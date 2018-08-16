package am.ik.blog;

import am.ik.blog.github.GitHubProps;
import am.ik.github.AccessToken;
import am.ik.github.GitHubClient;
import com.zaxxer.hikari.HikariDataSource;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableAsync
public class BlogApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogApiApplication.class, args);
	}

	@Bean
	public GitHubClient gitHubClient(GitHubProps props, WebClient.Builder builder) {
		return new GitHubClient(builder, new AccessToken(props.getAccessToken()));
	}

	@Bean
	public Scheduler jdbcScheduler(HikariDataSource dataSource) {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(8);
		taskExecutor.setQueueCapacity(0);
		taskExecutor.setMaxPoolSize(dataSource.getMaximumPoolSize());
		taskExecutor.afterPropertiesSet();
		return Schedulers.fromExecutor(taskExecutor);
	}
}

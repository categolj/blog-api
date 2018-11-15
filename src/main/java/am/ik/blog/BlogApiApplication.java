package am.ik.blog;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import am.ik.blog.github.GitHubProps;
import am.ik.github.AccessToken;
import am.ik.github.GitHubClient;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.instrument.async.TraceableExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
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
	public Scheduler jdbcScheduler(BeanFactory beanFactory,
			@Value("${spring.datasource.hikari.maximum-pool-size}") int maxPoolSize) {
		TraceableExecutorService executorService = new TraceableExecutorService(
				beanFactory, new ThreadPoolExecutor(8, maxPoolSize, 0L,
						TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()));
		return Schedulers.fromExecutor(executorService);
	}
}

package am.ik.blog;

import java.time.OffsetDateTime;

import reactor.core.publisher.Hooks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlogApiApplication {

	public static void main(String[] args) {
		Hooks.onErrorDropped(e -> { /* https://github.com/rsocket/rsocket-java/issues/1018 */});
		System.setProperty("info.env.launch", OffsetDateTime.now().toString());
		SpringApplication.run(BlogApiApplication.class, args);
	}
}

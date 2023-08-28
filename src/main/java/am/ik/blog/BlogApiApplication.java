package am.ik.blog;

import java.time.Instant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlogApiApplication {

	public static void main(String[] args) {
		System.setProperty("info.env.launch", Instant.now().toString());
		SpringApplication.run(BlogApiApplication.class, args);
	}

}

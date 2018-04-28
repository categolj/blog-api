package am.ik.blog.entry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static am.ik.blog.entry.Asserts.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON;
import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON_VALUE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({ "classpath:/delete-test-data.sql", "classpath:/insert-test-data.sql" })
public class EntryControllerFluxTest {
	@LocalServerPort
	int port;
	WebTestClient webClient;

	@Before
	public void setup() {
		this.webClient = WebTestClient.bindToServer() //
				.baseUrl("http://localhost:" + port) //
				.build();
	}

	@Test
	public void streamEntries() {
		Flux<Entry> body = this.webClient.get().uri("/api/entries") //
				.header(ACCEPT, APPLICATION_STREAM_JSON_VALUE) //
				.exchange() //
				.expectStatus().isOk() //
				.expectHeader().contentType(new MediaType(APPLICATION_STREAM_JSON, UTF_8)) //
				.returnResult(Entry.class) //
				.getResponseBody();
		StepVerifier.create(body)//
				.assertNext(entry -> assertEntry99999(entry).assertThatContentIsNotSet()) //
				.assertNext(entry -> assertEntry99998(entry).assertThatContentIsNotSet()) //
				.assertNext(entry -> assertEntry99997(entry).assertThatContentIsNotSet()) //
				.verifyComplete();
	}
}

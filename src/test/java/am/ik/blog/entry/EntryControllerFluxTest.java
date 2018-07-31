package am.ik.blog.entry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static am.ik.blog.entry.Asserts.*;
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
	public void streamEntriesJson() {
		Flux<Entry> body = this.webClient.get().uri("/api/entries") //
				.header(ACCEPT, APPLICATION_STREAM_JSON_VALUE) //
				.exchange() //
				.expectStatus().isOk() //
				.expectHeader().contentType(APPLICATION_STREAM_JSON) //
				.returnResult(Entry.class) //
				.getResponseBody();
		StepVerifier.create(body)//
				.assertNext(entry -> assertEntry99999(entry).assertThatContentIsNotSet()) //
				.assertNext(entry -> assertEntry99998(entry).assertThatContentIsNotSet()) //
				.assertNext(entry -> assertEntry99997(entry).assertThatContentIsNotSet()) //
				.verifyComplete();
	}

	@Test
	public void streamEntriesSmile() {
		Flux<Entry> body = this.webClient.get().uri("/api/entries") //
				.header(ACCEPT, EntryHandler.STREAM_SMILE_MIME_TYPE.toString()) //
				.exchange() //
				.expectStatus().isOk() //
				.expectHeader().contentType(EntryHandler.STREAM_SMILE_MIME_TYPE) //
				.returnResult(Entry.class) //
				.getResponseBody();
		StepVerifier.create(body)//
				.assertNext(entry -> assertEntry99999(entry).assertThatContentIsNotSet()) //
				.assertNext(entry -> assertEntry99998(entry).assertThatContentIsNotSet()) //
				.assertNext(entry -> assertEntry99997(entry).assertThatContentIsNotSet()) //
				.verifyComplete();
	}
}

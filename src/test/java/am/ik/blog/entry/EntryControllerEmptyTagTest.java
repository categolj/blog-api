package am.ik.blog.entry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({ "classpath:/delete-test-data.sql", "classpath:/insert-empty-tag-entry.sql" })
@AutoConfigureRestDocs
public class EntryControllerEmptyTagTest {
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
	public void emptyTag() {
		this.webClient.get() //
				.uri("/api/entries/99996") //
				.exchange() //
				.expectStatus().isOk() //
				.expectBody(Entry.class) //
				.consumeWith(r -> {
					Entry entry = r.getResponseBody();
					assertThat(entry).isNotNull();
					assertThat(entry.getEntryId()).isEqualTo(new EntryId(99996L));
					assertThat(entry.getContent()).isEqualTo(
							new Content("This is an entry which has no entry."));
					assertThat(entry.getFrontMatter().getTags()).isEqualTo(new Tags());
				});
	}
}
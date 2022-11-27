package am.ik.blog.entry.web;

import am.ik.blog.entry.Entry;
import am.ik.blog.util.FileLoader;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.StringJoiner;

import static am.ik.blog.entry.web.Asserts.assertEntry99997;
import static am.ik.blog.entry.web.Asserts.assertEntry99998;
import static am.ik.blog.entry.web.Asserts.assertEntry99999;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class EntryRestControllerIntegrationTest {

	WebTestClient webTestClient;

	@Autowired
	JdbcTemplate jdbcTemplate;

	int port;

	public EntryRestControllerIntegrationTest(@Value("${local.server.port}") int port) {
		this.webTestClient = WebTestClient.bindToServer(new JdkClientHttpConnector())
				.baseUrl("http://localhost:" + port).build();
		this.port = port;
	}

	@BeforeEach
	public void reset() {
		jdbcTemplate.update(FileLoader.loadAsString("sql/delete-test-data.sql"));
		jdbcTemplate.update(FileLoader.loadAsString("sql/insert-test-data.sql"));
	}

	@Test
	void responseEntry() {
		this.webTestClient.get().uri("/entries/99999").exchange().expectBody(Entry.class)
				.consumeWith(result -> assertEntry99999(result.getResponseBody())
						.assertContent());
	}

	@Test
	void responsePage() {
		this.webTestClient.get().uri("/entries").exchange().expectBody(EntryPage.class)
				.consumeWith(result -> {
					final EntryPage entryPage = result.getResponseBody();
					assertThat(entryPage.getTotalElements()).isEqualTo(3);
					assertThat(entryPage).isNotNull();
					assertEntry99999(entryPage.getContent().get(0))
							.assertThatContentIsNotSet();
					assertEntry99998(entryPage.getContent().get(1))
							.assertThatContentIsNotSet();
					assertEntry99997(entryPage.getContent().get(2))
							.assertThatContentIsNotSet();
				});
	}

	@Test
	void searchByKeyword() {
		this.webTestClient.get().uri("/entries?query=test data").exchange()
				.expectBody(EntryPage.class).consumeWith(result -> {
					final EntryPage entryPage = result.getResponseBody();
					assertThat(entryPage).isNotNull();
					assertThat(entryPage.getTotalElements()).isEqualTo(2);
					assertEntry99998(entryPage.getContent().get(0))
							.assertThatContentIsNotSet();
					assertEntry99997(entryPage.getContent().get(1))
							.assertThatContentIsNotSet();
				});
	}

	@Test
	void searchByTag() {
		this.webTestClient.get().uri("/entries?tag=test2").exchange()
				.expectBody(EntryPage.class).consumeWith(result -> {
					final EntryPage entryPage = result.getResponseBody();
					assertThat(entryPage).isNotNull();
					assertThat(entryPage.getTotalElements()).isEqualTo(2);
					assertEntry99999(entryPage.getContent().get(0))
							.assertThatContentIsNotSet();
					assertEntry99998(entryPage.getContent().get(1))
							.assertThatContentIsNotSet();
				});
	}

	@Test
	void searchByCategories() {
		this.webTestClient.get().uri("/entries?categories=x,y").exchange()
				.expectBody(EntryPage.class).consumeWith(result -> {
					final EntryPage entryPage = result.getResponseBody();
					assertThat(entryPage).isNotNull();
					assertThat(entryPage.getTotalElements()).isEqualTo(2);
					assertEntry99999(entryPage.getContent().get(0))
							.assertThatContentIsNotSet();
					assertEntry99997(entryPage.getContent().get(1))
							.assertThatContentIsNotSet();
				});
	}

	@Test
	void createFromMarkdown() {
		this.webTestClient.put().uri("/entries/99991")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_MARKDOWN_VALUE)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue("""
						---
						title: Hello World!
						tags: ["Test", "Demo"]
						categories: ["Dev", "Blog", "Test"]
						---

						Hello World!
						Test Test Test!
						""").exchange().expectStatus().isCreated().expectHeader()
				.location("http://localhost:%d/entries/99991".formatted(port))
				.expectBody().jsonPath("$.entryId").isEqualTo(99991).jsonPath("$.content")
				.isEqualTo("Hello World!\nTest Test Test!").jsonPath("$.created.name")
				.isEqualTo("admin").jsonPath("$.created.date").isNotEmpty()
				.jsonPath("$.updated.name").isEqualTo("admin").jsonPath("$.updated.date")
				.isNotEmpty().jsonPath("$.frontMatter.title").isEqualTo("Hello World!")
				.jsonPath("$.frontMatter.tags.length()").isEqualTo(2)
				.jsonPath("$.frontMatter.tags[0].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Demo")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");

		this.webTestClient.get().uri("/entries/99991").exchange().expectStatus().isOk()
				.expectBody().jsonPath("$.entryId").isEqualTo(99991).jsonPath("$.content")
				.isEqualTo("Hello World!\nTest Test Test!").jsonPath("$.created.name")
				.isEqualTo("admin").jsonPath("$.created.date").isNotEmpty()
				.jsonPath("$.updated.name").isEqualTo("admin").jsonPath("$.updated.date")
				.isNotEmpty().jsonPath("$.frontMatter.title").isEqualTo("Hello World!")
				.jsonPath("$.frontMatter.tags.length()").isEqualTo(2)
				.jsonPath("$.frontMatter.tags[0].name").isEqualTo("Demo") // alphabetic
																			// order
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");
	}

	@Test
	void updateFromMarkdown() {
		this.webTestClient.put().uri("/entries/99999")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_MARKDOWN_VALUE)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue("""
						---
						title: Hello World!
						tags: ["Test", "Demo"]
						categories: ["Dev", "Blog", "Test"]
						---

						Hello World!
						Test Test Test!
						""").exchange().expectStatus().isOk().expectBody()
				.jsonPath("$.entryId").isEqualTo(99999).jsonPath("$.content")
				.isEqualTo("Hello World!\nTest Test Test!").jsonPath("$.created.name")
				.isEqualTo("making") // creator remains unchanged
				.jsonPath("$.created.date").isEqualTo("2017-04-01T01:00:00Z")
				.jsonPath("$.updated.name").isEqualTo("admin").jsonPath("$.updated.date")
				.isNotEmpty().jsonPath("$.frontMatter.title").isEqualTo("Hello World!")
				.jsonPath("$.frontMatter.tags.length()").isEqualTo(2)
				.jsonPath("$.frontMatter.tags[0].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Demo")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");

		this.webTestClient.get().uri("/entries/99999").exchange().expectStatus().isOk()
				.expectBody().jsonPath("$.entryId").isEqualTo(99999).jsonPath("$.content")
				.isEqualTo("Hello World!\nTest Test Test!").jsonPath("$.created.name")
				.isEqualTo("making").jsonPath("$.created.date")
				.isEqualTo("2017-04-01T01:00:00Z").jsonPath("$.updated.name")
				.isEqualTo("admin").jsonPath("$.updated.date").isNotEmpty()
				.jsonPath("$.frontMatter.title").isEqualTo("Hello World!")
				.jsonPath("$.frontMatter.tags.length()").isEqualTo(2)
				.jsonPath("$.frontMatter.tags[0].name").isEqualTo("Demo") // alphabetic
																			// order
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");
	}

	@Test
	void delete() {
		this.webTestClient.delete().uri("/entries/99999")
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.exchange().expectStatus().isNoContent();

		this.webTestClient.get().uri("/entries/99999").exchange().expectStatus()
				.isNotFound();
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	static class EntryPage {

		private List<Entry> content;

		private int number;

		private int size;

		private long totalElements;

		public List<Entry> getContent() {
			return content;
		}

		public void setContent(List<Entry> content) {
			this.content = content;
		}

		public int getNumber() {
			return number;
		}

		public void setNumber(int number) {
			this.number = number;
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public long getTotalElements() {
			return totalElements;
		}

		public void setTotalElements(long totalElements) {
			this.totalElements = totalElements;
		}

		@Override
		public String toString() {
			return new StringJoiner(", ", EntryPage.class.getSimpleName() + "[", "]")
					.add("number=" + number).add("size=" + size)
					.add("totalElements=" + totalElements).toString();
		}
	}
}
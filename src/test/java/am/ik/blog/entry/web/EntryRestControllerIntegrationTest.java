package am.ik.blog.entry.web;

import am.ik.blog.entry.Entry;
import am.ik.blog.util.FileLoader;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

	@Autowired
	WebTestClient webTestClient;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@BeforeEach
	public void reset() {
		jdbcTemplate.update(FileLoader.loadAsString("sql/delete-test-data.sql"));
		jdbcTemplate.update(FileLoader.loadAsString("sql/insert-test-data.sql"));
	}

	@Test
	void responseEntry() {
		this.webTestClient.get().uri("/entries/99999")
				.exchange()
				.expectBody(Entry.class)
				.consumeWith(result -> assertEntry99999(result.getResponseBody()).assertContent());
	}

	@Test
	void responsePage() {
		this.webTestClient.get().uri("/entries")
				.exchange()
				.expectBody(EntryPage.class)
				.consumeWith(result -> {
					final EntryPage entryPage = result.getResponseBody();
					assertThat(entryPage.getTotalElements()).isEqualTo(3);
					assertThat(entryPage).isNotNull();
					assertEntry99999(entryPage.getContent().get(0)).assertThatContentIsNotSet();
					assertEntry99998(entryPage.getContent().get(1)).assertThatContentIsNotSet();
					assertEntry99997(entryPage.getContent().get(2)).assertThatContentIsNotSet();
				});
	}

	@Test
	void searchByKeyword() {
		this.webTestClient.get().uri("/entries?query=This")
				.exchange()
				.expectBody(EntryPage.class)
				.consumeWith(result -> {
					final EntryPage entryPage = result.getResponseBody();
					assertThat(entryPage).isNotNull();
					assertThat(entryPage.getTotalElements()).isEqualTo(2);
					assertEntry99998(entryPage.getContent().get(0)).assertThatContentIsNotSet();
					assertEntry99997(entryPage.getContent().get(1)).assertThatContentIsNotSet();
				});
	}

	@Test
	void searchByTag() {
		this.webTestClient.get().uri("/entries?tag=test2")
				.exchange()
				.expectBody(EntryPage.class)
				.consumeWith(result -> {
					final EntryPage entryPage = result.getResponseBody();
					assertThat(entryPage).isNotNull();
					assertThat(entryPage.getTotalElements()).isEqualTo(2);
					assertEntry99999(entryPage.getContent().get(0)).assertThatContentIsNotSet();
					assertEntry99998(entryPage.getContent().get(1)).assertThatContentIsNotSet();
				});
	}

	@Test
	void searchByCategories() {
		this.webTestClient.get().uri("/entries?categories=x,y")
				.exchange()
				.expectBody(EntryPage.class)
				.consumeWith(result -> {
					final EntryPage entryPage = result.getResponseBody();
					assertThat(entryPage).isNotNull();
					assertThat(entryPage.getTotalElements()).isEqualTo(2);
					assertEntry99999(entryPage.getContent().get(0)).assertThatContentIsNotSet();
					assertEntry99997(entryPage.getContent().get(1)).assertThatContentIsNotSet();
				});
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
					.add("number=" + number)
					.add("size=" + size)
					.add("totalElements=" + totalElements)
					.toString();
		}
	}
}
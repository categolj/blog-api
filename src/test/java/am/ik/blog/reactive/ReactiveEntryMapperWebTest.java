package am.ik.blog.reactive;

import am.ik.blog.entry.Author;
import am.ik.blog.entry.Categories;
import am.ik.blog.entry.Category;
import am.ik.blog.entry.Content;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryId;
import am.ik.blog.entry.EventTime;
import am.ik.blog.entry.FrontMatter;
import am.ik.blog.entry.Name;
import am.ik.blog.entry.Tag;
import am.ik.blog.entry.Tags;
import am.ik.blog.entry.Title;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import reactor.core.publisher.Mono;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({ "classpath:/delete-test-data.sql", "classpath:/insert-test-data.sql" })
@Import(ReactiveEntryMapperWebTest.DemoHandler.class)
public class ReactiveEntryMapperWebTest {
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
	public void insert() {
		EventTime now = EventTime.now();
		Entry entry = Entry.builder() //
				.entryId(new EntryId(99991L)) //
				.content(new Content("demo")) //
				.created(new Author(new Name("test"), now)) //
				.updated(new Author(new Name("test"), now)) //
				.frontMatter(new FrontMatter(new Title("hello"),
						new Categories(new Category("foo"), new Category("bar")),
						new Tags(new Tag("test1"), new Tag("test2")))) //
				.build().useFrontMatterDate();

		this.webClient.post() //
				.uri("/demo") //
				.syncBody(entry) //
				.exchange().expectBody(JsonNode.class) //
				.consumeWith(r -> {
					JsonNode body = r.getResponseBody();
					assertThat(body).isNotNull();
					assertThat(body.has("entryId")).isTrue();
					assertThat(body.get("entryId").asLong()).isEqualTo(99991);
				});

		this.webClient.get() //
				.uri("/api/entries/99991") //
				.exchange() //
				.expectStatus().isOk() //
				.expectBody(Entry.class) //
				.consumeWith(r -> {
					assertThat(r.getResponseBody()).isNotNull();
					Entry e = r.getResponseBody().useFrontMatterDate();
					assertThat(e).isEqualTo(entry);
				});
	}

	@Test
	public void update() {
		EventTime now = EventTime.now();
		Entry entry = Entry.builder() //
				.entryId(new EntryId(99999L)) //
				.content(new Content("demo")) //
				.created(new Author(new Name("test"), now)) //
				.updated(new Author(new Name("test"), now)) //
				.frontMatter(new FrontMatter(new Title("hello"),
						new Categories(new Category("foo"), new Category("bar")),
						new Tags(new Tag("test1"), new Tag("test2")))) //
				.build().useFrontMatterDate();

		this.webClient.post() //
				.uri("/demo") //
				.syncBody(entry) //
				.exchange().expectBody(JsonNode.class) //
				.consumeWith(r -> {
					JsonNode body = r.getResponseBody();
					assertThat(body).isNotNull();
					assertThat(body.has("entryId")).isTrue();
					assertThat(body.get("entryId").asLong()).isEqualTo(99999);
				});

		this.webClient.get() //
				.uri("/api/entries/99999") //
				.exchange() //
				.expectStatus().isOk() //
				.expectBody(Entry.class) //
				.consumeWith(r -> {
					assertThat(r.getResponseBody()).isNotNull();
					Entry e = r.getResponseBody().useFrontMatterDate();
					assertThat(e).isEqualTo(entry);
				});
	}

	@Test
	public void delete() {
		this.webClient.delete() //
				.uri("/demo/99999") //
				.exchange() //
				.expectBody(JsonNode.class) //
				.consumeWith(r -> {
					JsonNode body = r.getResponseBody();
					assertThat(body).isNotNull();
					assertThat(body.has("entryId")).isTrue();
					assertThat(body.get("entryId").asLong()).isEqualTo(99999);
				});
		this.webClient.get() //
				.uri("/api/entries/99999") //
				.exchange() //
				.expectStatus().isNotFound();
	}

	@RestController
	static public class DemoHandler {
		private final ReactiveEntryMapper entryMapper;

		public DemoHandler(ReactiveEntryMapper entryMapper) {
			this.entryMapper = entryMapper;
		}

		@DeleteMapping("demo/{id}")
		public Mono<?> delete(@PathVariable("id") EntryId entryId) {
			return this.entryMapper.delete(entryId);
		}

		@PostMapping("demo")
		public Mono<?> post(@RequestBody Entry entry) {
			return this.entryMapper.save(entry);
		}
	}

}
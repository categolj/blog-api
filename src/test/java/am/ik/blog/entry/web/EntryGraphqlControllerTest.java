package am.ik.blog.entry.web;

import am.ik.blog.entry.Entry;
import am.ik.blog.util.FileLoader;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.reactive.server.WebTestClient;

import static am.ik.blog.entry.web.Asserts.assertEntry99999;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureJsonTesters
class EntryGraphqlControllerTest {

	final HttpGraphQlTester tester;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	JacksonTester<JsonNode> json;

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine");

	EntryGraphqlControllerTest(@Value("${local.server.port}") int port) {
		this.tester = HttpGraphQlTester
			.builder(WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/graphql"))
			.build();
		;
	}

	@BeforeEach
	public void reset() {
		jdbcTemplate.update(FileLoader.loadAsString("sql/delete-test-data.sql"));
		jdbcTemplate.update(FileLoader.loadAsString("sql/insert-test-data.sql"));
	}

	@Test
	void getEntry() {
		this.tester.documentName("getEntry")
			.variable("entryId", 99999)
			.execute()
			.path("getEntry")
			.entity(Entry.class)
			.satisfies(entry -> assertEntry99999(entry).assertContent());
	}

	@Test
	void getEntryForTenantUnAuthorized() {
		this.tester.documentName("getEntry")
			.variable("entryId", 99999)
			.variable("tenantId", "_")
			.execute()
			.errors()
			.expect(errors -> ErrorType.UNAUTHORIZED.equals(errors.getErrorType()));
	}

	@Test
	void getEntryForTenantAuthorized() {
		this.tester.mutate()
			.headers(headers -> headers.setBasicAuth("blog-ui", "empty"))
			.build()
			.documentName("getEntry")
			.variable("entryId", 99999)
			.variable("tenantId", "_")
			.execute()
			.path("getEntry")
			.entity(Entry.class)
			.satisfies(entry -> assertEntry99999(entry).assertContent());
	}

	@Test
	void getEntries() throws Exception {
		final JsonNode node = this.tester.documentName("getEntriesWithOnlyEntryIdAndTitleAndCursor")
			.variable("first", 2)
			.execute()
			.path("getEntries")
			.entity(JsonNode.class)
			.get();
		assertThat(json.write(node)).isEqualToJson("""
				{
				  "edges" : [ {
				    "node" : {
				      "entryId" : "99999",
				      "frontMatter" : {
				        "title" : "Hello World!!"
				      }
				    }
				  }, {
				    "node" : {
				      "entryId" : "99998",
				      "frontMatter" : {
				        "title" : "Test!!"
				      }
				    }
				  } ],
				  "pageInfo" : {
				    "endCursor" : "2017-04-01T00:00:00Z"
				  }
				}
				""");
	}

	@Test
	void getEntriesAfter() throws Exception {
		final JsonNode node = this.tester.documentName("getEntriesWithOnlyEntryIdAndTitleAndCursor")
			.variable("first", 2)
			.variable("after", "2017-04-01T00:00:00Z")
			.execute()
			.path("getEntries")
			.entity(JsonNode.class)
			.get();
		assertThat(json.write(node)).isEqualToJson("""
				{
				  "edges" : [ {
				    "node" : {
				      "entryId" : "99997",
				      "frontMatter" : {
				        "title" : "CategoLJ 4"
				      }
				    }
				  } ],
				  "pageInfo" : {
				    "endCursor" : "2017-03-31T00:00:00Z"
				  }
				}
				""");
	}

	@Test
	void getEntriesForTenantUnAuthorized() {
		this.tester.documentName("getEntriesWithOnlyEntryIdAndTitleAndCursor")
			.variable("first", 2)
			.variable("tenantId", "_")
			.execute()
			.errors()
			.expect(errors -> ErrorType.UNAUTHORIZED.equals(errors.getErrorType()));
	}

}
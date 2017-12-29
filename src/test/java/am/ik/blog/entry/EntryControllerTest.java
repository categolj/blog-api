package am.ik.blog.entry;

import static am.ik.blog.entry.Asserts.*;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.RestAssured;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({ "classpath:/delete-test-data.sql", "classpath:/insert-test-data.sql" })
public class EntryControllerTest {
	@LocalServerPort
	int port;
	UserInfoServer userInfoServer;
	MockRestServiceServer mockServer;
	@Autowired
	OAuth2RestTemplate restTemplate;
	@Autowired
	ObjectMapper objectMapper;

	@Before
	public void setUp() throws Exception {
		RestAssured.port = port;
		userInfoServer = new UserInfoServer(34539);
		userInfoServer.start();
		setupMock();
	}

	void setupMock() throws Exception {
		mockServer = MockRestServiceServer.bindTo(restTemplate).build();
	}

	@After
	public void tearDown() {
		userInfoServer.shutdown();
	}

	@Test
	public void getEntries() throws Exception {
		given().log().all().get("/api/entries").then().log().all().assertThat()
				.statusCode(200).body("size", equalTo(10)).body("number", equalTo(0))
				.body("totalPages", equalTo(1)).body("totalElements", equalTo(3))
				.body("numberOfElements", equalTo(3)).body("first", equalTo(true))
				.body("last", equalTo(true)).body("content", hasSize(3))
				.body("content[0].entryId", equalTo(99999))
				.body("content[0].created.name", equalTo("making"))
				.body("content[0].created.date", equalTo("2017-04-01T01:00:00+09:00"))
				.body("content[0].updated.name", equalTo("making"))
				.body("content[0].updated.date", equalTo("2017-04-01T02:00:00+09:00"))
				.body("content[0].frontMatter.title", equalTo("Hello World!!"))
				.body("content[0].frontMatter.categories", hasSize(3))
				.body("content[0].frontMatter.categories[0]", equalTo("x"))
				.body("content[0].frontMatter.categories[1]", equalTo("y"))
				.body("content[0].frontMatter.categories[2]", equalTo("z"))
				.body("content[0].frontMatter.tags", hasSize(3))
				.body("content[0].frontMatter.tags[0]", equalTo("test1"))
				.body("content[0].frontMatter.tags[1]", equalTo("test2"))
				.body("content[0].frontMatter.tags[2]", equalTo("test3"))
				.body("content[1].entryId", equalTo(99998))
				.body("content[1].created.name", equalTo("making"))
				.body("content[1].created.date", equalTo("2017-04-01T00:00:00+09:00"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-04-01T00:00:00+09:00"))
				.body("content[1].frontMatter.title", equalTo("Test!!"))
				.body("content[1].frontMatter.categories", hasSize(3))
				.body("content[1].frontMatter.categories[0]", equalTo("a"))
				.body("content[1].frontMatter.categories[1]", equalTo("b"))
				.body("content[1].frontMatter.categories[2]", equalTo("c"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0]", equalTo("test1"))
				.body("content[1].frontMatter.tags[1]", equalTo("test2"))
				.body("content[2].entryId", equalTo(99997))
				.body("content[2].created.name", equalTo("admin"))
				.body("content[2].created.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[2].updated.name", equalTo("making"))
				.body("content[2].updated.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[2].frontMatter.title", equalTo("CategoLJ 4"))
				.body("content[2].frontMatter.categories", hasSize(2))
				.body("content[2].frontMatter.categories[0]", equalTo("x"))
				.body("content[2].frontMatter.categories[1]", equalTo("y"))
				.body("content[2].frontMatter.tags", hasSize(2))
				.body("content[2].frontMatter.tags[0]", equalTo("test1"))
				.body("content[2].frontMatter.tags[1]", equalTo("test3"))
				.body("content[2].frontMatter.point", equalTo(50));
	}

	@Test
	public void searchEntries() throws Exception {
		given().log().all().queryParam("q", "test").get("/api/entries").then().log().all()
				.assertThat().statusCode(200).body("size", equalTo(10))
				.body("number", equalTo(0)).body("totalPages", equalTo(1))
				.body("totalElements", equalTo(3)).body("numberOfElements", equalTo(3))
				.body("first", equalTo(true)).body("last", equalTo(true))
				.body("content", hasSize(3)).body("content[0].entryId", equalTo(99999))
				.body("content[0].created.name", equalTo("making"))
				.body("content[0].created.date", equalTo("2017-04-01T01:00:00+09:00"))
				.body("content[0].updated.name", equalTo("making"))
				.body("content[0].updated.date", equalTo("2017-04-01T02:00:00+09:00"))
				.body("content[0].frontMatter.title", equalTo("Hello World!!"))
				.body("content[0].frontMatter.categories", hasSize(3))
				.body("content[0].frontMatter.categories[0]", equalTo("x"))
				.body("content[0].frontMatter.categories[1]", equalTo("y"))
				.body("content[0].frontMatter.categories[2]", equalTo("z"))
				.body("content[0].frontMatter.tags", hasSize(3))
				.body("content[0].frontMatter.tags[0]", equalTo("test1"))
				.body("content[0].frontMatter.tags[1]", equalTo("test2"))
				.body("content[0].frontMatter.tags[2]", equalTo("test3"))
				.body("content[1].entryId", equalTo(99998))
				.body("content[1].created.name", equalTo("making"))
				.body("content[1].created.date", equalTo("2017-04-01T00:00:00+09:00"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-04-01T00:00:00+09:00"))
				.body("content[1].frontMatter.title", equalTo("Test!!"))
				.body("content[1].frontMatter.categories", hasSize(3))
				.body("content[1].frontMatter.categories[0]", equalTo("a"))
				.body("content[1].frontMatter.categories[1]", equalTo("b"))
				.body("content[1].frontMatter.categories[2]", equalTo("c"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0]", equalTo("test1"))
				.body("content[1].frontMatter.tags[1]", equalTo("test2"))
				.body("content[2].entryId", equalTo(99997))
				.body("content[2].created.name", equalTo("admin"))
				.body("content[2].created.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[2].updated.name", equalTo("making"))
				.body("content[2].updated.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[2].frontMatter.title", equalTo("CategoLJ 4"))
				.body("content[2].frontMatter.categories", hasSize(2))
				.body("content[2].frontMatter.categories[0]", equalTo("x"))
				.body("content[2].frontMatter.categories[1]", equalTo("y"))
				.body("content[2].frontMatter.tags", hasSize(2))
				.body("content[2].frontMatter.tags[0]", equalTo("test1"))
				.body("content[2].frontMatter.tags[1]", equalTo("test3"))
				.body("content[2].frontMatter.point", equalTo(50));
	}

	@Test
	@Sql("classpath:/update-test-data-for-search.sql")
	public void searchEntries_ModifiedData() throws Exception {
		given().log().all().queryParam("q", "test").get("/api/entries").then().log().all()
				.assertThat().statusCode(200).body("size", equalTo(10))
				.body("number", equalTo(0)).body("totalPages", equalTo(1))
				.body("totalElements", equalTo(2)).body("numberOfElements", equalTo(2))
				.body("first", equalTo(true)).body("last", equalTo(true))
				.body("content", hasSize(2)).body("content[0].entryId", equalTo(99999))
				.body("content[0].created.name", equalTo("making"))
				.body("content[0].created.date", equalTo("2017-04-01T01:00:00+09:00"))
				.body("content[0].updated.name", equalTo("making"))
				.body("content[0].updated.date", equalTo("2017-04-01T02:00:00+09:00"))
				.body("content[0].frontMatter.title", equalTo("Hello World!!"))
				.body("content[0].frontMatter.categories", hasSize(3))
				.body("content[0].frontMatter.categories[0]", equalTo("x"))
				.body("content[0].frontMatter.categories[1]", equalTo("y"))
				.body("content[0].frontMatter.categories[2]", equalTo("z"))
				.body("content[0].frontMatter.tags", hasSize(3))
				.body("content[0].frontMatter.tags[0]", equalTo("test1"))
				.body("content[0].frontMatter.tags[1]", equalTo("test2"))
				.body("content[0].frontMatter.tags[2]", equalTo("test3"))
				.body("content[1].entryId", equalTo(99997))
				.body("content[1].created.name", equalTo("admin"))
				.body("content[1].created.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[1].frontMatter.title", equalTo("CategoLJ 4"))
				.body("content[1].frontMatter.categories", hasSize(2))
				.body("content[1].frontMatter.categories[0]", equalTo("x"))
				.body("content[1].frontMatter.categories[1]", equalTo("y"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0]", equalTo("test1"))
				.body("content[1].frontMatter.tags[1]", equalTo("test3"))
				.body("content[1].frontMatter.point", equalTo(50));
	}

	@Test
	public void getEntriesByCreatedBy() throws Exception {
		given().log().all().get("/api/users/{createdBy}/entries", "making").then().log()
				.all().assertThat().statusCode(200).body("size", equalTo(10))
				.body("number", equalTo(0)).body("totalPages", equalTo(1))
				.body("totalElements", equalTo(2)).body("numberOfElements", equalTo(2))
				.body("first", equalTo(true)).body("last", equalTo(true))
				.body("content", hasSize(2)).body("content[0].entryId", equalTo(99999))
				.body("content[0].created.name", equalTo("making"))
				.body("content[0].created.date", equalTo("2017-04-01T01:00:00+09:00"))
				.body("content[0].updated.name", equalTo("making"))
				.body("content[0].updated.date", equalTo("2017-04-01T02:00:00+09:00"))
				.body("content[0].frontMatter.title", equalTo("Hello World!!"))
				.body("content[0].frontMatter.categories", hasSize(3))
				.body("content[0].frontMatter.categories[0]", equalTo("x"))
				.body("content[0].frontMatter.categories[1]", equalTo("y"))
				.body("content[0].frontMatter.categories[2]", equalTo("z"))
				.body("content[0].frontMatter.tags", hasSize(3))
				.body("content[0].frontMatter.tags[0]", equalTo("test1"))
				.body("content[0].frontMatter.tags[1]", equalTo("test2"))
				.body("content[0].frontMatter.tags[2]", equalTo("test3"))
				.body("content[1].entryId", equalTo(99998))
				.body("content[1].created.name", equalTo("making"))
				.body("content[1].created.date", equalTo("2017-04-01T00:00:00+09:00"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-04-01T00:00:00+09:00"))
				.body("content[1].frontMatter.title", equalTo("Test!!"))
				.body("content[1].frontMatter.categories", hasSize(3))
				.body("content[1].frontMatter.categories[0]", equalTo("a"))
				.body("content[1].frontMatter.categories[1]", equalTo("b"))
				.body("content[1].frontMatter.categories[2]", equalTo("c"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0]", equalTo("test1"))
				.body("content[1].frontMatter.tags[1]", equalTo("test2"));
	}

	@Test
	public void getEntriesByUpdatedBy() throws Exception {
		given().queryParam("updated").log().all()
				.get("/api/users/{updatedBy}/entries", "making").then().log().all()
				.assertThat().statusCode(200).body("size", equalTo(10))
				.body("number", equalTo(0)).body("totalPages", equalTo(1))
				.body("totalElements", equalTo(3)).body("numberOfElements", equalTo(3))
				.body("first", equalTo(true)).body("last", equalTo(true))
				.body("content", hasSize(3)).body("content[0].entryId", equalTo(99999))
				.body("content[0].created.name", equalTo("making"))
				.body("content[0].created.date", equalTo("2017-04-01T01:00:00+09:00"))
				.body("content[0].updated.name", equalTo("making"))
				.body("content[0].updated.date", equalTo("2017-04-01T02:00:00+09:00"))
				.body("content[0].frontMatter.title", equalTo("Hello World!!"))
				.body("content[0].frontMatter.categories", hasSize(3))
				.body("content[0].frontMatter.categories[0]", equalTo("x"))
				.body("content[0].frontMatter.categories[1]", equalTo("y"))
				.body("content[0].frontMatter.categories[2]", equalTo("z"))
				.body("content[0].frontMatter.tags", hasSize(3))
				.body("content[0].frontMatter.tags[0]", equalTo("test1"))
				.body("content[0].frontMatter.tags[1]", equalTo("test2"))
				.body("content[0].frontMatter.tags[2]", equalTo("test3"))
				.body("content[1].entryId", equalTo(99998))
				.body("content[1].created.name", equalTo("making"))
				.body("content[1].created.date", equalTo("2017-04-01T00:00:00+09:00"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-04-01T00:00:00+09:00"))
				.body("content[1].frontMatter.title", equalTo("Test!!"))
				.body("content[1].frontMatter.categories", hasSize(3))
				.body("content[1].frontMatter.categories[0]", equalTo("a"))
				.body("content[1].frontMatter.categories[1]", equalTo("b"))
				.body("content[1].frontMatter.categories[2]", equalTo("c"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0]", equalTo("test1"))
				.body("content[1].frontMatter.tags[1]", equalTo("test2"))
				.body("content[2].entryId", equalTo(99997))
				.body("content[2].created.name", equalTo("admin"))
				.body("content[2].created.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[2].updated.name", equalTo("making"))
				.body("content[2].updated.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[2].frontMatter.title", equalTo("CategoLJ 4"))
				.body("content[2].frontMatter.categories", hasSize(2))
				.body("content[2].frontMatter.categories[0]", equalTo("x"))
				.body("content[2].frontMatter.categories[1]", equalTo("y"))
				.body("content[2].frontMatter.tags", hasSize(2))
				.body("content[2].frontMatter.tags[0]", equalTo("test1"))
				.body("content[2].frontMatter.tags[1]", equalTo("test3"))
				.body("content[2].frontMatter.point", equalTo(50));
	}

	@Test
	public void getEntriesByTag() throws Exception {
		given().log().all().get("/api/tags/{tag}/entries", "test3").then().log().all()
				.assertThat().statusCode(200).body("size", equalTo(10))
				.body("number", equalTo(0)).body("totalPages", equalTo(1))
				.body("totalElements", equalTo(2)).body("numberOfElements", equalTo(2))
				.body("first", equalTo(true)).body("last", equalTo(true))
				.body("content", hasSize(2)).body("content[0].entryId", equalTo(99999))
				.body("content[0].created.name", equalTo("making"))
				.body("content[0].created.date", equalTo("2017-04-01T01:00:00+09:00"))
				.body("content[0].updated.name", equalTo("making"))
				.body("content[0].updated.date", equalTo("2017-04-01T02:00:00+09:00"))
				.body("content[0].frontMatter.title", equalTo("Hello World!!"))
				.body("content[0].frontMatter.categories", hasSize(3))
				.body("content[0].frontMatter.categories[0]", equalTo("x"))
				.body("content[0].frontMatter.categories[1]", equalTo("y"))
				.body("content[0].frontMatter.categories[2]", equalTo("z"))
				.body("content[0].frontMatter.tags", hasSize(3))
				.body("content[0].frontMatter.tags[0]", equalTo("test1"))
				.body("content[0].frontMatter.tags[1]", equalTo("test2"))
				.body("content[0].frontMatter.tags[2]", equalTo("test3"))
				.body("content[1].entryId", equalTo(99997))
				.body("content[1].created.name", equalTo("admin"))
				.body("content[1].created.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[1].frontMatter.title", equalTo("CategoLJ 4"))
				.body("content[1].frontMatter.categories", hasSize(2))
				.body("content[1].frontMatter.categories[0]", equalTo("x"))
				.body("content[1].frontMatter.categories[1]", equalTo("y"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0]", equalTo("test1"))
				.body("content[1].frontMatter.tags[1]", equalTo("test3"))
				.body("content[1].frontMatter.point", equalTo(50));
	}

	@Test
	public void getEntriesByCategories() throws Exception {
		given().log().all().get("/api/categories/x,y/entries").then().log().all()
				.assertThat().statusCode(200).body("size", equalTo(10))
				.body("number", equalTo(0)).body("totalPages", equalTo(1))
				.body("totalElements", equalTo(2)).body("numberOfElements", equalTo(2))
				.body("first", equalTo(true)).body("last", equalTo(true))
				.body("content", hasSize(2)).body("content[0].entryId", equalTo(99999))
				.body("content[0].created.name", equalTo("making"))
				.body("content[0].created.date", equalTo("2017-04-01T01:00:00+09:00"))
				.body("content[0].updated.name", equalTo("making"))
				.body("content[0].updated.date", equalTo("2017-04-01T02:00:00+09:00"))
				.body("content[0].frontMatter.title", equalTo("Hello World!!"))
				.body("content[0].frontMatter.categories", hasSize(3))
				.body("content[0].frontMatter.categories[0]", equalTo("x"))
				.body("content[0].frontMatter.categories[1]", equalTo("y"))
				.body("content[0].frontMatter.categories[2]", equalTo("z"))
				.body("content[0].frontMatter.tags", hasSize(3))
				.body("content[0].frontMatter.tags[0]", equalTo("test1"))
				.body("content[0].frontMatter.tags[1]", equalTo("test2"))
				.body("content[0].frontMatter.tags[2]", equalTo("test3"))
				.body("content[1].entryId", equalTo(99997))
				.body("content[1].created.name", equalTo("admin"))
				.body("content[1].created.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[1].frontMatter.title", equalTo("CategoLJ 4"))
				.body("content[1].frontMatter.categories", hasSize(2))
				.body("content[1].frontMatter.categories[0]", equalTo("x"))
				.body("content[1].frontMatter.categories[1]", equalTo("y"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0]", equalTo("test1"))
				.body("content[1].frontMatter.tags[1]", equalTo("test3"))
				.body("content[1].frontMatter.point", equalTo(50));
	}

	@Test
	public void getEntry() throws Exception {
		given().log().all().get("/api/entries/{entryId}", 99999).then().log().all()
				.assertThat().statusCode(200).body("entryId", equalTo(99999))
				.body("content", equalTo("This is a test data."))
				.body("created.name", equalTo("making"))
				.body("created.date", equalTo("2017-04-01T01:00:00+09:00"))
				.body("updated.name", equalTo("making"))
				.body("updated.date", equalTo("2017-04-01T02:00:00+09:00"))
				.body("created.name", equalTo("making"))
				.body("frontMatter.title", equalTo("Hello World!!"))
				.body("frontMatter.categories", hasSize(3))
				.body("frontMatter.categories[0]", equalTo("x"))
				.body("frontMatter.categories[1]", equalTo("y"))
				.body("frontMatter.categories[2]", equalTo("z"))
				.body("frontMatter.tags", hasSize(3))
				.body("frontMatter.tags[0]", equalTo("test1"))
				.body("frontMatter.tags[1]", equalTo("test2"))
				.body("frontMatter.tags[2]", equalTo("test3"));
	}

	@Test
	public void nonExistingEntryShouldReturn404() throws Exception {
		given().log().all().get("/api/entries/{entryId}", 100000).then().log().all()
				.assertThat().statusCode(404)
				.body("message", equalTo("entry 100000 is not found."));
	}

	@Test
	public void invalidEntryIdShouldReturn400() throws Exception {
		given().log().all().get("/api/entries/{entryId}", "foo").then().log().all()
				.assertThat().statusCode(400).body("message",
						equalTo("The given request (entryId = foo) is not valid."));
	}

	@Test
	public void getEntry99999() throws Exception {
		Entry entry = given().log().all().queryParam("excludeContent", "true")
				.get("/api/entries/{entryId}", 99999).then().log().all().assertThat()
				.statusCode(200).extract().as(Entry.class);
		assertEntry99999(entry).assertThatContentIsNotSet();
	}

	@Test
	public void getEntry99998() throws Exception {
		Entry entry = given().log().all().queryParam("excludeContent", "true")
				.get("/api/entries/{entryId}", 99998).then().log().all().assertThat()
				.statusCode(200).extract().as(Entry.class);
		assertEntry99998(entry).assertThatContentIsNotSet();
	}

	@Test
	public void getEntry99997() throws Exception {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("pint", 100);
		response.put("entryIds", asList(99997, 99998));
		mockServer.expect(requestTo("http://blog-point/v1/user"))
				.andRespond(withSuccess(objectMapper.writeValueAsString(response),
						MediaType.APPLICATION_JSON_UTF8));

		Entry entry = given().log().all()
				.header(HttpHeaders.AUTHORIZATION, "Bearer test-user-1")
				.queryParam("excludeContent", "true").get("/api/entries/{entryId}", 99997)
				.then().log().all().assertThat().statusCode(200).extract()
				.as(Entry.class);
		assertEntry99997(entry).assertThatContentIsNotSet();
	}

	@Test
	public void getEntry99997_noToken_excludeContent() throws Exception {
		Entry entry = given().log().all().queryParam("excludeContent", "true")
				.get("/api/entries/{entryId}", 99997).then().log().all().assertThat()
				.statusCode(200).extract().as(Entry.class);
		assertEntry99997(entry).assertThatContentIsNotSet();
	}

	@Test
	public void getEntry99999_includeContent() throws Exception {
		Entry entry = given().log().all().queryParam("excludeContent", "false")
				.get("/api/entries/{entryId}", 99999).then().log().all().assertThat()
				.statusCode(200).extract().as(Entry.class);
		assertEntry99999(entry).assertContent();
	}

	@Test
	public void getEntry99998_includeContent() throws Exception {
		Entry entry = given().log().all().queryParam("excludeContent", "false")
				.get("/api/entries/{entryId}", 99998).then().log().all().assertThat()
				.statusCode(200).extract().as(Entry.class);
		assertEntry99998(entry).assertContent();
	}

	@Test
	public void getEntry99997_includeContent_subscribed() throws Exception {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("point", 100);
		response.put("entryIds", asList(99997, 99998));
		mockServer.expect(requestTo("http://blog-point/v1/user"))
				.andRespond(withSuccess(objectMapper.writeValueAsString(response),
						MediaType.APPLICATION_JSON_UTF8));

		Entry entry = given().log().all()
				.header(HttpHeaders.AUTHORIZATION, "Bearer test-user-1")
				.queryParam("excludeContent", "false")
				.get("/api/entries/{entryId}", 99997).then().log().all().assertThat()
				.statusCode(200).extract().as(Entry.class);
		assertEntry99997(entry).assertContent();
	}

	@Test
	public void getEntry99997_includeContent_not_subscribed() throws Exception {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("point", 100);
		response.put("entryIds", asList(99998));
		mockServer.expect(requestTo("http://blog-point/v1/user"))
				.andRespond(withSuccess(objectMapper.writeValueAsString(response),
						MediaType.APPLICATION_JSON_UTF8));

		JsonNode error = given().log().all()
				.header(HttpHeaders.AUTHORIZATION, "Bearer test-user-1")
				.queryParam("excludeContent", "false")
				.get("/api/entries/{entryId}", 99997).then().log().all().assertThat()
				.statusCode(HttpStatus.PAYMENT_REQUIRED.value()).extract()
				.as(JsonNode.class);
		assertThat(error.get("message").asText())
				.isEqualTo("entry 99997 is not subscribed.");
	}

	@Test
	public void getEntry99997_includeContent_noToken() throws Exception {
		given().log().all().queryParam("excludeContent", "false")
				.get("/api/entries/{entryId}", 99997).then().log().all().assertThat()
				.statusCode(401);
	}

	@Test
	public void getPremiumEntry_notFound() throws Exception {
		JsonNode error = given().log().all()
				.header(HttpHeaders.AUTHORIZATION, "Bearer test-user-1")
				.queryParam("excludeContent", "false")
				.get("/api/p/entries/{entryId}", 99990).then().log().all().assertThat()
				.statusCode(404).extract().as(JsonNode.class);
		assertThat(error.get("message").asText()).isEqualTo("entry 99990 is not found.");
	}
}
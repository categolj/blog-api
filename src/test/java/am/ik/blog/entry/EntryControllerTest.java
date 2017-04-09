package am.ik.blog.entry;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.RestAssured;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({ "classpath:/delete-test-data.sql", "classpath:/insert-test-data.sql" })
public class EntryControllerTest {
	@LocalServerPort
	int port;
	@Autowired
	ObjectMapper objectMapper;

	@Before
	public void setUp() throws Exception {
		RestAssured.port = port;
	}

	@Test
	public void getEntries() throws Exception {
		given().log().all().get("/api/entries").then().log().all().assertThat()
				.statusCode(200).body("size", equalTo(10)).body("number", equalTo(0))
				.body("totalPages", equalTo(1)).body("totalElements", equalTo(3))
				.body("numberOfElements", equalTo(3)).body("first", equalTo(true))
				.body("last", equalTo(true)).body("content", hasSize(3))
				.body("content[0].entryId", equalTo(99999))
				.body("content[0].content", isEmptyString())
				.body("content[0].created.name", equalTo("making"))
				.body("content[0].created.date", equalTo("2017-04-01T01:00:00+0900"))
				.body("content[0].updated.name", equalTo("making"))
				.body("content[0].updated.date", equalTo("2017-04-01T02:00:00+0900"))
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
				.body("content[1].content", isEmptyString())
				.body("content[1].created.name", equalTo("making"))
				.body("content[1].created.date", equalTo("2017-04-01T00:00:00+0900"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-04-01T00:00:00+0900"))
				.body("content[1].frontMatter.title", equalTo("Test!!"))
				.body("content[1].frontMatter.categories", hasSize(3))
				.body("content[1].frontMatter.categories[0]", equalTo("a"))
				.body("content[1].frontMatter.categories[1]", equalTo("b"))
				.body("content[1].frontMatter.categories[2]", equalTo("c"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0]", equalTo("test1"))
				.body("content[1].frontMatter.tags[1]", equalTo("test2"))
				.body("content[2].entryId", equalTo(99997))
				.body("content[2].content", isEmptyString())
				.body("content[2].created.name", equalTo("admin"))
				.body("content[2].created.date", equalTo("2017-03-31T00:00:00+0900"))
				.body("content[2].updated.name", equalTo("making"))
				.body("content[2].updated.date", equalTo("2017-03-31T00:00:00+0900"))
				.body("content[2].frontMatter.title", equalTo("CategoLJ 4"))
				.body("content[2].frontMatter.categories", hasSize(2))
				.body("content[2].frontMatter.categories[0]", equalTo("x"))
				.body("content[2].frontMatter.categories[1]", equalTo("y"))
				.body("content[2].frontMatter.tags", hasSize(2))
				.body("content[2].frontMatter.tags[0]", equalTo("test1"))
				.body("content[2].frontMatter.tags[1]", equalTo("test3"));
	}

	@Test
	public void searchEntries() throws Exception {

	}

	@Test
	public void getEntriesByCreatedBy() throws Exception {
		given().log().all().get("/api/users/{createdBy}/entries", "making").then().log()
				.all().assertThat().statusCode(200).body("size", equalTo(10))
				.body("number", equalTo(0)).body("totalPages", equalTo(1))
				.body("totalElements", equalTo(2)).body("numberOfElements", equalTo(2))
				.body("first", equalTo(true)).body("last", equalTo(true))
				.body("content", hasSize(2)).body("content[0].entryId", equalTo(99999))
				.body("content[0].content", isEmptyString())
				.body("content[0].created.name", equalTo("making"))
				.body("content[0].created.date", equalTo("2017-04-01T01:00:00+0900"))
				.body("content[0].updated.name", equalTo("making"))
				.body("content[0].updated.date", equalTo("2017-04-01T02:00:00+0900"))
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
				.body("content[1].content", isEmptyString())
				.body("content[1].created.name", equalTo("making"))
				.body("content[1].created.date", equalTo("2017-04-01T00:00:00+0900"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-04-01T00:00:00+0900"))
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
				.body("content[0].content", isEmptyString())
				.body("content[0].created.name", equalTo("making"))
				.body("content[0].created.date", equalTo("2017-04-01T01:00:00+0900"))
				.body("content[0].updated.name", equalTo("making"))
				.body("content[0].updated.date", equalTo("2017-04-01T02:00:00+0900"))
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
				.body("content[1].content", isEmptyString())
				.body("content[1].created.name", equalTo("making"))
				.body("content[1].created.date", equalTo("2017-04-01T00:00:00+0900"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-04-01T00:00:00+0900"))
				.body("content[1].frontMatter.title", equalTo("Test!!"))
				.body("content[1].frontMatter.categories", hasSize(3))
				.body("content[1].frontMatter.categories[0]", equalTo("a"))
				.body("content[1].frontMatter.categories[1]", equalTo("b"))
				.body("content[1].frontMatter.categories[2]", equalTo("c"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0]", equalTo("test1"))
				.body("content[1].frontMatter.tags[1]", equalTo("test2"))
				.body("content[2].entryId", equalTo(99997))
				.body("content[2].content", isEmptyString())
				.body("content[2].created.name", equalTo("admin"))
				.body("content[2].created.date", equalTo("2017-03-31T00:00:00+0900"))
				.body("content[2].updated.name", equalTo("making"))
				.body("content[2].updated.date", equalTo("2017-03-31T00:00:00+0900"))
				.body("content[2].frontMatter.title", equalTo("CategoLJ 4"))
				.body("content[2].frontMatter.categories", hasSize(2))
				.body("content[2].frontMatter.categories[0]", equalTo("x"))
				.body("content[2].frontMatter.categories[1]", equalTo("y"))
				.body("content[2].frontMatter.tags", hasSize(2))
				.body("content[2].frontMatter.tags[0]", equalTo("test1"))
				.body("content[2].frontMatter.tags[1]", equalTo("test3"));
	}

	@Test
	public void getEntriesByTag() throws Exception {
		given().log().all().get("/api/tags/{tag}/entries", "test3").then().log().all()
				.assertThat().statusCode(200).body("size", equalTo(10))
				.body("number", equalTo(0)).body("totalPages", equalTo(1))
				.body("totalElements", equalTo(2)).body("numberOfElements", equalTo(2))
				.body("first", equalTo(true)).body("last", equalTo(true))
				.body("content", hasSize(2)).body("content[0].entryId", equalTo(99999))
				.body("content[0].content", isEmptyString())
				.body("content[0].created.name", equalTo("making"))
				.body("content[0].created.date", equalTo("2017-04-01T01:00:00+0900"))
				.body("content[0].updated.name", equalTo("making"))
				.body("content[0].updated.date", equalTo("2017-04-01T02:00:00+0900"))
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
				.body("content[1].content", isEmptyString())
				.body("content[1].created.name", equalTo("admin"))
				.body("content[1].created.date", equalTo("2017-03-31T00:00:00+0900"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-03-31T00:00:00+0900"))
				.body("content[1].frontMatter.title", equalTo("CategoLJ 4"))
				.body("content[1].frontMatter.categories", hasSize(2))
				.body("content[1].frontMatter.categories[0]", equalTo("x"))
				.body("content[1].frontMatter.categories[1]", equalTo("y"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0]", equalTo("test1"))
				.body("content[1].frontMatter.tags[1]", equalTo("test3"));
	}

	@Test
	public void getEntriesByCategories() throws Exception {
		given().log().all().get("/api/categories/x,y/entries").then().log().all()
				.assertThat().statusCode(200).body("size", equalTo(10))
				.body("number", equalTo(0)).body("totalPages", equalTo(1))
				.body("totalElements", equalTo(2)).body("numberOfElements", equalTo(2))
				.body("first", equalTo(true)).body("last", equalTo(true))
				.body("content", hasSize(2)).body("content[0].entryId", equalTo(99999))
				.body("content[0].content", isEmptyString())
				.body("content[0].created.name", equalTo("making"))
				.body("content[0].created.date", equalTo("2017-04-01T01:00:00+0900"))
				.body("content[0].updated.name", equalTo("making"))
				.body("content[0].updated.date", equalTo("2017-04-01T02:00:00+0900"))
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
				.body("content[1].content", isEmptyString())
				.body("content[1].created.name", equalTo("admin"))
				.body("content[1].created.date", equalTo("2017-03-31T00:00:00+0900"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-03-31T00:00:00+0900"))
				.body("content[1].frontMatter.title", equalTo("CategoLJ 4"))
				.body("content[1].frontMatter.categories", hasSize(2))
				.body("content[1].frontMatter.categories[0]", equalTo("x"))
				.body("content[1].frontMatter.categories[1]", equalTo("y"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0]", equalTo("test1"))
				.body("content[1].frontMatter.tags[1]", equalTo("test3"));
	}

	@Test
	public void getEntry() throws Exception {
		given().log().all().get("/api/entries/{entryId}", 99999).then().log().all()
				.assertThat().statusCode(200).body("entryId", equalTo(99999))
				.body("content", isEmptyString()).body("created.name", equalTo("making"))
				.body("created.date", equalTo("2017-04-01T01:00:00+0900"))
				.body("updated.name", equalTo("making"))
				.body("updated.date", equalTo("2017-04-01T02:00:00+0900"))
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

}
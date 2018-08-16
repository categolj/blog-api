package am.ik.blog.entry;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static am.ik.blog.entry.Asserts.*;
import static io.restassured.RestAssured.given;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.IF_MODIFIED_SINCE;
import static org.springframework.http.HttpHeaders.LAST_MODIFIED;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({ "classpath:/delete-test-data.sql", "classpath:/insert-test-data.sql" })
@AutoConfigureRestDocs
public class EntryControllerTest {
	@LocalServerPort
	int port;
	@Autowired
	RequestSpecification documentationSpec;
	private final OffsetDateTime lastModifiedDate99998 = OffsetDateTime.of(2017, 4, 1, 0,
			0, 0, 0, ZoneOffset.ofHours(9));
	private final OffsetDateTime lastModifiedDate99999 = OffsetDateTime.of(2017, 4, 1, 2,
			0, 0, 0, ZoneOffset.ofHours(9));

	@Before
	public void setUp() throws Exception {
		RestAssured.port = port;
	}

	@Test
	public void getEntries() throws Exception {
		given()
//		given(this.documentationSpec) //
//				.filter(document("api/get-entries", uri(),
//						preprocessResponse(prettyPrint()),
//						responseFields(
//								fieldWithPath("content[].entryId")
//										.description("Entry ID"),
//								fieldWithPath("content[].content").description("Content")
//										.optional(),
//								fieldWithPath("content[].frontMatter.title")
//										.description("Title"),
//								fieldWithPath("content[].frontMatter.categories")
//										.description("Categories"),
//								fieldWithPath("content[].frontMatter.tags")
//										.description("Tags"),
//								fieldWithPath("content[].frontMatter.point")
//										.description("Point (Deprecated)").optional(),
//								fieldWithPath("content[].created.name")
//										.description("Creator's name"),
//								fieldWithPath("content[].created.date")
//										.description("Created date"),
//								fieldWithPath("content[].updated.name")
//										.description("Updater's name"),
//								fieldWithPath("content[].updated.date")
//										.description("Updated date"),
//								fieldWithPath("last").description("Is last"),
//								fieldWithPath("first").description("Is first"),
//								fieldWithPath("totalPages").description("Total pages"),
//								fieldWithPath("totalElements")
//										.description("Total elements"),
//								fieldWithPath("size").description("Size"),
//								fieldWithPath("number").description("Number"),
//								fieldWithPath("numberOfElements")
//										.description("Number of elements"),
//								fieldWithPath("pageable.sort.sorted")
//										.description("Is sorted"),
//								fieldWithPath("pageable.sort.unsorted")
//										.description("Is unsorted"),
//								fieldWithPath("pageable.offset").description("Offset"),
//								fieldWithPath("pageable.pageSize")
//										.description("Page size"),
//								fieldWithPath("pageable.pageNumber")
//										.description("Page number"),
//								fieldWithPath("pageable.paged").description("Is paged"),
//								fieldWithPath("pageable.unpaged")
//										.description("Is unpaged"),
//								fieldWithPath("sort.sorted").description("Is sorted"),
//								fieldWithPath("sort.unsorted")
//										.description("Is unsorted"))))
				.log().all() //
				.get("/api/entries").then() //
				.log().all() //
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
				.body("content[2].frontMatter.tags[1]", equalTo("test3"));
	}

	@Test
	public void searchEntries() throws Exception {
		given()
//		given(this.documentationSpec) //
//				.filter(document("api/search-entries", uri(),
//						preprocessResponse(prettyPrint()))) //
				.log().all().queryParam("q", "test").get("/api/entries").then().log()
				.all().assertThat().statusCode(200).body("size", equalTo(10))
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
				.body("content[2].frontMatter.tags[1]", equalTo("test3"));
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
				.body("content[1].frontMatter.tags[1]", equalTo("test3"));
	}

	@Test
	public void getEntriesByCreatedBy() throws Exception {
		given()
//		given(this.documentationSpec) //
//				.filter(document("api/get-entries-by-created-by", uri(),
//						preprocessResponse(prettyPrint()))) //
				.log().all().get("/api/users/{createdBy}/entries", "making").then().log()
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
		given()
//		given(this.documentationSpec) //
//				.filter(document("api/get-entries-by-updated-by", uri(),
//						preprocessResponse(prettyPrint()))) //
				.queryParam("updated").log().all()
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
				.body("content[2].frontMatter.tags[1]", equalTo("test3"));
	}

	@Test
	public void getEntriesByTag() throws Exception {
		given()
//		given(this.documentationSpec) //
//				.filter(document("api/get-entries-by-tag", uri(),
//						preprocessResponse(prettyPrint()))) //
				.log().all().get("/api/tags/{tag}/entries", "test3").then().log().all()
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
				.body("content[1].frontMatter.tags[1]", equalTo("test3"));
	}

	@Test
	public void getEntriesByCategories() throws Exception {
		given()
//		given(this.documentationSpec) //
//				.filter(document("api/get-entries-by-categories", uri(),
//						preprocessResponse(prettyPrint()))) //
				.log().all().get("/api/categories/x,y/entries").then().log().all()
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
				.body("content[1].frontMatter.tags[1]", equalTo("test3"));
	}

	@Test
	public void getEntry() throws Exception {
		given()
//		given(this.documentationSpec) //
//				.filter(document("api/get-an-entry", uri(),
//						preprocessResponse(prettyPrint()),
//						responseFields(fieldWithPath("entryId").description("Entry ID"),
//								fieldWithPath("content").description("Content")
//										.optional(),
//								fieldWithPath("frontMatter.title").description("Title"),
//								fieldWithPath("frontMatter.categories")
//										.description("Categories"),
//								fieldWithPath("frontMatter.tags").description("Tags"),
//								fieldWithPath("frontMatter.point")
//										.description("Point (Deprecated)")
//										.type(Number.class.getSimpleName()).optional(),
//								fieldWithPath("created.name")
//										.description("Creator's name"),
//								fieldWithPath("created.date").description("Created date"),
//								fieldWithPath("updated.name")
//										.description("Updater's name"),
//								fieldWithPath("updated.date")
//										.description("Updated date")))) //
				.log().all().get("/api/entries/{entryId}", 99999).then().log().all()
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
				.assertThat().statusCode(400)
				.body("message", equalTo("The given request (foo) is not valid."));
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
	public void headEntry99998() throws Exception {
		given().log().all() //
				.head("/api/entries/{entryId}", 99998) //
				.then() //
				.log().all() //
				.assertThat()
				.header(LAST_MODIFIED, lastModifiedDate99998.format(RFC_1123_DATE_TIME)) //
				.assertThat().statusCode(200);
	}

	@Test
	public void headEntry99998_If_Modified_Since_304() throws Exception {
		given().log().all() //
				.header(IF_MODIFIED_SINCE,
						lastModifiedDate99998.format(RFC_1123_DATE_TIME)) //
				.head("/api/entries/{entryId}", 99998) //
				.then() //
				.log().all() //
				.assertThat()
				.header(LAST_MODIFIED, lastModifiedDate99998.format(RFC_1123_DATE_TIME)) //
				.assertThat().statusCode(304);
	}

	@Test
	public void headEntry99998_If_Modified_Since_200() throws Exception {
		given().log().all() //
				.header(IF_MODIFIED_SINCE,
						lastModifiedDate99998.minusMinutes(1).format(RFC_1123_DATE_TIME)) //
				.head("/api/entries/{entryId}", 99998) //
				.then() //
				.log().all() //
				.assertThat()
				.header(LAST_MODIFIED, lastModifiedDate99998.format(RFC_1123_DATE_TIME)) //
				.assertThat().statusCode(200);
	}

	@Test
	public void headEntries() throws Exception {
		given().log().all() //
				.head("/api/entries") //
				.then() //
				.log().all() //
				.assertThat()
				.header(LAST_MODIFIED, lastModifiedDate99999.format(RFC_1123_DATE_TIME)) //
				.assertThat().statusCode(200);
	}

	@Test
	public void headEntries_If_Modified_Since_304() throws Exception {
		given().log().all() //
				.header(IF_MODIFIED_SINCE,
						lastModifiedDate99999.format(RFC_1123_DATE_TIME)) //
				.head("/api/entries") //
				.then() //
				.log().all() //
				.assertThat()
				.header(LAST_MODIFIED, lastModifiedDate99999.format(RFC_1123_DATE_TIME)) //
				.assertThat().statusCode(304);
	}

	@Test
	public void headEntries_If_Modified_Since_200() throws Exception {
		given().log().all() //
				.header(IF_MODIFIED_SINCE,
						lastModifiedDate99999.minusMinutes(1).format(RFC_1123_DATE_TIME)) //
				.head("/api/entries") //
				.then() //
				.log().all() //
				.assertThat()
				.header(LAST_MODIFIED, lastModifiedDate99999.format(RFC_1123_DATE_TIME)) //
				.assertThat().statusCode(200);
	}

	private static OperationRequestPreprocessor uri() {
		return preprocessRequest(
				modifyUris().scheme("https").host("api.example.com").removePort());
	}
}
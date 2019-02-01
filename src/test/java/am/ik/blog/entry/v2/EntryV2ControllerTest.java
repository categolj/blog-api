package am.ik.blog.entry.v2;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static am.ik.blog.entry.v2.AssertsV2.assertEntry99997;
import static am.ik.blog.entry.v2.AssertsV2.assertEntry99998;
import static am.ik.blog.entry.v2.AssertsV2.assertEntry99999;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.resourceDetails;
import static io.restassured.RestAssured.given;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
import static org.springframework.http.HttpHeaders.EXPIRES;
import static org.springframework.http.HttpHeaders.IF_MODIFIED_SINCE;
import static org.springframework.http.HttpHeaders.LAST_MODIFIED;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({ "classpath:/delete-test-data.sql", "classpath:/insert-test-data.sql" })
@AutoConfigureRestDocs
public class EntryV2ControllerTest {
	@Value("${restdoc.scheme:http}")
	private String restdocScheme;
	@Value("${restdoc.host:localhost}")
	private String restdocHost;
	@Value("${restdoc.port:8080}")
	private int restdocPort;
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
		given(this.documentationSpec) //
				.filter(RestAssuredRestDocumentationWrapper.document("get-entries",
						resourceDetails().description("Get entries"), //
						uri(), //
						preprocessResponse(prettyPrint()), //
						entriesResponseFields())) //
				.log().all() //
				.when().port(this.port).get("/entries").then() //
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
				.body("content[0].frontMatter.categories[0].name", equalTo("x"))
				.body("content[0].frontMatter.categories[1].name", equalTo("y"))
				.body("content[0].frontMatter.categories[2].name", equalTo("z"))
				.body("content[0].frontMatter.tags", hasSize(3))
				.body("content[0].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[0].frontMatter.tags[1].tag.name", equalTo("test2"))
				.body("content[0].frontMatter.tags[2].tag.name", equalTo("test3"))
				.body("content[1].entryId", equalTo(99998))
				.body("content[1].created.name", equalTo("making"))
				.body("content[1].created.date", equalTo("2017-04-01T00:00:00+09:00"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-04-01T00:00:00+09:00"))
				.body("content[1].frontMatter.title", equalTo("Test!!"))
				.body("content[1].frontMatter.categories", hasSize(3))
				.body("content[1].frontMatter.categories[0].name", equalTo("a"))
				.body("content[1].frontMatter.categories[1].name", equalTo("b"))
				.body("content[1].frontMatter.categories[2].name", equalTo("c"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[1].frontMatter.tags[1].tag.name", equalTo("test2"))
				.body("content[2].entryId", equalTo(99997))
				.body("content[2].created.name", equalTo("admin"))
				.body("content[2].created.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[2].updated.name", equalTo("making"))
				.body("content[2].updated.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[2].frontMatter.title", equalTo("CategoLJ 4"))
				.body("content[2].frontMatter.categories", hasSize(2))
				.body("content[2].frontMatter.categories[0].name", equalTo("x"))
				.body("content[2].frontMatter.categories[1].name", equalTo("y"))
				.body("content[2].frontMatter.tags", hasSize(2))
				.body("content[2].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[2].frontMatter.tags[1].tag.name", equalTo("test3"));
	}

	@Test
	public void searchEntries() throws Exception {
		given(this.documentationSpec) //
				.filter(RestAssuredRestDocumentationWrapper.document("search-entries",
						resourceDetails().description("Search entries"), //
						uri(), //
						preprocessResponse(prettyPrint()), //
						requestParameters(
								parameterWithName("q").description("A search query")
										.optional(),
								parameterWithName("excludeContent").description(
										"Whether the entry includes content (true/false)")
										.optional()), //
						entriesResponseFields())) //
				.log().all() //
				.queryParam("q", "test") //
				.queryParam("excludeContent", true) //
				.port(this.port).get("/entries").then().log().all().assertThat()
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
				.body("content[0].frontMatter.categories[0].name", equalTo("x"))
				.body("content[0].frontMatter.categories[1].name", equalTo("y"))
				.body("content[0].frontMatter.categories[2].name", equalTo("z"))
				.body("content[0].frontMatter.tags", hasSize(3))
				.body("content[0].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[0].frontMatter.tags[1].tag.name", equalTo("test2"))
				.body("content[0].frontMatter.tags[2].tag.name", equalTo("test3"))
				.body("content[1].entryId", equalTo(99998))
				.body("content[1].created.name", equalTo("making"))
				.body("content[1].created.date", equalTo("2017-04-01T00:00:00+09:00"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-04-01T00:00:00+09:00"))
				.body("content[1].frontMatter.title", equalTo("Test!!"))
				.body("content[1].frontMatter.categories", hasSize(3))
				.body("content[1].frontMatter.categories[0].name", equalTo("a"))
				.body("content[1].frontMatter.categories[1].name", equalTo("b"))
				.body("content[1].frontMatter.categories[2].name", equalTo("c"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[1].frontMatter.tags[1].tag.name", equalTo("test2"))
				.body("content[2].entryId", equalTo(99997))
				.body("content[2].created.name", equalTo("admin"))
				.body("content[2].created.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[2].updated.name", equalTo("making"))
				.body("content[2].updated.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[2].frontMatter.title", equalTo("CategoLJ 4"))
				.body("content[2].frontMatter.categories", hasSize(2))
				.body("content[2].frontMatter.categories[0].name", equalTo("x"))
				.body("content[2].frontMatter.categories[1].name", equalTo("y"))
				.body("content[2].frontMatter.tags", hasSize(2))
				.body("content[2].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[2].frontMatter.tags[1].tag.name", equalTo("test3"));
	}

	@Test
	@Sql("classpath:/update-test-data-for-search.sql")
	public void searchEntries_ModifiedData() throws Exception {
		given().log().all().queryParam("q", "test").get("/entries").then().log().all()
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
				.body("content[0].frontMatter.categories[0].name", equalTo("x"))
				.body("content[0].frontMatter.categories[1].name", equalTo("y"))
				.body("content[0].frontMatter.categories[2].name", equalTo("z"))
				.body("content[0].frontMatter.tags", hasSize(3))
				.body("content[0].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[0].frontMatter.tags[1].tag.name", equalTo("test2"))
				.body("content[0].frontMatter.tags[2].tag.name", equalTo("test3"))
				.body("content[1].entryId", equalTo(99997))
				.body("content[1].created.name", equalTo("admin"))
				.body("content[1].created.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[1].frontMatter.title", equalTo("CategoLJ 4"))
				.body("content[1].frontMatter.categories", hasSize(2))
				.body("content[1].frontMatter.categories[0].name", equalTo("x"))
				.body("content[1].frontMatter.categories[1].name", equalTo("y"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[1].frontMatter.tags[1].tag.name", equalTo("test3"));
	}

	@Test
	public void getEntriesByCreatedBy() throws Exception {
		given(this.documentationSpec) //
				.filter(RestAssuredRestDocumentationWrapper.document(
						"get-entries-by-created-by",
						resourceDetails().description("Get entries by created by"), uri(),
						preprocessResponse(prettyPrint()), //
						entriesResponseFields())) //
				.log().all().port(this.port).get("/users/{createdBy}/entries", "making")
				.then().log().all().assertThat().statusCode(200).body("size", equalTo(10))
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
				.body("content[0].frontMatter.categories[0].name", equalTo("x"))
				.body("content[0].frontMatter.categories[1].name", equalTo("y"))
				.body("content[0].frontMatter.categories[2].name", equalTo("z"))
				.body("content[0].frontMatter.tags", hasSize(3))
				.body("content[0].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[0].frontMatter.tags[1].tag.name", equalTo("test2"))
				.body("content[0].frontMatter.tags[2].tag.name", equalTo("test3"))
				.body("content[1].entryId", equalTo(99998))
				.body("content[1].created.name", equalTo("making"))
				.body("content[1].created.date", equalTo("2017-04-01T00:00:00+09:00"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-04-01T00:00:00+09:00"))
				.body("content[1].frontMatter.title", equalTo("Test!!"))
				.body("content[1].frontMatter.categories", hasSize(3))
				.body("content[1].frontMatter.categories[0].name", equalTo("a"))
				.body("content[1].frontMatter.categories[1].name", equalTo("b"))
				.body("content[1].frontMatter.categories[2].name", equalTo("c"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[1].frontMatter.tags[1].tag.name", equalTo("test2"));
	}

	@Test
	public void getEntriesByUpdatedBy() throws Exception {
		given(this.documentationSpec) //
				.filter(RestAssuredRestDocumentationWrapper.document(
						"get-entries-by-updated-by",
						resourceDetails().description("Get entries by updated by"), uri(),
						preprocessResponse(prettyPrint()), //
						entriesResponseFields())) //
				.queryParam("updated").log().all().port(this.port)
				.get("/users/{updatedBy}/entries", "making").then().log().all()
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
				.body("content[0].frontMatter.categories[0].name", equalTo("x"))
				.body("content[0].frontMatter.categories[1].name", equalTo("y"))
				.body("content[0].frontMatter.categories[2].name", equalTo("z"))
				.body("content[0].frontMatter.tags", hasSize(3))
				.body("content[0].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[0].frontMatter.tags[1].tag.name", equalTo("test2"))
				.body("content[0].frontMatter.tags[2].tag.name", equalTo("test3"))
				.body("content[1].entryId", equalTo(99998))
				.body("content[1].created.name", equalTo("making"))
				.body("content[1].created.date", equalTo("2017-04-01T00:00:00+09:00"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-04-01T00:00:00+09:00"))
				.body("content[1].frontMatter.title", equalTo("Test!!"))
				.body("content[1].frontMatter.categories", hasSize(3))
				.body("content[1].frontMatter.categories[0].name", equalTo("a"))
				.body("content[1].frontMatter.categories[1].name", equalTo("b"))
				.body("content[1].frontMatter.categories[2].name", equalTo("c"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[1].frontMatter.tags[1].tag.name", equalTo("test2"))
				.body("content[2].entryId", equalTo(99997))
				.body("content[2].created.name", equalTo("admin"))
				.body("content[2].created.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[2].updated.name", equalTo("making"))
				.body("content[2].updated.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[2].frontMatter.title", equalTo("CategoLJ 4"))
				.body("content[2].frontMatter.categories", hasSize(2))
				.body("content[2].frontMatter.categories[0].name", equalTo("x"))
				.body("content[2].frontMatter.categories[1].name", equalTo("y"))
				.body("content[2].frontMatter.tags", hasSize(2))
				.body("content[2].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[2].frontMatter.tags[1].tag.name", equalTo("test3"));
	}

	@Test
	public void getEntriesByTag() throws Exception {
		given(this.documentationSpec) //
				.filter(RestAssuredRestDocumentationWrapper.document("get-entries-by-tag",
						resourceDetails().description("Get entries by tag"), uri(),
						preprocessResponse(prettyPrint()), //
						entriesResponseFields())) //
				.log().all().port(this.port).get("/tags/{tag}/entries", "test3").then()
				.log().all().assertThat().statusCode(200).body("size", equalTo(10))
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
				.body("content[0].frontMatter.categories[0].name", equalTo("x"))
				.body("content[0].frontMatter.categories[1].name", equalTo("y"))
				.body("content[0].frontMatter.categories[2].name", equalTo("z"))
				.body("content[0].frontMatter.tags", hasSize(3))
				.body("content[0].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[0].frontMatter.tags[1].tag.name", equalTo("test2"))
				.body("content[0].frontMatter.tags[2].tag.name", equalTo("test3"))
				.body("content[1].entryId", equalTo(99997))
				.body("content[1].created.name", equalTo("admin"))
				.body("content[1].created.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[1].frontMatter.title", equalTo("CategoLJ 4"))
				.body("content[1].frontMatter.categories", hasSize(2))
				.body("content[1].frontMatter.categories[0].name", equalTo("x"))
				.body("content[1].frontMatter.categories[1].name", equalTo("y"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[1].frontMatter.tags[1].tag.name", equalTo("test3"));
	}

	@Test
	public void getEntriesByCategories() throws Exception {
		given(this.documentationSpec) //
				.filter(RestAssuredRestDocumentationWrapper.document(
						"get-entries-by-categories",
						resourceDetails().description("Get entries by categories"), uri(),
						preprocessResponse(prettyPrint()), //
						entriesResponseFields())) //
				.log().all().port(this.port)
				.get("/categories/{categories}/entries", "x,y").then().log().all()
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
				.body("content[0].frontMatter.categories[0].name", equalTo("x"))
				.body("content[0].frontMatter.categories[1].name", equalTo("y"))
				.body("content[0].frontMatter.categories[2].name", equalTo("z"))
				.body("content[0].frontMatter.tags", hasSize(3))
				.body("content[0].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[0].frontMatter.tags[1].tag.name", equalTo("test2"))
				.body("content[0].frontMatter.tags[2].tag.name", equalTo("test3"))
				.body("content[1].entryId", equalTo(99997))
				.body("content[1].created.name", equalTo("admin"))
				.body("content[1].created.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[1].updated.name", equalTo("making"))
				.body("content[1].updated.date", equalTo("2017-03-31T00:00:00+09:00"))
				.body("content[1].frontMatter.title", equalTo("CategoLJ 4"))
				.body("content[1].frontMatter.categories", hasSize(2))
				.body("content[1].frontMatter.categories[0].name", equalTo("x"))
				.body("content[1].frontMatter.categories[1].name", equalTo("y"))
				.body("content[1].frontMatter.tags", hasSize(2))
				.body("content[1].frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("content[1].frontMatter.tags[1].tag.name", equalTo("test3"));
	}

	@Test
	public void getEntry() throws Exception {
		given(this.documentationSpec) //
				.filter(RestAssuredRestDocumentationWrapper.document("get-an-entry",
						resourceDetails().description("Get an entry"), uri(),
						preprocessResponse(prettyPrint()), //
						entryResponseFields())) //
				.log().all().port(this.port).get("/entries/{entryId}", 99999).then().log()
				.all().assertThat().statusCode(200).body("entryId", equalTo(99999))
				.body("content", equalTo("This is a test data."))
				.body("created.name", equalTo("making"))
				.body("created.date", equalTo("2017-04-01T01:00:00+09:00"))
				.body("updated.name", equalTo("making"))
				.body("updated.date", equalTo("2017-04-01T02:00:00+09:00"))
				.body("created.name", equalTo("making"))
				.body("frontMatter.title", equalTo("Hello World!!"))
				.body("frontMatter.categories", hasSize(3))
				.body("frontMatter.categories[0].name", equalTo("x"))
				.body("frontMatter.categories[1].name", equalTo("y"))
				.body("frontMatter.categories[2].name", equalTo("z"))
				.body("frontMatter.tags", hasSize(3))
				.body("frontMatter.tags[0].tag.name", equalTo("test1"))
				.body("frontMatter.tags[1].tag.name", equalTo("test2"))
				.body("frontMatter.tags[2].tag.name", equalTo("test3"));
	}

	@Test
	public void nonExistingEntryShouldReturn404() throws Exception {
		given() //
				.log().all().get("/entries/{entryId}", 100000).then().log().all()
				.assertThat().statusCode(404)
				.body("message", equalTo("entry 100000 is not found.")) //
				.body("b3", notNullValue());
	}

	@Test
	public void invalidEntryIdShouldReturn400() throws Exception {
		given() //
				.log().all().get("/entries/{entryId}", "foo").then().log().all()
				.assertThat().statusCode(400)
				.body("message", equalTo("The given request (foo) is not valid.")) //
				.body("b3", notNullValue());
	}

	@Test
	public void getEntry99999() throws Exception {
		EntryV2 entry = given().log().all().queryParam("excludeContent", "true")
				.get("/entries/{entryId}", 99999).then().log().all().assertThat()
				.statusCode(200).extract().as(EntryV2.class);
		assertEntry99999(entry).assertThatContentIsNotSet();
	}

	@Test
	public void getEntry99998() throws Exception {
		EntryV2 entry = given().log().all().queryParam("excludeContent", "true")
				.get("/entries/{entryId}", 99998).then().log().all().assertThat()
				.statusCode(200).extract().as(EntryV2.class);
		assertEntry99998(entry).assertThatContentIsNotSet();
	}

	@Test
	public void getEntry99997_noToken_excludeContent() throws Exception {
		EntryV2 entry = given().log().all().queryParam("excludeContent", "true")
				.get("/entries/{entryId}", 99997).then().log().all().assertThat()
				.statusCode(200).extract().as(EntryV2.class);
		assertEntry99997(entry).assertThatContentIsNotSet();
	}

	@Test
	public void getEntry99999_includeContent() throws Exception {
		EntryV2 entry = given().log().all().queryParam("excludeContent", "false")
				.get("/entries/{entryId}", 99999).then().log().all().assertThat()
				.statusCode(200).extract().as(EntryV2.class);
		assertEntry99999(entry).assertContent();
	}

	@Test
	public void getEntry99998_includeContent() throws Exception {
		EntryV2 entry = given().log().all().queryParam("excludeContent", "false")
				.get("/entries/{entryId}", 99998).then().log().all().assertThat()
				.statusCode(200).extract().as(EntryV2.class);
		assertEntry99998(entry).assertContent();
	}

	@Test
	public void headEntry99998() throws Exception {
		given().log().all() //
				.head("/entries/{entryId}", 99998) //
				.then() //
				.log().all() //
				.assertThat()
				.header(LAST_MODIFIED, lastModifiedDate99998.format(RFC_1123_DATE_TIME)) //
				.header(EXPIRES, lastModifiedDate99998.format(RFC_1123_DATE_TIME)) //
				.header(CACHE_CONTROL, "max-age=0") //
				.assertThat().statusCode(200);
	}

	@Test
	public void headEntry99998_If_Modified_Since_304() throws Exception {
		given().log().all() //
				.header(IF_MODIFIED_SINCE,
						lastModifiedDate99998.format(RFC_1123_DATE_TIME)) //
				.head("/entries/{entryId}", 99998) //
				.then() //
				.log().all() //
				.assertThat()
				.header(LAST_MODIFIED, lastModifiedDate99998.format(RFC_1123_DATE_TIME)) //
				.header(EXPIRES, lastModifiedDate99998.format(RFC_1123_DATE_TIME)) //
				.header(CACHE_CONTROL, "max-age=0") //
				.assertThat().statusCode(304);
	}

	@Test
	public void headEntry99998_If_Modified_Since_200() throws Exception {
		given().log().all() //
				.header(IF_MODIFIED_SINCE,
						lastModifiedDate99998.minusMinutes(1).format(RFC_1123_DATE_TIME)) //
				.head("/entries/{entryId}", 99998) //
				.then() //
				.log().all() //
				.assertThat()
				.header(LAST_MODIFIED, lastModifiedDate99998.format(RFC_1123_DATE_TIME)) //
				.header(EXPIRES, lastModifiedDate99998.format(RFC_1123_DATE_TIME)) //
				.header(CACHE_CONTROL, "max-age=0") //
				.assertThat().statusCode(200);
	}

	@Test
	public void headEntries() throws Exception {
		given().log().all() //
				.head("/entries") //
				.then() //
				.log().all() //
				.assertThat()
				.header(LAST_MODIFIED, lastModifiedDate99999.format(RFC_1123_DATE_TIME)) //
				.header(EXPIRES, lastModifiedDate99999.format(RFC_1123_DATE_TIME)) //
				.header(CACHE_CONTROL, "max-age=0") //
				.assertThat().statusCode(200);
	}

	@Test
	public void headEntries_If_Modified_Since_304() throws Exception {
		given().log().all() //
				.header(IF_MODIFIED_SINCE,
						lastModifiedDate99999.format(RFC_1123_DATE_TIME)) //
				.head("/entries") //
				.then() //
				.log().all() //
				.assertThat()
				.header(LAST_MODIFIED, lastModifiedDate99999.format(RFC_1123_DATE_TIME)) //
				.header(EXPIRES, lastModifiedDate99999.format(RFC_1123_DATE_TIME)) //
				.header(CACHE_CONTROL, "max-age=0") //
				.assertThat().statusCode(304);
	}

	@Test
	public void headEntries_If_Modified_Since_200() throws Exception {
		given().log().all() //
				.header(IF_MODIFIED_SINCE,
						lastModifiedDate99999.minusMinutes(1).format(RFC_1123_DATE_TIME)) //
				.head("/entries") //
				.then() //
				.log().all() //
				.assertThat()
				.header(LAST_MODIFIED, lastModifiedDate99999.format(RFC_1123_DATE_TIME)) //
				.header(EXPIRES, lastModifiedDate99999.format(RFC_1123_DATE_TIME)) //
				.header(CACHE_CONTROL, "max-age=0") //
				.assertThat().statusCode(200);
	}

	private static ResponseFieldsSnippet entryResponseFields() {
		return responseFields(fieldWithPath("entryId").description("Entry ID"),
				fieldWithPath("content").description("Content").optional(),
				fieldWithPath("frontMatter.title").description("Title"),
				fieldWithPath("frontMatter.categories").description("Categories"),
				fieldWithPath("frontMatter.categories[].name")
						.description("Category name"),
				fieldWithPath("frontMatter.tags").description("Tags"),
				fieldWithPath("frontMatter.tags[].tag").description("A tag"),
				fieldWithPath("frontMatter.tags[].tag.name").description("Tag name"),
				fieldWithPath("created.name").description("Creator's name"),
				fieldWithPath("created.date").description("Created date"),
				fieldWithPath("updated.name").description("Updater's name"),
				fieldWithPath("updated.date").description("Updated date"));
	}

	private static ResponseFieldsSnippet entriesResponseFields() {
		return responseFields(fieldWithPath("content[].entryId").description("Entry ID"),
				fieldWithPath("content[].content").description("Content").optional(),
				fieldWithPath("content[].frontMatter.title").description("Title"),
				fieldWithPath("content[].frontMatter.categories")
						.description("Categories"),
				fieldWithPath("content[].frontMatter.categories[].name")
						.description("Category name"),
				fieldWithPath("content[].frontMatter.tags").description("Tags"),
				fieldWithPath("content[].frontMatter.tags[].tag").description("A tag"),
				fieldWithPath("content[].frontMatter.tags[].tag.name")
						.description("Tag name"),
				fieldWithPath("content[].created.name").description("Creator's name"),
				fieldWithPath("content[].created.date").description("Created date"),
				fieldWithPath("content[].updated.name").description("Updater's name"),
				fieldWithPath("content[].updated.date").description("Updated date"),
				fieldWithPath("last").description("Is last"),
				fieldWithPath("first").description("Is first"),
				fieldWithPath("totalPages").description("Total pages"),
				fieldWithPath("totalElements").description("Total elements"),
				fieldWithPath("size").description("Size"),
				fieldWithPath("number").description("Number"),
				fieldWithPath("numberOfElements").description("Number of elements"),
				fieldWithPath("pageable.sort.sorted").description("Is sorted"),
				fieldWithPath("pageable.sort.unsorted").description("Is unsorted"),
				fieldWithPath("pageable.offset").description("Offset"),
				fieldWithPath("pageable.pageSize").description("Page size"),
				fieldWithPath("pageable.pageNumber").description("Page number"),
				fieldWithPath("pageable.paged").description("Is paged"),
				fieldWithPath("pageable.unpaged").description("Is unpaged"),
				fieldWithPath("pageable.sort.empty")
						.description("Whether the pageable.sort is empty or not"),
				fieldWithPath("sort.sorted").description("Is sorted"),
				fieldWithPath("sort.unsorted").description("Is unsorted"),
				fieldWithPath("sort.empty")
						.description("Whether the sort is empty or not"),
				fieldWithPath("empty")
						.description("Whether the content is empty or not"));
	}

	private static ResponseFieldsSnippet errorResponseFields() {
		return responseFields(fieldWithPath("timestamp").description("Timestamp"),
				fieldWithPath("path").description("Error request path"),
				fieldWithPath("status").description("Response status"),
				fieldWithPath("error").description("Error response status"),
				fieldWithPath("message").description("Error message"),
				fieldWithPath("trace").description("Stacktrace"),
				fieldWithPath("b3").description("B3 Trace"));
	}

	private OperationRequestPreprocessor uri() {
		return preprocessRequest(modifyUris() //
				.scheme(this.restdocScheme) //
				.host(this.restdocHost) //
				.port(this.restdocPort));
	}
}
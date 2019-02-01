package am.ik.blog.entry.v2;

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

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.resourceDetails;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({ "classpath:/delete-test-data.sql", "classpath:/insert-test-data.sql" })
@AutoConfigureRestDocs
public class CategoryV2ControllerTest {
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

	@Before
	public void setUp() throws Exception {
		RestAssured.port = port;
	}

	@Test
	public void getCategories() throws Exception {
		given(this.documentationSpec) //
				.filter(RestAssuredRestDocumentationWrapper.document("get-categories",
						resourceDetails().description("Get categories"), //
						uri(), //
						preprocessResponse(prettyPrint()), //
						categoriesResponseFields())) //
				.log().all() //
				.when().port(this.port).get("/categories").then() //
				.log().all() //
				.assertThat().statusCode(200).body("size", equalTo(3))
				.body("[0].categories[0].name", equalTo("a")) //
				.body("[0].categories[1].name", equalTo("b")) //
				.body("[0].categories[2].name", equalTo("c"))
				.body("[1].categories[0].name", equalTo("x")) //
				.body("[1].categories[1].name", equalTo("y")) //
				.body("[2].categories[0].name", equalTo("x")) //
				.body("[2].categories[1].name", equalTo("y")) //
				.body("[2].categories[2].name", equalTo("z"));
	}

	private static ResponseFieldsSnippet categoriesResponseFields() {
		return responseFields(fieldWithPath("[].categories").description("Categories"),
				fieldWithPath("[].categories[].name").description("Category name"));
	}

	private OperationRequestPreprocessor uri() {
		return preprocessRequest(modifyUris() //
				.scheme(this.restdocScheme) //
				.host(this.restdocHost) //
				.port(this.restdocPort));
	}
}
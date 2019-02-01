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
public class TagV2ControllerTest {
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
	public void getTags() throws Exception {
		given(this.documentationSpec) //
				.filter(RestAssuredRestDocumentationWrapper.document("get-tags",
						resourceDetails().description("Get tags"), //
						uri(), //
						preprocessResponse(prettyPrint()), //
						tagsResponseFields())) //
				.log().all() //
				.when().port(this.port).get("/tags").then() //
				.log().all() //
				.assertThat().statusCode(200).body("size", equalTo(3))
				.body("[0].tag.name", equalTo("test1"))
				.body("[1].tag.name", equalTo("test2"))
				.body("[2].tag.name", equalTo("test3"));
	}

	private static ResponseFieldsSnippet tagsResponseFields() {
		return responseFields(fieldWithPath("[].tag").description("A tag"),
				fieldWithPath("[].tag.name").description("Tag name"));
	}

	private OperationRequestPreprocessor uri() {
		return preprocessRequest(modifyUris() //
				.scheme(this.restdocScheme) //
				.host(this.restdocHost) //
				.port(this.restdocPort));
	}
}
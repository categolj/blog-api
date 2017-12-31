package am.ik.blog.entry;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.RestAssured;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({ "classpath:/delete-test-data.sql", "classpath:/insert-test-data.sql" })
public class TagControllerTest {
	@LocalServerPort
	int port;

	@Before
	public void setUp() throws Exception {
		RestAssured.port = port;
	}

	@Test
	public void getTags() throws Exception {
		given().log().all().get("/api/tags").then().log().all().assertThat()
				.statusCode(200).body("$", hasSize(3)).body("[0]", equalTo("test1"))
				.body("[1]", equalTo("test2")).body("[2]", equalTo("test3"));
	}

}
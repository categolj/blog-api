package am.ik.blog.entry;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.RestAssured;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({ "classpath:/delete-test-data.sql", "classpath:/insert-test-data.sql" })
public class CategoryControllerTest {
	@LocalServerPort
	int port;

	@Before
	public void setUp() throws Exception {
		RestAssured.port = port;
	}

	@Test
	public void getCategories() throws Exception {
		given().log().all().get("/api/categories").then().log().all().assertThat()
				.statusCode(200).body("$", hasSize(3)).body("[0]", hasSize(3))
				.body("[0][0]", equalTo("a")).body("[0][1]", equalTo("b"))
				.body("[0][2]", equalTo("c")).body("[1]", hasSize(2))
				.body("[1][0]", equalTo("x")).body("[1][1]", equalTo("y"))
				.body("[2]", hasSize(3)).body("[2][0]", equalTo("x"))
				.body("[2][1]", equalTo("y")).body("[2][2]", equalTo("z"));
	}

}
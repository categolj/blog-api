package am.ik.blog.category.web;

import java.util.List;

import am.ik.blog.category.Category;
import am.ik.blog.category.CategoryMapper;
import am.ik.blog.config.SecurityConfig;
import am.ik.blog.github.GitHubProps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.BDDMockito.given;

@WebMvcTest
@Import({SecurityConfig.class, GitHubProps.class})
class CategoryRestControllerTest {
	@Autowired
	WebTestClient webTestClient;

	@MockBean
	CategoryMapper categoryMapper;

	@ParameterizedTest
	@CsvSource({ ",", "demo," })
	void categories(String tenantId) {
		given(this.categoryMapper.findAll(tenantId)).willReturn(List.of(
				List.of(new Category("a"), new Category("b")),
				List.of(new Category("a"), new Category("b"), new Category("c"))));
		this.webTestClient.get()
				.uri((tenantId == null ? "" : "/tenants/" + tenantId) + "/categories")
				.exchange().expectStatus().isOk().expectBody().jsonPath("$.length()")
				.isEqualTo(2).jsonPath("$.[0].length()").isEqualTo(2)
				.jsonPath("$.[0][0].name").isEqualTo("a").jsonPath("$.[0][1].name")
				.isEqualTo("b").jsonPath("$.[1].length()").isEqualTo(3)
				.jsonPath("$.[1][0].name").isEqualTo("a").jsonPath("$.[1][1].name")
				.isEqualTo("b").jsonPath("$.[1][2].name").isEqualTo("c");
	}
}
package am.ik.blog.category.web;

import java.util.List;

import am.ik.blog.category.Category;
import am.ik.blog.category.CategoryMapper;
import am.ik.blog.config.SecurityConfig;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.BDDMockito.given;

@WebMvcTest
@Import(SecurityConfig.class)
class CategoryRestControllerTest {
	@Autowired
	WebTestClient webTestClient;

	@MockBean
	CategoryMapper categoryMapper;

	@Test
	void categories() {
		given(this.categoryMapper.findAll()).willReturn(List.of(
				List.of(new Category("a"), new Category("b")),
				List.of(new Category("a"), new Category("b"), new Category("c"))));
		this.webTestClient.get().uri("/categories").exchange().expectStatus().isOk()
				.expectBody().jsonPath("$.length()").isEqualTo(2)
				.jsonPath("$.[0].length()").isEqualTo(2).jsonPath("$.[0][0].name")
				.isEqualTo("a").jsonPath("$.[0][1].name").isEqualTo("b")
				.jsonPath("$.[1].length()").isEqualTo(3).jsonPath("$.[1][0].name")
				.isEqualTo("a").jsonPath("$.[1][1].name").isEqualTo("b")
				.jsonPath("$.[1][2].name").isEqualTo("c");
	}
}
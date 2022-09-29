package am.ik.blog.category.web;

import java.util.List;

import am.ik.blog.category.Category;
import am.ik.blog.category.CategoryMapper;
import am.ik.blog.tag.Tag;
import am.ik.blog.tag.TagMapper;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@WebFluxTest
class CategoryRestControllerTest {
	@Autowired
	WebTestClient webTestClient;

	@MockBean
	CategoryMapper categoryMapper;

	@Test
	void categories() {
		given(this.categoryMapper.findAll()).willReturn(Flux.just(List.of(Category.of("a"), Category.of("b")), List.of(Category.of("a"), Category.of("b"), Category.of("c"))));
		this.webTestClient.get()
				.uri("/categories")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.length()").isEqualTo(2)
				.jsonPath("$.[0].length()").isEqualTo(2)
				.jsonPath("$.[0][0].name").isEqualTo("a")
				.jsonPath("$.[0][1].name").isEqualTo("b")
				.jsonPath("$.[1].length()").isEqualTo(3)
				.jsonPath("$.[1][0].name").isEqualTo("a")
				.jsonPath("$.[1][1].name").isEqualTo("b")
				.jsonPath("$.[1][2].name").isEqualTo("c");
	}
}
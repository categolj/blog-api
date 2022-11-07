package am.ik.blog.tag.web;

import java.util.List;

import am.ik.blog.config.SecurityConfig;
import am.ik.blog.tag.Tag;
import am.ik.blog.tag.TagMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.BDDMockito.given;

@WebMvcTest
@Import(SecurityConfig.class)
class TagRestControllerTest {
	@Autowired
	WebTestClient webTestClient;

	@MockBean
	TagMapper tagMapper;

	@Test
	void tags() {
		given(this.tagMapper.findOrderByTagNameAsc()).willReturn(List.of(new Tag("aaa"), new Tag("bbb")));
		this.webTestClient.get()
				.uri("/tags")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.length()").isEqualTo(2)
				.jsonPath("$.[0].name").isEqualTo("aaa")
				.jsonPath("$.[1].name").isEqualTo("bbb");
	}
}
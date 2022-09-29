package am.ik.blog.tag.web;

import am.ik.blog.tag.Tag;
import am.ik.blog.tag.TagMapper;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebFluxTest
class TagRestControllerTest {
	@Autowired
	WebTestClient webTestClient;

	@MockBean
	TagMapper tagMapper;

	@Test
	void tags() {
		given(this.tagMapper.findOrderByTagNameAsc()).willReturn(Flux.just(Tag.of("aaa"), Tag.of("bbb")));
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
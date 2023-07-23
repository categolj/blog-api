package am.ik.blog.tag.web;

import java.util.List;

import am.ik.blog.config.SecurityConfig;
import am.ik.blog.github.GitHubProps;
import am.ik.blog.tag.TagMapper;
import am.ik.blog.tag.TagNameAndCount;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.BDDMockito.given;

@WebMvcTest
@Import({ SecurityConfig.class, GitHubProps.class })
class TagRestControllerTest {
	@Autowired
	WebTestClient webTestClient;

	@MockBean
	TagMapper tagMapper;

	@ParameterizedTest
	@CsvSource({ ",", "demo," })
	void tags(String tenantId) {
		given(this.tagMapper.findOrderByTagNameAsc(tenantId)).willReturn(
				List.of(new TagNameAndCount("aaa", 1), new TagNameAndCount("bbb", 2)));
		this.webTestClient.get()
				.uri((tenantId == null ? "" : "/tenants/" + tenantId) + "/tags")
				.exchange().expectStatus().isOk().expectBody().jsonPath("$.length()")
				.isEqualTo(2).jsonPath("$.[0].name").isEqualTo("aaa")
				.jsonPath("$.[0].count").isEqualTo(1).jsonPath("$.[1].name")
				.isEqualTo("bbb").jsonPath("$.[1].count").isEqualTo(2);
	}
}
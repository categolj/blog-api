package am.ik.blog.tag.web;

import java.util.List;

import am.ik.blog.config.SecurityConfig;
import am.ik.blog.github.GitHubProps;
import am.ik.blog.proto.ProtoUtils;
import am.ik.blog.proto.TagsResponse;
import am.ik.blog.tag.Tag;
import am.ik.blog.tag.TagMapper;
import am.ik.blog.tag.TagAndCount;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebMvcTest
@Import({ SecurityConfig.class, GitHubProps.class })
class TagRestControllerTest {

	@Autowired
	WebTestClient webTestClient;

	@MockitoBean
	TagMapper tagMapper;

	@ParameterizedTest
	@CsvSource({ ",", "demo," })
	void tags(String tenantId) {
		given(this.tagMapper.findOrderByTagNameAsc(tenantId))
			.willReturn(List.of(new TagAndCount(new Tag("aaa"), 1), new TagAndCount(new Tag("bbb"), 2)));
		this.webTestClient.get()
			.uri((tenantId == null ? "" : "/tenants/" + tenantId) + "/tags")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.length()")
			.isEqualTo(2)
			.jsonPath("$.[0].name")
			.isEqualTo("aaa")
			.jsonPath("$.[0].count")
			.isEqualTo(1)
			.jsonPath("$.[1].name")
			.isEqualTo("bbb")
			.jsonPath("$.[1].count")
			.isEqualTo(2);
	}

	@ParameterizedTest
	@CsvSource({ ",", "demo," })
	void tags_protobuf(String tenantId) throws Exception {
		List<TagAndCount> tagAndCounts = List.of(new TagAndCount(new Tag("aaa"), 1),
				new TagAndCount(new Tag("bbb"), 2));
		given(this.tagMapper.findOrderByTagNameAsc(tenantId)).willReturn(tagAndCounts);
		this.webTestClient.get()
			.uri((tenantId == null ? "" : "/tenants/" + tenantId) + "/tags")
			.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_PROTOBUF_VALUE)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(TagsResponse.class)
			.consumeWith(r -> assertThat(r.getResponseBody()).isEqualTo(ProtoUtils.toProtoTagsResponse(tagAndCounts)));
	}

}
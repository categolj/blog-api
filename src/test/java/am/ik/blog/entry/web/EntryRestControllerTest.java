package am.ik.blog.entry.web;

import java.util.List;
import java.util.Optional;

import am.ik.blog.config.SecurityConfig;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryBuilder;
import am.ik.blog.entry.EntryMapper;
import am.ik.blog.entry.EntryService;
import am.ik.blog.entry.FrontMatterBuilder;
import am.ik.blog.github.GitHubUserContentClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@WebMvcTest
@Import(SecurityConfig.class)
class EntryRestControllerTest {
	@Autowired
	WebTestClient webTestClient;

	@MockBean
	EntryMapper entryMapper;

	Entry entry100 = new EntryBuilder()
			.withEntryId(100L)
			.withFrontMatter(new FrontMatterBuilder()
					.withTitle("Hello")
					.build())
			.withContent("Hello World!")
			.build();

	Entry entry200 = new EntryBuilder()
			.withEntryId(200L)
			.withFrontMatter(new FrontMatterBuilder()
					.withTitle("Blog")
					.build())
			.withContent("Hello Blog!")
			.build();

	@Test
	void getEntry_200() {
		given(this.entryMapper.findOne(100L, false))
				.willReturn(Optional.of(this.entry100));
		this.webTestClient.get()
				.uri("/entries/100")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.entryId").isEqualTo(100L)
				.jsonPath("$.content").isEqualTo("Hello World!")
				.jsonPath("$.frontMatter.title").isEqualTo("Hello");
	}

	@Test
	void getEntry_404() {
		given(this.entryMapper.findOne(100L, false))
				.willReturn(Optional.empty());
		this.webTestClient.get()
				.uri("/entries/100")
				.exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void getEntries() {
		given(this.entryMapper.findPage(any(), any())).willReturn(new PageImpl<>(List.of(this.entry100, this.entry200)));
		this.webTestClient.get()
				.uri("/entries")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.content.length()").isEqualTo(2)
				.jsonPath("$.content[0].entryId").isEqualTo(100L)
				.jsonPath("$.content[0].content").isEqualTo("Hello World!")
				.jsonPath("$.content[0].frontMatter.title").isEqualTo("Hello")
				.jsonPath("$.content[1].entryId").isEqualTo(200L)
				.jsonPath("$.content[1].content").isEqualTo("Hello Blog!")
				.jsonPath("$.content[1].frontMatter.title").isEqualTo("Blog");
	}

	@Test
	void delete() {
		given(this.entryMapper.delete(100L)).willReturn(1);
		this.webTestClient.delete()
				.uri("/entries/100")
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.exchange()
				.expectStatus().isNoContent();
		verify(this.entryMapper).delete(100L);
	}

	@TestConfiguration
	static class Config {
		@Bean
		public EntryService entryService(EntryMapper entryMapper) {
			return new EntryService(entryMapper, Mockito.mock(GitHubUserContentClient.class));
		}
	}
}
package am.ik.blog.entry.web;

import java.util.List;

import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryBuilder;
import am.ik.blog.entry.EntryMapper;
import am.ik.blog.entry.FrontMatterBuilder;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebFluxTest
class EntryHandlerTest {
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
				.willReturn(Mono.just(this.entry100));
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
				.willReturn(Mono.empty());
		this.webTestClient.get()
				.uri("/entries/100")
				.exchange()
				.expectStatus().isNotFound()
				.expectBody()
				.jsonPath("$.message").isEqualTo("The requested entry is not found (entryId = 100)")
				.jsonPath("$.status").isEqualTo(404)
				.jsonPath("$.error").isEqualTo("Not Found");
	}

	@Test
	void getEntries() {
		given(this.entryMapper.findPage(any(), any())).willReturn(Mono.just(new PageImpl<>(List.of(this.entry100, this.entry200))));
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

	@Configuration
	static class Config {
		@Bean
		public EntryHandler entryHandler(EntryMapper entryMapper) {
			return new EntryHandler(entryMapper);
		}

		@Bean
		public RouterFunction<ServerResponse> routes(EntryHandler entryHandler) {
			return entryHandler.routes();
		}
	}
}
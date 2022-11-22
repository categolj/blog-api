package am.ik.blog.admin.web;

import am.ik.blog.MockConfig;
import am.ik.blog.config.SecurityConfig;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryMapper;
import am.ik.blog.github.EntryFetcher;
import am.ik.blog.github.Fixtures;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@WebMvcTest(properties = { "spring.security.user.name=admin", "spring.security.user.password=password" })
@Import({ SecurityConfig.class, MockConfig.class })
class EntryImportControllerTest {
	@Autowired
	WebTestClient webTestClient;

	@MockBean
	EntryFetcher entryFetcher;

	@MockBean
	EntryMapper entryMapper;

	@Test
	void importEntries_200() {
		final Entry entry = Fixtures.entry(100L);
		given(this.entryFetcher.fetch("foo", "my-blog", "content/00100.md")).willReturn(Mono.just(entry));
		this.webTestClient.post()
				.uri("/admin/import?from=100&to=100&owner=foo&repo=my-blog")
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "password"))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.length()").isEqualTo(1)
				.jsonPath("[0]", "100 Hello");
		verify(entryMapper).save(entry);
	}

	@Test
	void importEntries_401() {
		final Entry entry = Fixtures.entry(100L);
		given(this.entryFetcher.fetch("foo", "my-blog", "content/00100.md")).willReturn(Mono.just(entry));
		this.webTestClient.post()
				.uri("/admin/import?from=100&to=100&owner=foo&repo=my-blog")
				.exchange()
				.expectStatus().isUnauthorized();
	}
}
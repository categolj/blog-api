package am.ik.blog.admin.web;

import am.ik.blog.MockConfig;
import am.ik.blog.config.SecurityConfig;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryMapper;
import am.ik.blog.github.EntryFetcher;
import am.ik.blog.github.Fixtures;
import am.ik.blog.github.GitHubProps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;

import static org.mockito.BDDMockito.given;

@WebMvcTest(properties = { "spring.security.user.name=admin", "spring.security.user.password=password",
		"blog.github.content-owner=foo", "blog.github.content-repo=my-blog" })
@Import({ SecurityConfig.class, MockConfig.class, GitHubProps.class })
class EntryImportControllerTest {

	@Autowired
	WebTestClient webTestClient;

	@MockitoBean
	EntryFetcher entryFetcher;

	@MockitoBean
	EntryMapper entryMapper;

	@ParameterizedTest
	@CsvSource({ ",", "demo," })
	void importEntries_200(String tenantId) {
		final Entry entry = Fixtures.entry(100L);
		given(this.entryFetcher.fetch(tenantId, "foo", "my-blog", "content/00100.md")).willReturn(Optional.of(entry));
		this.webTestClient.post()
			.uri("%s/admin/import?from=100&to=100".formatted(tenantId == null ? "" : "/tenants/" + tenantId))
			.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "password"))
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.length()")
			.isEqualTo(1)
			.jsonPath("[0]", "100 Hello");
	}

	@ParameterizedTest
	@CsvSource({ ",", "demo," })
	void importEntries_401(String tenantId) {
		final Entry entry = Fixtures.entry(100L);
		given(this.entryFetcher.fetch(tenantId, "foo", "my-blog", "content/00100.md")).willReturn(Optional.of(entry));
		this.webTestClient.post()
			.uri("%s/admin/import?from=100&to=100".formatted(tenantId == null ? "" : "/tenants/" + tenantId))
			.exchange()
			.expectStatus()
			.isUnauthorized();
	}

}
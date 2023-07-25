package am.ik.blog.github;

import am.ik.blog.category.Category;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.FrontMatter;
import am.ik.blog.tag.Tag;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.support.RestTemplateAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class EntryFetcherTest {

	MockWebServer server = new MockWebServer();

	MockWebServer anotherServer = new MockWebServer();

	EntryFetcher entryFetcher;

	int port;

	int anotherPort;

	GitHubClient gitHubClient;

	String path = "content/00001.md";

	@BeforeEach
	void setup() throws Exception {
		this.port = new Random().nextInt(60000, 65534);
		this.anotherPort = this.port + 1;
		this.server.start(this.port);
		this.anotherServer.start(this.anotherPort);
		this.gitHubClient = this.createClient(this.port);
		final GitHubClient anotherGitHubClient = this.createClient(this.anotherPort);
		this.entryFetcher = new EntryFetcher(this.gitHubClient,
				Map.of("xyz", anotherGitHubClient));
		try (Buffer contentResponse = new Buffer()
				.readFrom(new ClassPathResource("github/sample-content-response.json")
						.getInputStream());
				Buffer commitsResponse = new Buffer().readFrom(
						new ClassPathResource("github/sample-commits-response.json")
								.getInputStream())) {
			this.server.setDispatcher(new Dispatcher() {
				@Override
				public MockResponse dispatch(RecordedRequest request) {
					String path = request.getPath();
					if (path.startsWith("/repos/someone/my-blog/commits")) {
						return new MockResponse() //
								.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE) //
								.setBody(commitsResponse) //
								.setResponseCode(200);
					}
					if (path.startsWith("/repos/someone/my-blog/contents")) {
						return new MockResponse() //
								.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE) //
								.setBody(contentResponse) //
								.setResponseCode(200);
					}
					return new MockResponse() //
							.setResponseCode(404);
				}
			});
			this.anotherServer.setDispatcher(new Dispatcher() {
				@Override
				public MockResponse dispatch(RecordedRequest request) {
					String path = request.getPath();
					if (path.startsWith("/repos/foo/bar/commits")) {
						return new MockResponse() //
								.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE) //
								.setBody(commitsResponse) //
								.setResponseCode(200);
					}
					if (path.startsWith("/repos/foo/bar/contents")) {
						return new MockResponse() //
								.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE) //
								.setBody(contentResponse) //
								.setResponseCode(200);
					}
					return new MockResponse() //
							.setResponseCode(404);
				}
			});
		}
	}

	private GitHubClient createClient(int port) {
		final RestTemplateAdapter adapter = RestTemplateAdapter.create(
				new RestTemplateBuilder().rootUri("http://localhost:%d".formatted(port))
						.defaultHeader(HttpHeaders.AUTHORIZATION, "token dummy").build());
		final HttpServiceProxyFactory factory = HttpServiceProxyFactory
				.builderFor(adapter).build();
		return factory.createClient(GitHubClient.class);
	}

	@AfterEach
	void shutdown() throws Exception {
		this.server.shutdown();
		this.anotherServer.shutdown();
	}

	@ParameterizedTest
	@CsvSource({ ",someone,my-blog", "demo,someone,my-blog", "xyz,foo,bar" })
	void fetch(String tenantId, String owner, String repo) throws Exception {
		try (Buffer contentResponse = new Buffer()
				.readFrom(new ClassPathResource("github/sample-content-response.json")
						.getInputStream());
				Buffer commitsResponse = new Buffer().readFrom(
						new ClassPathResource("github/sample-commits-response.json")
								.getInputStream())) {
			final String basePath = "/repos/" + owner + "/" + repo;
			this.server.setDispatcher(new Dispatcher() {
				@Override
				public MockResponse dispatch(RecordedRequest request) {
					String path = request.getPath();
					if (path.startsWith(basePath + "/commits")) {
						return new MockResponse() //
								.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE) //
								.setBody(commitsResponse) //
								.setResponseCode(200);
					}
					if (path.startsWith(basePath + "/contents")) {
						return new MockResponse() //
								.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE) //
								.setBody(contentResponse) //
								.setResponseCode(200);
					}
					return new MockResponse() //
							.setResponseCode(404);
				}
			});
			Optional<Entry> entry = this.entryFetcher.fetch(tenantId, owner, repo, path);
			assertThat(entry).isNotEmpty();
			Entry e = entry.get();
			assertThat(e).isNotNull();
			assertThat(e.getEntryId()).isEqualTo(1L);
			assertThat(e.getContent()).isEqualTo("This is my first blog post!");
			assertThat(e.getCreated()).isNotNull();
			assertThat(e.getCreated().getName()).isEqualTo("Toshiaki Maki");
			assertThat(e.getCreated().getDate())
					.isEqualTo(OffsetDateTime.parse("2015-12-28T17:16:23Z"));
			assertThat(e.getUpdated()).isNotNull();
			assertThat(e.getUpdated().getName()).isEqualTo("Toshiaki Maki");
			assertThat(e.getUpdated().getDate())
					.isEqualTo(OffsetDateTime.parse("2018-01-14T08:09:06Z"));
			FrontMatter frontMatter = e.getFrontMatter();
			assertThat(frontMatter).isNotNull();
			assertThat(frontMatter.getTitle()).isEqualTo("First article");
			assertThat(frontMatter.getCategories()).containsExactly(new Category("Demo"),
					new Category("Hello"));
			assertThat(frontMatter.getTags()).containsExactly(new Tag("Demo"));
		}
	}
}
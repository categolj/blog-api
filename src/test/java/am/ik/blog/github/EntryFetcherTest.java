package am.ik.blog.github;

import am.ik.blog.category.Category;
import am.ik.blog.config.GitHubConfig;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.FrontMatter;
import am.ik.blog.tag.Tag;
import am.ik.spring.logbook.AccessLoggerLogbookAutoConfiguration;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javassist.bytecode.analysis.Executor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.zalando.logbook.autoconfigure.LogbookAutoConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatReflectiveOperationException;

@SpringBootTest(
		properties = { "blog.github.retry-interval=5ms", "blog.github.retry-max-elapsed-time=40ms",
				"blog.github.tenants.xyz.access-token=foo", "blog.github.api-url=http://localhost:29998" },
		webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class EntryFetcherTest {

	@Autowired
	EntryFetcher entryFetcher;

	String path = "content/00001.md";

	@ParameterizedTest
	@CsvSource({ ",someone,my-blog", "demo,someone,my-blog", "xyz,foo,bar" })
	void fetch(String tenantId, String owner, String repo) throws Exception {
		final String basePath = "/repos/" + owner + "/" + repo;
		HttpServer httpServer = mockServer(29998, basePath);
		try {
			Optional<Entry> entry = this.entryFetcher.fetch(tenantId, owner, repo, path);
			assertThat(entry).isNotEmpty();
			Entry e = entry.get();
			assertThat(e).isNotNull();
			assertThat(e.getEntryId()).isEqualTo(1L);
			assertThat(e.getContent()).isEqualTo("This is my first blog post!");
			assertThat(e.getCreated()).isNotNull();
			assertThat(e.getCreated().name()).isEqualTo("Toshiaki Maki");
			assertThat(e.getCreated().date()).isEqualTo(OffsetDateTime.parse("2015-12-28T17:16:23Z"));
			assertThat(e.getUpdated()).isNotNull();
			assertThat(e.getUpdated().name()).isEqualTo("Toshiaki Maki");
			assertThat(e.getUpdated().date()).isEqualTo(OffsetDateTime.parse("2018-01-14T08:09:06Z"));
			FrontMatter frontMatter = e.getFrontMatter();
			assertThat(frontMatter).isNotNull();
			assertThat(frontMatter.title()).isEqualTo("First article");
			assertThat(frontMatter.categories()).containsExactly(new Category("Demo"), new Category("Hello"));
			assertThat(frontMatter.tags()).containsExactly(new Tag("Demo"));
		}
		finally {
			httpServer.stop(0);
		}
	}

	HttpServer mockServer(int port, String basePath) throws Exception {
		HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
		httpServer.setExecutor(Executors.newSingleThreadExecutor());
		httpServer.createContext(basePath + "/contents", exchange -> {
			try (final OutputStream stream = new BufferedOutputStream(exchange.getResponseBody())) {
				byte[] body = new ClassPathResource("github/sample-content-response.json").getContentAsByteArray();
				stream.write(body);
				exchange.getResponseHeaders().set("Content-Type", "application/json");
				exchange.sendResponseHeaders(200, body.length);
				stream.flush();
			}
		});
		httpServer.createContext(basePath + "/commits", exchange -> {
			try (final OutputStream stream = new BufferedOutputStream(exchange.getResponseBody())) {
				byte[] body = new ClassPathResource("github/sample-commits-response.json").getContentAsByteArray();
				stream.write(body);
				exchange.getResponseHeaders().set("Content-Type", "application/json");
				exchange.sendResponseHeaders(200, body.length);
				stream.flush();
			}
		});
		httpServer.start();
		return httpServer;
	}

}
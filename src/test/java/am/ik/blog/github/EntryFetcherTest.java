package am.ik.blog.github;

import am.ik.blog.category.Category;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.FrontMatter;
import am.ik.blog.tag.Tag;
import am.ik.github.AccessToken;
import am.ik.github.GitHubClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class EntryFetcherTest {

    MockWebServer server = new MockWebServer();

    EntryFetcher entryFetcher;

    int port;

    GitHubClient gitHubClient;

    String owner = "someone";

    String repo = "my-blog";

    String path = "content/00001.md";

    String basePath = "/repos/" + owner + "/" + repo;

    @BeforeEach
    void setup() throws Exception {
        this.port = new Random().nextInt(60000, 65535);
        this.server.start(this.port);
        this.gitHubClient = new GitHubClient("http://localhost:" + port,
            WebClient.builder(), new AccessToken("dummy"));
        this.entryFetcher = new EntryFetcher(this.gitHubClient);
    }

    @AfterEach
    void shutdown() throws Exception {
        server.shutdown();
    }

    @Test
    void fetch() throws Exception {
        Buffer contentResponse = new Buffer().readFrom(
            new ClassPathResource("github/sample-content-response.json").getInputStream());
        Buffer commitsResponse = new Buffer().readFrom(
            new ClassPathResource("github/sample-commits-response.json").getInputStream());

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
        Mono<Entry> entry = this.entryFetcher.fetch(owner, repo, path);
        StepVerifier.create(entry) //
            .assertNext(e -> {
                assertThat(e).isNotNull();
                assertThat(e.getEntryId()).isEqualTo(1L);
                assertThat(e.getContent())
                    .isEqualTo("This is my first blog post!");
                assertThat(e.getCreated()).isNotNull();
                assertThat(e.getCreated().getName())
                    .isEqualTo("Toshiaki Maki");
                assertThat(e.getCreated().getDate()).isEqualTo(OffsetDateTime.parse("2015-12-28T17:16:23Z"));
                assertThat(e.getUpdated()).isNotNull();
                assertThat(e.getUpdated().getName())
                    .isEqualTo("Toshiaki Maki");
                assertThat(e.getUpdated().getDate()).isEqualTo(OffsetDateTime.parse("2018-01-14T08:09:06Z"));
                FrontMatter frontMatter = e.getFrontMatter();
                assertThat(frontMatter).isNotNull();
                assertThat(frontMatter.getTitle()).isEqualTo("First article");
                assertThat(frontMatter.getCategories()).containsExactly(
                    Category.of("Demo"), Category.of("Hello"));
                assertThat(frontMatter.getTags()).containsExactly(Tag.of("Demo"));
            }) //
            .verifyComplete();
        ;
    }
}
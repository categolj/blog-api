package am.ik.blog.github;

import java.time.OffsetDateTime;

import am.ik.blog.entry.*;
import am.ik.github.AccessToken;
import am.ik.github.GitHubClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.SocketUtils;
import org.springframework.web.reactive.function.client.WebClient;

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

    @Before
    public void setup() throws Exception {
        this.port = SocketUtils.findAvailableTcpPort();
        this.server.start(this.port);
        this.gitHubClient = new GitHubClient("http://localhost:" + port,
                WebClient.builder(), new AccessToken("dummy"));
        this.entryFetcher = new EntryFetcher(this.gitHubClient);
    }

    @After
    public void shutdown() throws Exception {
        server.shutdown();
    }

    @Test
    public void fetch() throws Exception {
        Buffer contentResponse = new Buffer().readFrom(
                new ClassPathResource("sample-content-response.json").getInputStream());
        Buffer commitsResponse = new Buffer().readFrom(
                new ClassPathResource("sample-commits-response.json").getInputStream());

        this.server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request)
                    throws InterruptedException {
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
                    assertThat(e.getEntryId()).isEqualTo(new EntryId(1L));
                    assertThat(e.getContent())
                            .isEqualTo(new Content("This is my first blog post!"));
                    assertThat(e.getCreated()).isNotNull();
                    assertThat(e.getCreated().getName())
                            .isEqualTo(new Name("Toshiaki Maki"));
                    assertThat(e.getCreated().getDate()).isEqualTo(
                            new EventTime(OffsetDateTime.parse("2015-12-28T17:16:23Z")));
                    assertThat(e.getUpdated()).isNotNull();
                    assertThat(e.getUpdated().getName())
                            .isEqualTo(new Name("Toshiaki Maki"));
                    assertThat(e.getUpdated().getDate()).isEqualTo(
                            new EventTime(OffsetDateTime.parse("2018-01-14T08:09:06Z")));
                    FrontMatter frontMatter = e.getFrontMatter();
                    assertThat(frontMatter).isNotNull();
                    assertThat(frontMatter.title()).isEqualTo(new Title("First article"));
                    assertThat(frontMatter.categories()).isEqualTo(
                            new Categories(new Category("Demo"), new Category("Hello")));
                    assertThat(frontMatter.tags()).isEqualTo(new Tags(new Tag("Demo")));
                }) //
                .verifyComplete();
        ;
    }
}
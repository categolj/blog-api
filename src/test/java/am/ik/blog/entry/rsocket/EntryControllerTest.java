package am.ik.blog.entry.rsocket;

import am.ik.blog.entry.Entry;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.TestConstructor;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static am.ik.blog.entry.rsocket.Asserts.assertEntry99997;
import static am.ik.blog.entry.rsocket.Asserts.assertEntry99998;
import static am.ik.blog.entry.rsocket.Asserts.assertEntry99999;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.zipkin.enabled=false")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class EntryControllerTest {

    private final RSocketRequester requester;

    private final DatabaseClient databaseClient;

    EntryControllerTest(RSocketRequester.Builder builder, @LocalServerPort int port, DatabaseClient databaseClient) {
        this.requester = builder.websocket(URI.create(String.format("ws://localhost:%d/rsocket", port)));
        this.databaseClient = databaseClient;
    }

    @BeforeEach
    public void reset() {
        this.databaseClient.sql(readFile("sql/delete-test-data.sql")).then().block();
        this.databaseClient.sql(readFile("sql/insert-test-data.sql")).then().block();
    }

    @Test
    void responseEntry() {
        final Mono<Entry> entryMono = this.requester.route("entries.99999")
            .retrieveMono(Entry.class);
        StepVerifier.create(entryMono)
            .consumeNextWith(entry -> assertEntry99999(entry).assertContent())
            .expectComplete()
            .verify();
    }

    @Test
    void responsePage() {
        final Mono<EntryPage> pageMono = this.requester.route("entries")
            .data(Map.of())
            .retrieveMono(EntryPage.class);

        StepVerifier.create(pageMono.flatMapIterable(EntryPage::getContent))
            .consumeNextWith(entry -> assertEntry99999(entry).assertThatContentIsNotSet())
            .consumeNextWith(entry -> assertEntry99998(entry).assertThatContentIsNotSet())
            .consumeNextWith(entry -> assertEntry99997(entry).assertThatContentIsNotSet())
            .expectComplete()
            .verify();
    }

    @Test
    void searchByKeyword() {
        final Mono<EntryPage> pageMono = this.requester.route("entries")
            .data(Map.of("query", "This"))
            .retrieveMono(EntryPage.class);

        StepVerifier.create(pageMono.flatMapIterable(EntryPage::getContent))
            .consumeNextWith(entry -> assertEntry99998(entry).assertThatContentIsNotSet())
            .consumeNextWith(entry -> assertEntry99997(entry).assertThatContentIsNotSet())
            .expectComplete()
            .verify();
    }

    @Test
    void searchByTag() {
        final Mono<EntryPage> pageMono = this.requester.route("entries")
            .data(Map.of("tag", "test2"))
            .retrieveMono(EntryPage.class);

        StepVerifier.create(pageMono.flatMapIterable(EntryPage::getContent))
            .consumeNextWith(entry -> assertEntry99999(entry).assertThatContentIsNotSet())
            .consumeNextWith(entry -> assertEntry99998(entry).assertThatContentIsNotSet())
            .expectComplete()
            .verify();
    }

    @Test
    void searchByCategories() {
        final Mono<EntryPage> pageMono = this.requester.route("entries")
            .data(Map.of("categories", List.of("x", "y")))
            .retrieveMono(EntryPage.class);

        StepVerifier.create(pageMono.flatMapIterable(EntryPage::getContent))
            .consumeNextWith(entry -> assertEntry99999(entry).assertThatContentIsNotSet())
            .consumeNextWith(entry -> assertEntry99997(entry).assertThatContentIsNotSet())
            .expectComplete()
            .verify();
    }

    static String readFile(String file) {
        try (final InputStream inputStream = new ClassPathResource(file).getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class EntryPage {

        private List<Entry> content;

        private int number;

        private int size;

        private long totalElements;

        public List<Entry> getContent() {
            return content;
        }

        public void setContent(List<Entry> content) {
            this.content = content;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", EntryPage.class.getSimpleName() + "[", "]")
                .add("number=" + number)
                .add("size=" + size)
                .add("totalElements=" + totalElements)
                .toString();
        }
    }
}
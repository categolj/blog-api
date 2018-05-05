package am.ik.blog.entry;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.net.ServerSocketFactory;

import am.ik.blog.exception.SimpleExceptionMessage;
import am.ik.blog.rsocket.RSocketCodec;
import com.fasterxml.jackson.databind.JsonNode;
import io.netty.buffer.PooledByteBufAllocator;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.exceptions.ApplicationErrorException;
import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static am.ik.blog.entry.Asserts.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
		"rsocket.server.codec=smile", "rsocket.server.port=47000" })
@Sql({ "classpath:/delete-test-data.sql", "classpath:/insert-test-data.sql" })
public class RSocketIntegrationTest {
	@Value("${rsocket.server.port}")
	int port;
	private RSocketCodec codec = RSocketCodec.SMILE;
	private DataBufferFactory dataBufferFactory = new NettyDataBufferFactory(
			PooledByteBufAllocator.DEFAULT);
	private RSocket rsocket;

	@Before
	public void setup() {
		ClientTransport transport = TcpClientTransport.create("localhost", port);
		for (int i = 0; i < 10; i++) {
			if (isPortAvailable(port)) {
				break;
			}
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		this.rsocket = RSocketFactory.connect().transport(transport).start().block();
	}

	@Test
	public void routeNotFoundForRequestResponse() throws Exception {
		Mono<String> response = this.rsocket
				.requestResponse(DefaultPayload.create("", "/foo")) //
				.onErrorResume(ApplicationErrorException.class,
						e -> Mono.just(DefaultPayload.create(e.getMessage())))
				.map(Payload::getDataUtf8);
		StepVerifier.create(response) //
				.assertNext(message -> assertThat(message).isEqualTo(
						"Response status 404 with reason \"No route matches to /foo\"")) //
				.verifyComplete();
	}

	@Test
	public void routeNotFoundForRequestStream() throws Exception {
		Flux<String> response = this.rsocket
				.requestStream(DefaultPayload.create("", "/foo")) //
				.onErrorResume(ApplicationErrorException.class,
						e -> Mono.just(DefaultPayload.create(e.getMessage())))
				.map(Payload::getDataUtf8);
		StepVerifier.create(response) //
				.assertNext(message -> assertThat(message).isEqualTo(
						"Response status 404 with reason \"No route matches to /foo\"")) //
				.verifyComplete();
	}

	@Test
	public void getEntry() throws Exception {
		Mono<Entry> response = this.rsocket
				.requestResponse(DefaultPayload.create("", "/entries/99999"))
				.transform(this::toEntryMono);
		StepVerifier.create(response) //
				.assertNext(entry -> assertEntry99999(entry).assertContent()) //
				.verifyComplete();
	}

	@Test
	public void getEntryNotFound() throws Exception {
		Mono<String> response = this.rsocket
				.requestResponse(DefaultPayload.create("", "/entries/1")) //
				.onErrorResume(ApplicationErrorException.class,
						e -> Mono.just(DefaultPayload.create(e.getMessage())))
				.map(Payload::getDataUtf8);
		StepVerifier.create(response) //
				.assertNext(message -> assertThat(message).isEqualTo(
						"Response status 404 with reason \"entry 1 is not found.\"")) //
				.verifyComplete();
	}

	@Test
	public void getEntriesExcludeContent() throws Exception {
		Flux<Entry> response = this.rsocket
				.requestStream(DefaultPayload.create("", "/entries"))
				.transform(this::toEntryFlux);
		StepVerifier.create(response) //
				.assertNext(entry -> assertEntry99999(entry).assertThatContentIsNotSet()) //
				.assertNext(entry -> assertEntry99998(entry).assertThatContentIsNotSet()) //
				.assertNext(entry -> assertEntry99997(entry).assertThatContentIsNotSet()) //
				.verifyComplete();
	}

	@Test
	public void getEntriesIncludeContent() throws Exception {
		Flux<Entry> response = this.rsocket
				.requestStream(DefaultPayload.create("excludeContent=false", "/entries"))
				.transform(this::toEntryFlux);
		StepVerifier.create(response) //
				.assertNext(entry -> assertEntry99999(entry).assertContent()) //
				.assertNext(entry -> assertEntry99998(entry).assertContent()) //
				.assertNext(entry -> assertEntry99997(entry).assertContent()) //
				.verifyComplete();
	}

	@Test
	public void getEntriesSetSize() throws Exception {
		Flux<Entry> response = this.rsocket
				.requestStream(DefaultPayload.create("size=1", "/entries"))
				.transform(this::toEntryFlux);
		StepVerifier.create(response) //
				.assertNext(entry -> assertEntry99999(entry).assertThatContentIsNotSet()) //
				.verifyComplete();
	}

	@Test
	public void getEntriesSetPage() throws Exception {
		Flux<Entry> response = this.rsocket
				.requestStream(DefaultPayload.create("size=1&page=1", "/entries"))
				.transform(this::toEntryFlux);
		StepVerifier.create(response) //
				.assertNext(entry -> assertEntry99998(entry).assertThatContentIsNotSet()) //
				.verifyComplete();
	}

	@Test
	public void searchForEmpty() throws Exception {
		Flux<Entry> response = this.rsocket
				.requestStream(DefaultPayload.create("q=Empty", "/entries"))
				.transform(this::toEntryFlux);
		StepVerifier.create(response) //
				.verifyComplete();
	}

	@Test
	public void getEntriesByTag() throws Exception {
		Flux<Entry> response = this.rsocket
				.requestStream(DefaultPayload.create("", "/tags/test2/entries"))
				.transform(this::toEntryFlux);
		StepVerifier.create(response) //
				.assertNext(entry -> assertEntry99999(entry).assertThatContentIsNotSet()) //
				.assertNext(entry -> assertEntry99998(entry).assertThatContentIsNotSet()) //
				.verifyComplete();
	}

	@Test
	public void getEntriesByCategories() throws Exception {
		Flux<Entry> response = this.rsocket
				.requestStream(DefaultPayload.create("", "/categories/x,y/entries"))
				.transform(this::toEntryFlux);
		StepVerifier.create(response) //
				.assertNext(entry -> assertEntry99999(entry).assertThatContentIsNotSet()) //
				.assertNext(entry -> assertEntry99997(entry).assertThatContentIsNotSet()) //
				.verifyComplete();
	}

	@Test
	public void getTags() throws Exception {
		Mono<List<Tag>> response = this.rsocket
				.requestResponse(DefaultPayload.create("", "/tags"))
				.transform(this::toTagsMono);
		StepVerifier.create(response) //
				.assertNext(x -> assertThat(x).containsExactly(new Tag("test1"),
						new Tag("test2"), new Tag("test3"))) //
				.verifyComplete();
	}

	@Test
	public void getCategories() throws Exception {
		Flux<Categories> response = this.rsocket
				.requestResponse(DefaultPayload.create("", "/categories"))
				.transform(this::toCategoriesMono) //
				.flatMapMany(Flux::fromIterable);
		StepVerifier.create(response) //
				.assertNext(x -> assertThat(x).isEqualTo(new Categories(new Category("a"),
						new Category("b"), new Category("c")))) //
				.assertNext(x -> assertThat(x)
						.isEqualTo(new Categories(new Category("x"), new Category("y")))) //
				.assertNext(x -> assertThat(x).isEqualTo(new Categories(new Category("x"),
						new Category("y"), new Category("z")))) //
				.verifyComplete();
	}

	private Flux<Entry> toEntryFlux(Publisher<Payload> payload) {
		return this.toFlux(payload, ResolvableType.forType(Entry.class))
				.cast(Entry.class);
	}

	private Mono<Entry> toEntryMono(Publisher<Payload> payload) {
		return this.toMono(payload, ResolvableType.forType(Entry.class))
				.cast(Entry.class);
	}

	private Mono<SimpleExceptionMessage> toSimpleExceptionMessageMono(
			Publisher<Payload> payload) {
		return this.toMono(payload, ResolvableType.forType(SimpleExceptionMessage.class))
				.cast(SimpleExceptionMessage.class);
	}

	private Mono<List<Tag>> toTagsMono(Publisher<Payload> payload) {
		return this.toFlux(payload, ResolvableType.forType(Tag.class)) //
				.cast(Tag.class) //
				.collectList();
	}

	private Mono<List<Categories>> toCategoriesMono(Publisher<Payload> payload) {
		return this.toFlux(payload, ResolvableType.forType(JsonNode.class))
				.cast(JsonNode.class) //
				.map(n -> new Categories( // TODO deserializer
						StreamSupport.stream(n.get("categories").spliterator(), false)
								.map(JsonNode::asText).map(Category::new)
								.collect(Collectors.toList())))
				.collectList();
	}

	private Flux<Object> toFlux(Publisher<Payload> payload, ResolvableType type) {
		return this.codec.decoder()
				.decode(Flux.from(payload).map(Payload::getData)
						.map(x -> dataBufferFactory.wrap(x)), type,
						this.codec.streamMediaType(), Collections.emptyMap());
	}

	private Mono<Object> toMono(Publisher<Payload> payload, ResolvableType type) {
		return this.codec.decoder()
				.decode(Flux.from(payload).map(Payload::getData)
						.map(x -> dataBufferFactory.wrap(x)), type,
						this.codec.singleMediaType(), Collections.emptyMap()) //
				.next();
	}

	private boolean isPortAvailable(int port) {
		try {
			ServerSocket serverSocket = ServerSocketFactory.getDefault()
					.createServerSocket(port, 1, InetAddress.getByName("localhost"));
			serverSocket.close();
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}
}
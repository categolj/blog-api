package am.ik.blog.github;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryId;
import am.ik.blog.entry.EntryMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Component
public class WebhookHandler {
	private final EntryFetcher entryFetcher;
	private final EntryMapper entryMapper;
	private final WebhookVerifier webhookVerifier;
	private final ObjectMapper objectMapper;
	private final ParameterizedTypeReference<Map<String, Long>> typeReference = new ParameterizedTypeReference<Map<String, Long>>() {
	};

	public WebhookHandler(GitHubProps props, EntryFetcher entryFetcher, EntryMapper entryMapper,
			ObjectMapper objectMapper) throws NoSuchAlgorithmException, InvalidKeyException {
		this.entryFetcher = entryFetcher;
		this.entryMapper = entryMapper;
		this.objectMapper = objectMapper;
		this.webhookVerifier = new WebhookVerifier(props.getWebhookSecret());
	}

	public RouterFunction<ServerResponse> routes() {
		return route() //
				.POST("/webhook", this::webhook) //
				.build();
	}

	public Mono<ServerResponse> webhook(ServerRequest request) {
		List<String> xHubSignature = request.headers().header("X-Hub-Signature");
		if (xHubSignature.isEmpty()) {
			return ServerResponse.status(HttpStatus.BAD_REQUEST)
					.bodyValue(Collections.singletonMap("message", "X-Hub-Signature is empty!"));
		}
		String signature = xHubSignature.get(0);
		return request.bodyToMono(String.class) //
				.flatMap(payload -> {
					this.webhookVerifier.verify(payload, signature);
					JsonNode node = this.node(payload);
					String[] repository = node.get("repository").get("full_name").asText().split("/");
					String owner = repository[0];
					String repo = repository[1];
					if (!node.has("commits")) {
						return ServerResponse.status(HttpStatus.BAD_REQUEST)
								.bodyValue(Collections.singletonMap("message", "commit is empty!"));
					}
					Stream<JsonNode> commits = StreamSupport.stream(node.get("commits").spliterator(), false);
					Flux<Map<String, Long>> response = Flux.fromStream(commits) //
							.flatMap(commit -> {
								Flux<EntryId> added = this.paths(commit.get("added"))
										.flatMap(path -> this.entryFetcher.fetch(owner, repo, path)) //
										.flatMap(entryMapper::save) //
										.map(Entry::entryId) //
										.log("added");
								Flux<EntryId> modified = this.paths(commit.get("modified")) //
										.flatMap(path -> this.entryFetcher.fetch(owner, repo, path)) //
										.flatMap(this.entryMapper::save) //
										.map(Entry::entryId) //
										.log("modified");
								Flux<EntryId> removed = this.paths(commit.get("removed")) //
										.map(path -> EntryId.fromFilePath(Paths.get(path))) //
										.flatMap(this.entryMapper::delete) //
										.log("removed");
								return added.map(id -> Collections.singletonMap("added", id.getValue())) //
										.mergeWith(
												modified.map(id -> Collections.singletonMap("modified", id.getValue()))) //
										.mergeWith(
												removed.map(id -> Collections.singletonMap("removed", id.getValue())))
										.log("merged");
							});
					return ServerResponse.ok().body(response, typeReference);
				});
	}

	JsonNode node(String payload) {
		try {
			return this.objectMapper.readValue(payload, JsonNode.class);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	Flux<String> paths(JsonNode paths) {
		return Flux.fromStream(StreamSupport.stream(paths.spliterator(), false).map(JsonNode::asText));
	}
}

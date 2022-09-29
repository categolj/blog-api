package am.ik.blog.github.web;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryMapper;
import am.ik.blog.github.EntryFetcher;
import am.ik.blog.github.GitHubProps;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class WebhookController {
	private final EntryFetcher entryFetcher;

	private final EntryMapper entryMapper;

	private final WebhookVerifier webhookVerifier;

	private final ObjectMapper objectMapper;

	public WebhookController(GitHubProps props, EntryFetcher entryFetcher, EntryMapper entryMapper, ObjectMapper objectMapper) throws NoSuchAlgorithmException, InvalidKeyException {
		this.entryFetcher = entryFetcher;
		this.entryMapper = entryMapper;
		this.webhookVerifier = new WebhookVerifier(props.getWebhookSecret());
		this.objectMapper = objectMapper;
	}

	@PostMapping(path = "webhook")
	public Flux<Map<String, Long>> webhook(@RequestHeader(name = "X-Hub-Signature") String signature, @RequestBody String payload) {
		this.webhookVerifier.verify(payload, signature);
		final JsonNode node = this.node(payload);
		final String[] repository = node.get("repository").get("full_name").asText().split("/");
		final String owner = repository[0];
		final String repo = repository[1];
		if (!node.has("commits")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "commit is empty!");
		}
		final Stream<JsonNode> commits = StreamSupport.stream(node.get("commits").spliterator(), false);
		return Flux.fromStream(commits) //
				.flatMap(commit -> {
					Flux<Long> added = this.paths(commit.get("added"))
							.flatMap(path -> this.entryFetcher.fetch(owner, repo, path)) //
							.flatMap(entryMapper::save) //
							.map(Entry::getEntryId)
							.log("added");
					Flux<Long> modified = this.paths(commit.get("modified")) //
							.flatMap(path -> this.entryFetcher.fetch(owner, repo, path)) //
							.flatMap(this.entryMapper::save) //
							.map(Entry::getEntryId)
							.log("modified");
					Flux<Long> removed = this.paths(commit.get("removed")) //
							.map(path -> Entry.parseId(Paths.get(path).getFileName().toString())) //
							.flatMap(this.entryMapper::delete) //
							.log("removed");
					return added.map(id -> Collections.singletonMap("added", id)) //
							.mergeWith(
									modified.map(id -> Collections.singletonMap("modified", id))) //
							.mergeWith(
									removed.map(id -> Collections.singletonMap("removed", id)))
							.log("merged");
				});

	}

	JsonNode node(String payload) {
		try {
			return this.objectMapper.readValue(payload, JsonNode.class);
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	Flux<String> paths(JsonNode paths) {
		return Flux.fromStream(StreamSupport.stream(paths.spliterator(), false).map(JsonNode::asText));
	}
}

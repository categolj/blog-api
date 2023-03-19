package am.ik.blog.github.web;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryService;
import am.ik.blog.github.EntryFetcher;
import am.ik.blog.github.GitHubProps;
import am.ik.webhook.WebhookVerifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static am.ik.webhook.WebhookHttpHeaders.X_HUB_SIGNATURE;

@RestController
@Tag(name = "webhook")
public class WebhookController {
	private final EntryFetcher entryFetcher;

	private final EntryService entryService;

	private final WebhookVerifier webhookVerifier;

	private final Map<String, WebhookVerifier> tenantsWebhookVerifier;

	private final ObjectMapper objectMapper;

	public WebhookController(GitHubProps props, EntryFetcher entryFetcher,
			EntryService entryService, ObjectMapper objectMapper) {
		this.entryFetcher = entryFetcher;
		this.entryService = entryService;
		this.webhookVerifier = WebhookVerifier.gitHubSha1(props.getWebhookSecret());
		this.tenantsWebhookVerifier = props.getTenants().entrySet().stream().collect(
				Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> WebhookVerifier
						.gitHubSha1(e.getValue().getWebhookSecret())));
		this.objectMapper = objectMapper;
	}

	@PostMapping(path = "webhook")
	public List<Map<String, Long>> webhook(
			@RequestHeader(name = X_HUB_SIGNATURE) String signature,
			@RequestBody String payload) {
		return this.webhookForTenant(signature, payload, null);
	}

	@PostMapping(path = "tenants/{tenantId}/webhook")
	public List<Map<String, Long>> webhookForTenant(
			@RequestHeader(name = X_HUB_SIGNATURE) String signature,
			@RequestBody String payload,
			@PathVariable(name = "tenantId", required = false) String tenantId) {
		if (tenantId == null) {
			this.webhookVerifier.verify(payload, signature);
		}
		else {
			final WebhookVerifier verifier = this.tenantsWebhookVerifier
					.getOrDefault(tenantId, this.webhookVerifier);
			verifier.verify(payload, signature);
		}
		final JsonNode node = this.node(payload);
		final String[] repository = node.get("repository").get("full_name").asText()
				.split("/");
		final String owner = repository[0];
		final String repo = repository[1];
		if (!node.has("commits")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "commit is empty!");
		}
		final Stream<JsonNode> commits = StreamSupport
				.stream(node.get("commits").spliterator(), false);
		final List<Map<String, Long>> result = new ArrayList<>();
		commits.forEach(commit -> {
			Stream.of("added", "modified").forEach(key -> {
				this.paths(commit.get(key))
						.forEach(path -> this.entryFetcher
								.fetch(tenantId, owner, repo, path)
								.doOnNext(e -> result.add(Map.of(key, e.getEntryId())))
								// blocking intentionally so that trace id is properly
								// propagated
								.blockOptional()
								.ifPresent(entry -> entryService.save(entry, tenantId)));
			});
			this.paths(commit.get("removed"))
					.forEach(path -> this.entryFetcher.fetch(tenantId, owner, repo, path)
							.map(Entry::getEntryId)
							.doOnNext(id -> result.add(Map.of("removed", id)))
							// blocking intentionally so that trace id is properly
							// propagated
							.blockOptional().ifPresent(
									entryId -> entryService.delete(entryId, tenantId)));
		});
		return result;
	}

	JsonNode node(String payload) {
		try {
			return this.objectMapper.readValue(payload, JsonNode.class);
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	Stream<String> paths(JsonNode paths) {
		return StreamSupport.stream(paths.spliterator(), false).map(JsonNode::asText);
	}
}

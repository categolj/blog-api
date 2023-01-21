package am.ik.blog.github.web;

import java.util.Map;

import am.ik.blog.github.EntryFetcher;
import am.ik.blog.github.Fixtures;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"blog.github.access-token=foo", "blog.github.webhook-secret=bar",
		"blog.github.tenants.xyz.webhook-secret=abc" })
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Testcontainers(disabledWithoutDocker = true)
public class WebhookControllerTest {

	private final ObjectMapper objectMapper;

	private final WebTestClient webClient;

	@MockBean
	EntryFetcher entryFetcher;

	@MockBean
	EntryMapper entryRepository;

	public WebhookControllerTest(ObjectMapper objectMapper,
			@Value("${local.server.port}") int port) {
		this.objectMapper = objectMapper;
		this.webClient = WebTestClient.bindToServer(new JdkClientHttpConnector())
				.baseUrl("http://localhost:" + port).build();
	}

	@ParameterizedTest
	@CsvSource({ ",bar", "demo,bar", "xyz,abc" })
	void webhookAdded(String tenantId, String secret) throws Exception {
		Entry entry = Fixtures.entry(100L);
		given(entryFetcher.fetch(tenantId, "example", "blog.example.com",
				"content/00100.md")).willReturn(Mono.just(entry));
		given(entryRepository.save(entry, tenantId)).willReturn(Map.of());

		ObjectNode body = this.objectMapper.createObjectNode();
		body.putObject("repository").put("full_name", "example/blog.example.com");
		ArrayNode commits = body.putArray("commits");
		ObjectNode commit = commits.addObject();
		commit.putArray("added").add("content/00100.md");
		commit.putArray("modified");
		commit.putArray("removed");

		WebhookVerifier verifier = new WebhookVerifier(secret);
		this.webClient.post() //
				.uri("%s/webhook"
						.formatted(tenantId == null ? "" : "/tenants/" + tenantId)) //
				.bodyValue(body) //
				.accept(MediaType.APPLICATION_JSON) //
				.header("X-Hub-Signature", verifier.signature(body.toString())) //
				.exchange() //
				.expectStatus() //
				.isOk() //
				.expectBody() //
				.jsonPath("$").isArray() //
				.jsonPath("$.length()").isEqualTo(1) //
				.jsonPath("$[0].added").isEqualTo(100);
	}

	@ParameterizedTest
	@CsvSource({ ",bar", "demo,bar", "xyz,abc" })
	void webhookModified(String tenantId, String secret) throws Exception {
		Entry entry = Fixtures.entry(100L);
		given(entryFetcher.fetch(tenantId, "example", "blog.example.com",
				"content/00100.md")).willReturn(Mono.just(entry));
		given(entryRepository.save(entry, tenantId)).willReturn(Map.of());

		ObjectNode body = this.objectMapper.createObjectNode();
		body.putObject("repository").put("full_name", "example/blog.example.com");
		ArrayNode commits = body.putArray("commits");
		ObjectNode commit = commits.addObject();
		commit.putArray("added");
		commit.putArray("modified").add("content/00100.md");
		commit.putArray("removed");

		WebhookVerifier verifier = new WebhookVerifier(secret);
		this.webClient.post() //
				.uri("%s/webhook"
						.formatted(tenantId == null ? "" : "/tenants/" + tenantId)) //
				.bodyValue(body) //
				.accept(MediaType.APPLICATION_JSON) //
				.header("X-Hub-Signature", verifier.signature(body.toString())) //
				.exchange() //
				.expectStatus() //
				.isOk() //
				.expectBody() //
				.jsonPath("$").isArray() //
				.jsonPath("$.length()").isEqualTo(1) //
				.jsonPath("$[0].modified").isEqualTo(100);
	}

	@ParameterizedTest
	@CsvSource({ ",bar", "demo,bar", "xyz,abc" })
	void webhookRemoved(String tenantId, String secret) throws Exception {
		Entry entry = Fixtures.entry(100L);
		given(entryFetcher.fetch(tenantId, "example", "blog.example.com",
				"content/00100.md")).willReturn(Mono.just(entry));
		Long entryId = entry.getEntryId();
		given(entryRepository.delete(entryId, tenantId)).willReturn(1);

		ObjectNode body = this.objectMapper.createObjectNode();
		body.putObject("repository").put("full_name", "example/blog.example.com");
		ArrayNode commits = body.putArray("commits");
		ObjectNode commit = commits.addObject();
		commit.putArray("added");
		commit.putArray("modified");
		commit.putArray("removed").add("content/00100.md");

		WebhookVerifier verifier = new WebhookVerifier(secret);
		this.webClient.post() //
				.uri("%s/webhook"
						.formatted(tenantId == null ? "" : "/tenants/" + tenantId)) //
				.bodyValue(body) //
				.accept(MediaType.APPLICATION_JSON) //
				.header("X-Hub-Signature", verifier.signature(body.toString())) //
				.exchange() //
				.expectStatus() //
				.isOk() //
				.expectBody() //
				.jsonPath("$").isArray() //
				.jsonPath("$.length()").isEqualTo(1) //
				.jsonPath("$[0].removed").isEqualTo(100);
	}

	@ParameterizedTest
	@CsvSource({ ",foo", "demo,foo", "xyz,foo" })
	void webhookForbidden(String tenantId, String secret) throws Exception {
		Entry entry = Fixtures.entry(100L);
		given(entryFetcher.fetch(tenantId, "example", "blog.example.com",
				"content/00100.md")).willReturn(Mono.just(entry));

		ObjectNode body = this.objectMapper.createObjectNode();
		body.putObject("repository").put("full_name", "example/blog.example.com");
		ArrayNode commits = body.putArray("commits");
		ObjectNode commit = commits.addObject();
		commit.putArray("added");
		commit.putArray("modified");
		commit.putArray("removed").add("content/00100.md");

		WebhookVerifier verifier = new WebhookVerifier(secret);
		this.webClient.post() //
				.uri("%s/webhook"
						.formatted(tenantId == null ? "" : "/tenants/" + tenantId)) //
				.bodyValue(body) //
				.accept(MediaType.APPLICATION_JSON) //
				.header("X-Hub-Signature", verifier.signature(body.toString())) //
				.exchange() //
				.expectStatus() //
				.isForbidden() //
				.expectBody()//
				.jsonPath("$.detail").isEqualTo(
						"Could not verify signature: 'sha1=b21805d592724f387d6e03be7b42c10a90ee109f'");
	}
}
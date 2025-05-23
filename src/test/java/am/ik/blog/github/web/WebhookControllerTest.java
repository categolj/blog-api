package am.ik.blog.github.web;

import java.util.Map;
import java.util.Optional;

import am.ik.blog.TestContainersConfig;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryMapper;
import am.ik.blog.github.EntryFetcher;
import am.ik.blog.github.Fixtures;
import am.ik.webhook.WebhookVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.spring.webflux.LogbookExchangeFilterFunction;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.reactive.server.WebTestClient;

import static am.ik.webhook.WebhookHttpHeaders.X_HUB_SIGNATURE_256;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "blog.github.access-token=foo", "blog.github.webhook-secret=bar",
				"blog.github.tenants.xyz.webhook-secret=abc" })
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Testcontainers(disabledWithoutDocker = true)
@Import(TestContainersConfig.class)
public class WebhookControllerTest {

	private final ObjectMapper objectMapper;

	private WebTestClient webClient;

	@MockitoBean
	EntryFetcher entryFetcher;

	@MockitoBean
	EntryMapper entryRepository;

	@Autowired
	Logbook logbook;

	int port;

	public WebhookControllerTest(ObjectMapper objectMapper, @Value("${local.server.port}") int port) {
		this.objectMapper = objectMapper;
		this.port = port;
	}

	@BeforeEach
	void setUp() {
		this.webClient = WebTestClient.bindToServer(new JdkClientHttpConnector())
			.baseUrl("http://localhost:" + this.port)
			.filter(new LogbookExchangeFilterFunction(this.logbook))
			.build();
	}

	@ParameterizedTest
	@CsvSource({ ",bar", "demo,bar", "xyz,abc" })
	void webhookAdded(String tenantId, String secret) throws Exception {
		Entry entry = Fixtures.entry(100L);
		given(entryFetcher.fetch(tenantId, "example", "blog.example.com", "content/00100.md"))
			.willReturn(Optional.of(entry));
		given(entryRepository.save(entry, tenantId)).willReturn(Map.of());

		ObjectNode body = this.objectMapper.createObjectNode();
		body.putObject("repository").put("full_name", "example/blog.example.com");
		ArrayNode commits = body.putArray("commits");
		ObjectNode commit = commits.addObject();
		commit.putArray("added").add("content/00100.md");
		commit.putArray("modified");
		commit.putArray("removed");

		WebhookVerifier verifier = WebhookVerifier.gitHubSha256(secret);
		this.webClient.post() //
			.uri("%s/webhook".formatted(tenantId == null ? "" : "/tenants/" + tenantId)) //
			.bodyValue(body) //
			.accept(MediaType.APPLICATION_JSON) //
			.header(X_HUB_SIGNATURE_256, verifier.sign(body.toString())) //
			.exchange() //
			.expectStatus() //
			.isOk() //
			.expectBody() //
			.jsonPath("$")
			.isArray() //
			.jsonPath("$.length()")
			.isEqualTo(1) //
			.jsonPath("$[0].added")
			.isEqualTo(100);
	}

	@ParameterizedTest
	@CsvSource({ ",bar", "demo,bar", "xyz,abc" })
	void webhookModified(String tenantId, String secret) throws Exception {
		Entry entry = Fixtures.entry(100L);
		given(entryFetcher.fetch(tenantId, "example", "blog.example.com", "content/00100.md"))
			.willReturn(Optional.of(entry));
		given(entryRepository.save(entry, tenantId)).willReturn(Map.of());

		ObjectNode body = this.objectMapper.createObjectNode();
		body.putObject("repository").put("full_name", "example/blog.example.com");
		ArrayNode commits = body.putArray("commits");
		ObjectNode commit = commits.addObject();
		commit.putArray("added");
		commit.putArray("modified").add("content/00100.md");
		commit.putArray("removed");

		WebhookVerifier verifier = WebhookVerifier.gitHubSha256(secret);
		this.webClient.post() //
			.uri("%s/webhook".formatted(tenantId == null ? "" : "/tenants/" + tenantId)) //
			.bodyValue(body) //
			.accept(MediaType.APPLICATION_JSON) //
			.header(X_HUB_SIGNATURE_256, verifier.sign(body.toString())) //
			.exchange() //
			.expectStatus() //
			.isOk() //
			.expectBody() //
			.jsonPath("$")
			.isArray() //
			.jsonPath("$.length()")
			.isEqualTo(1) //
			.jsonPath("$[0].modified")
			.isEqualTo(100);
	}

	@ParameterizedTest
	@CsvSource({ ",bar", "demo,bar", "xyz,abc" })
	void webhookRemoved(String tenantId, String secret) throws Exception {
		Entry entry = Fixtures.entry(100L);
		given(entryFetcher.fetch(tenantId, "example", "blog.example.com", "content/00100.md"))
			.willReturn(Optional.of(entry));
		Long entryId = entry.getEntryId();
		given(entryRepository.delete(entryId, tenantId)).willReturn(1);

		ObjectNode body = this.objectMapper.createObjectNode();
		body.putObject("repository").put("full_name", "example/blog.example.com");
		ArrayNode commits = body.putArray("commits");
		ObjectNode commit = commits.addObject();
		commit.putArray("added");
		commit.putArray("modified");
		commit.putArray("removed").add("content/00100.md");

		WebhookVerifier verifier = WebhookVerifier.gitHubSha256(secret);
		this.webClient.post() //
			.uri("%s/webhook".formatted(tenantId == null ? "" : "/tenants/" + tenantId)) //
			.bodyValue(body) //
			.accept(MediaType.APPLICATION_JSON) //
			.header(X_HUB_SIGNATURE_256, verifier.sign(body.toString())) //
			.exchange() //
			.expectStatus() //
			.isOk() //
			.expectBody() //
			.jsonPath("$")
			.isArray() //
			.jsonPath("$.length()")
			.isEqualTo(1) //
			.jsonPath("$[0].removed")
			.isEqualTo(100);
	}

	@ParameterizedTest
	@CsvSource({ ",foo", "demo,foo", "xyz,foo" })
	void webhookForbidden(String tenantId, String secret) throws Exception {
		Entry entry = Fixtures.entry(100L);
		given(entryFetcher.fetch(tenantId, "example", "blog.example.com", "content/00100.md"))
			.willReturn(Optional.of(entry));

		ObjectNode body = this.objectMapper.createObjectNode();
		body.putObject("repository").put("full_name", "example/blog.example.com");
		ArrayNode commits = body.putArray("commits");
		ObjectNode commit = commits.addObject();
		commit.putArray("added");
		commit.putArray("modified");
		commit.putArray("removed").add("content/00100.md");

		WebhookVerifier verifier = WebhookVerifier.gitHubSha256(secret);
		this.webClient.post() //
			.uri("%s/webhook".formatted(tenantId == null ? "" : "/tenants/" + tenantId)) //
			.bodyValue(body) //
			.accept(MediaType.APPLICATION_JSON) //
			.header(X_HUB_SIGNATURE_256, verifier.sign(body.toString())) //
			.exchange() //
			.expectStatus() //
			.isForbidden() //
			.expectBody()//
			.jsonPath("$.detail")
			.isEqualTo(
					"Could not verify signature: 'sha256=0befdaf80b8ee4dd1863d8e8ee665a7400e7c69d761d0fa2086e747a2b877503'");
	}

}
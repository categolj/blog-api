package am.ik.blog.entry.web;

import am.ik.blog.MockConfig;
import am.ik.blog.config.SecurityConfig;
import am.ik.blog.config.WebConfig;
import am.ik.blog.entry.*;
import am.ik.blog.github.GitHubProps;
import am.ik.pagination.OffsetPage;
import am.ik.pagination.OffsetPageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebMvcTest(properties = {
		"blog.tenant.users[0]=user|{noop}password|demo=GET,LIST|xyz=GET",
		"blog.tenant.users[1]=foo|{noop}bar|demo=GET,LIST,EDIT,DELETE" })
@Import({ SecurityConfig.class, WebConfig.class, MockConfig.class, GitHubProps.class })
class EntryRestControllerTest {
	@Autowired
	WebTestClient webTestClient;

	@MockBean
	EntryMapper entryMapper;

	Entry entry100 = new EntryBuilder().withEntryId(100L)
			.withFrontMatter(new FrontMatterBuilder().withTitle("Hello").build())
			.withContent("Hello World!")
			.withCreated(new Author("demo", OffsetDateTime.now()))
			.withUpdated(new Author("demo", OffsetDateTime.now())).build();

	Entry entry200 = new EntryBuilder().withEntryId(200L)
			.withFrontMatter(new FrontMatterBuilder().withTitle("Blog").build())
			.withContent("Hello Blog!")
			.withCreated(new Author("demo", OffsetDateTime.now()))
			.withUpdated(new Author("demo", OffsetDateTime.now())).build();

	private Consumer<HttpHeaders> configureAuth(String tenantId, String username,
			String password) {
		return new Consumer<HttpHeaders>() {
			@Override
			public void accept(HttpHeaders httpHeaders) {
				if (tenantId != null) {
					httpHeaders.setBasicAuth(username, password);
				}
			}
		};
	}

	@ParameterizedTest
	@CsvSource({ ",,", "demo,user,password", "demo,foo,bar", "demo,admin,changeme" })
	void getEntry_200(String tenantId, String username, String password) {
		given(this.entryMapper.findOne(100L, tenantId, false))
				.willReturn(Optional.of(this.entry100));
		this.webTestClient.get()
				.uri((tenantId == null ? "" : "/tenants/" + tenantId) + "/entries/100")
				.headers(configureAuth(tenantId, username, password)).exchange()
				.expectStatus().isOk().expectBody().jsonPath("$.entryId").isEqualTo(100L)
				.jsonPath("$.content").isEqualTo("Hello World!")
				.jsonPath("$.frontMatter.title").isEqualTo("Hello");
	}

	@ParameterizedTest
	@CsvSource({ ",," })
	void getEntry_304(String tenantId, String username, String password) {
		given(this.entryMapper.findOne(100L, tenantId, false))
				.willReturn(Optional.of(this.entry100));
		this.webTestClient.get()
				.uri((tenantId == null ? "" : "/tenants/" + tenantId) + "/entries/100")
				.headers(configureAuth(tenantId, username, password))
				.headers(httpHeaders -> httpHeaders.setIfModifiedSince(
						this.entry100.getUpdated().getDate().toInstant()))
				.exchange().expectStatus().isNotModified().expectHeader()
				.lastModified(this.entry100.getUpdated().getDate().toInstant()
						.truncatedTo(ChronoUnit.SECONDS).toEpochMilli());
	}

	@ParameterizedTest
	@CsvSource({ ",,", "demo,user,password", "demo,foo,bar", "demo,admin,changeme" })
	void getEntry_404(String tenantId, String username, String password) {
		given(this.entryMapper.findOne(100L, tenantId, false))
				.willReturn(Optional.empty());
		this.webTestClient.get()
				.uri((tenantId == null ? "" : "/tenants/" + tenantId) + "/entries/100")
				.headers(configureAuth(tenantId, username, password)).exchange()
				.expectStatus().isNotFound();
	}

	@ParameterizedTest
	@CsvSource({ ",,", "demo,user,password", "demo,foo,bar", "demo,admin,changeme" })
	void getEntries_200(String tenantId, String username, String password) {
		given(this.entryMapper.findPage(any(), any(),
				ArgumentMatchers.<OffsetPageRequest> any())).willReturn(
						new OffsetPage<>(List.of(this.entry100, this.entry200), 2, 0, 2));
		this.webTestClient.get()
				.uri((tenantId == null ? "" : "/tenants/" + tenantId) + "/entries")
				.headers(configureAuth(tenantId, username, password)).exchange()
				.expectStatus().isOk().expectBody().jsonPath("$.content.length()")
				.isEqualTo(2).jsonPath("$.content[0].entryId").isEqualTo(100L)
				.jsonPath("$.content[0].content").isEqualTo("Hello World!")
				.jsonPath("$.content[0].frontMatter.title").isEqualTo("Hello")
				.jsonPath("$.content[1].entryId").isEqualTo(200L)
				.jsonPath("$.content[1].content").isEqualTo("Hello Blog!")
				.jsonPath("$.content[1].frontMatter.title").isEqualTo("Blog");
	}

	@Test
	void getEntries_401() {
		this.webTestClient.get().uri("/tenants/xyz/entries")
				.headers(httpHeaders -> httpHeaders.setBasicAuth("user", "pass"))
				.exchange().expectStatus().isUnauthorized();
	}

	@Test
	void getEntries_403() {
		this.webTestClient.get().uri("/tenants/xyz/entries")
				.headers(httpHeaders -> httpHeaders.setBasicAuth("user", "password"))
				.exchange().expectStatus().isForbidden();
	}

	@ParameterizedTest
	@ValueSource(strings = { """
			text/markdown,
			---
			title: Hello World!
			tags: ["Test", "Demo"]
			categories: ["Dev", "Blog", "Test"]
			---

			Hello World!
			Test Test Test!
			""", """
			application/json,
				{
				  "content": "Hello World!\\nTest Test Test!",
				  "frontMatter": {
				    "title": "Hello World!",
				    "tags": [
				      {
				        "name": "Test"
				      },
				      {
				        "name": "Demo"
				      }
				    ],
				    "categories": [
				      {
				        "name": "Dev"
				      },
				      {
				        "name": "Blog"
				      },
				      {
				        "name": "Test"
				      }
				    ]
				  }
				}
			""" })
	void postEntry_201(String value) {
		final String[] vals = value.split(",", 2);
		final String contentType = vals[0];
		final String body = vals[1];
		given(this.entryMapper.nextId(null)).willReturn(200L);
		this.webTestClient.post().uri("/entries")
				.header(HttpHeaders.CONTENT_TYPE, contentType)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue(body).exchange().expectStatus().isCreated().expectHeader()
				.location("http://localhost/entries/200").expectBody()
				.jsonPath("$.entryId").isEqualTo(200).jsonPath("$.content")
				.isEqualTo("Hello World!\nTest Test Test!").jsonPath("$.created.name")
				.isEqualTo("admin").jsonPath("$.created.date")
				.isEqualTo("2022-04-01T01:00:00Z").jsonPath("$.updated.name")
				.isEqualTo("admin").jsonPath("$.updated.date")
				.isEqualTo("2022-04-01T01:00:00Z").jsonPath("$.frontMatter.title")
				.isEqualTo("Hello World!").jsonPath("$.frontMatter.tags.length()")
				.isEqualTo(2).jsonPath("$.frontMatter.tags[0].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Demo")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");
	}

	@ParameterizedTest
	@ValueSource(strings = { """
			text/markdown,
			---
			title: Hello World!
			tags: ["Test", "Demo"]
			categories: ["Dev", "Blog", "Test"]
			date: 2023-01-01T01:00:00Z
			updated: 2023-01-01T01:00:00Z
			---

			Hello World!
			Test Test Test!
			""", """
			application/json,
				{
				  "content": "Hello World!\\nTest Test Test!",
				  "created": {
				    "date" : "2023-01-01T01:00:00Z"
				   },
				  "updated": {
				    "date" : "2023-01-01T01:00:00Z"
				  },
				  "frontMatter": {
				    "title": "Hello World!",
				    "tags": [
				      {
				        "name": "Test"
				      },
				      {
				        "name": "Demo"
				      }
				    ],
				    "categories": [
				      {
				        "name": "Dev"
				      },
				      {
				        "name": "Blog"
				      },
				      {
				        "name": "Test"
				      }
				    ]
				  }
				}
			""" })
	void postEntry_201_fixedDate(String value) {
		final String[] vals = value.split(",", 2);
		final String contentType = vals[0];
		final String body = vals[1];
		given(this.entryMapper.nextId(null)).willReturn(200L);
		this.webTestClient.post().uri("/entries")
				.header(HttpHeaders.CONTENT_TYPE, contentType)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue(body).exchange().expectStatus().isCreated().expectHeader()
				.location("http://localhost/entries/200").expectBody()
				.jsonPath("$.entryId").isEqualTo(200).jsonPath("$.content")
				.isEqualTo("Hello World!\nTest Test Test!").jsonPath("$.created.name")
				.isEqualTo("admin").jsonPath("$.created.date")
				.isEqualTo("2023-01-01T01:00:00Z").jsonPath("$.updated.name")
				.isEqualTo("admin").jsonPath("$.updated.date")
				.isEqualTo("2023-01-01T01:00:00Z").jsonPath("$.frontMatter.title")
				.isEqualTo("Hello World!").jsonPath("$.frontMatter.tags.length()")
				.isEqualTo(2).jsonPath("$.frontMatter.tags[0].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Demo")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");
	}

	@ParameterizedTest
	@ValueSource(strings = { """
			text/markdown,
			---
			---
			""", """
			application/json,
				{
				  "frontMatter": {
				    "title": "no title"
				  }
				}
			""" })
	void postEntry_400(String value) {
		final String[] vals = value.split(",", 2);
		final String contentType = vals[0];
		final String body = vals[1];
		given(this.entryMapper.nextId(null)).willReturn(200L);
		this.webTestClient.post().uri("/entries")
				.header(HttpHeaders.CONTENT_TYPE, contentType)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue(body).exchange().expectStatus().isBadRequest().expectHeader()
				.contentType(MediaType.APPLICATION_PROBLEM_JSON).expectBody()
				.jsonPath("$.title").isEqualTo("Bad Request").jsonPath("$.status")
				.isEqualTo(400).jsonPath("$.detail")
				.isEqualTo("Constraint violations found!").jsonPath("$.instance")
				.isEqualTo("/entries").jsonPath("$.violations.length()").isEqualTo(2)
				.jsonPath("$.violations[0].defaultMessage")
				.isEqualTo("\"content\" must not be blank")
				.jsonPath("$.violations[1].defaultMessage").isEqualTo(
						"The size of \"frontMatter.categories\" must be greater than or equal to 1. The given size is 0");
	}

	@ParameterizedTest
	@ValueSource(strings = { """
			text/markdown,
			---
			title: Hello World!
			tags: ["Test", "Demo"]
			categories: ["Dev", "Blog", "Test"]
			---

			Hello World!
			Test Test Test!
			""", """
			application/json,
				{
				  "content": "Hello World!\\nTest Test Test!",
				  "frontMatter": {
				    "title": "Hello World!",
				    "tags": [
				      {
				        "name": "Test"
				      },
				      {
				        "name": "Demo"
				      }
				    ],
				    "categories": [
				      {
				        "name": "Dev"
				      },
				      {
				        "name": "Blog"
				      },
				      {
				        "name": "Test"
				      }
				    ]
				  }
				}
			""" })
	void postEntry_401(String value) {
		final String[] vals = value.split(",", 2);
		final String contentType = vals[0];
		final String body = vals[1];
		this.webTestClient.post().uri("/entries")
				.header(HttpHeaders.CONTENT_TYPE, contentType)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("invalid", "invalid"))
				.bodyValue(body).exchange().expectStatus().isUnauthorized();
	}

	@ParameterizedTest
	@ValueSource(strings = { """
			text/markdown,
			---
			title: Hello World!
			tags: ["Test", "Demo"]
			categories: ["Dev", "Blog", "Test"]
			---

			Hello World!
			Test Test Test!
			""", """
			application/json,
				{
				  "content": "Hello World!\\nTest Test Test!",
				  "frontMatter": {
				    "title": "Hello World!",
				    "tags": [
				      {
				        "name": "Test"
				      },
				      {
				        "name": "Demo"
				      }
				    ],
				    "categories": [
				      {
				        "name": "Dev"
				      },
				      {
				        "name": "Blog"
				      },
				      {
				        "name": "Test"
				      }
				    ]
				  }
				}
			""" })
	void putEntry_201(String value) {
		final String[] vals = value.split(",", 2);
		final String contentType = vals[0];
		final String body = vals[1];
		given(this.entryMapper.findOne(100L, null, true)).willReturn(Optional.empty());
		this.webTestClient.put().uri("/entries/100")
				.header(HttpHeaders.CONTENT_TYPE, contentType)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue(body).exchange().expectStatus().isCreated().expectHeader()
				.location("http://localhost/entries/100").expectBody()
				.jsonPath("$.entryId").isEqualTo(100).jsonPath("$.content")
				.isEqualTo("Hello World!\nTest Test Test!").jsonPath("$.created.name")
				.isEqualTo("admin").jsonPath("$.created.date")
				.isEqualTo("2022-04-01T01:00:00Z").jsonPath("$.updated.name")
				.isEqualTo("admin").jsonPath("$.updated.date")
				.isEqualTo("2022-04-01T01:00:00Z").jsonPath("$.frontMatter.title")
				.isEqualTo("Hello World!").jsonPath("$.frontMatter.tags.length()")
				.isEqualTo(2).jsonPath("$.frontMatter.tags[0].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Demo")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");
	}

	@ParameterizedTest
	@ValueSource(strings = { """
			text/markdown,
			---
			title: Hello World!
			tags: ["Test", "Demo"]
			categories: ["Dev", "Blog", "Test"]
			date: 2023-01-01T01:00:00Z
			updated: 2023-01-01T01:00:00Z
			---

			Hello World!
			Test Test Test!
			""", """
			application/json,
				{
				  "content": "Hello World!\\nTest Test Test!",
				  "created": {
				    "date" : "2023-01-01T01:00:00Z"
				   },
				  "updated": {
				    "date" : "2023-01-01T01:00:00Z"
				  },
				  "frontMatter": {
				    "title": "Hello World!",
				    "tags": [
				      {
				        "name": "Test"
				      },
				      {
				        "name": "Demo"
				      }
				    ],
				    "categories": [
				      {
				        "name": "Dev"
				      },
				      {
				        "name": "Blog"
				      },
				      {
				        "name": "Test"
				      }
				    ]
				  }
				}
			""" })
	void putEntry_201_fixedDate(String value) {
		final String[] vals = value.split(",", 2);
		final String contentType = vals[0];
		final String body = vals[1];
		given(this.entryMapper.findOne(100L, null, true)).willReturn(Optional.empty());
		this.webTestClient.put().uri("/entries/100")
				.header(HttpHeaders.CONTENT_TYPE, contentType)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue(body).exchange().expectStatus().isCreated().expectHeader()
				.location("http://localhost/entries/100").expectBody()
				.jsonPath("$.entryId").isEqualTo(100).jsonPath("$.content")
				.isEqualTo("Hello World!\nTest Test Test!").jsonPath("$.created.name")
				.isEqualTo("admin").jsonPath("$.created.date")
				.isEqualTo("2023-01-01T01:00:00Z").jsonPath("$.updated.name")
				.isEqualTo("admin").jsonPath("$.updated.date")
				.isEqualTo("2023-01-01T01:00:00Z").jsonPath("$.frontMatter.title")
				.isEqualTo("Hello World!").jsonPath("$.frontMatter.tags.length()")
				.isEqualTo(2).jsonPath("$.frontMatter.tags[0].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Demo")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");
	}

	@ParameterizedTest
	@ValueSource(strings = { """
			text/markdown,
			---
			title: Hello World!
			tags: ["Test", "Demo"]
			categories: ["Dev", "Blog", "Test"]
			---

			Hello World!
			Test Test Test!
			""", """
			application/json,
				{
				  "content": "Hello World!\\nTest Test Test!",
				  "frontMatter": {
				    "title": "Hello World!",
				    "tags": [
				      {
				        "name": "Test"
				      },
				      {
				        "name": "Demo"
				      }
				    ],
				    "categories": [
				      {
				        "name": "Dev"
				      },
				      {
				        "name": "Blog"
				      },
				      {
				        "name": "Test"
				      }
				    ]
				  }
				}
			""" })
	void putEntry_200(String value) {
		final String[] vals = value.split(",", 2);
		final String contentType = vals[0];
		final String body = vals[1];
		final OffsetDateTime createdDate = OffsetDateTime.of(2022, 3, 1, 1, 0, 0, 0,
				ZoneOffset.UTC);
		given(this.entryMapper.findOne(100L, null, true))
				.willReturn(Optional.of(new EntryBuilder().withEntryId(100L)
						.withCreated(new Author("test", createdDate))
						.withUpdated(new Author("test", createdDate)).build()));
		this.webTestClient.put().uri("/entries/100")
				.header(HttpHeaders.CONTENT_TYPE, contentType)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue(body).exchange().expectStatus().isOk().expectBody()
				.jsonPath("$.entryId").isEqualTo(100).jsonPath("$.content")
				.isEqualTo("Hello World!\nTest Test Test!").jsonPath("$.created.name")
				.isEqualTo("test").jsonPath("$.created.date")
				.isEqualTo("2022-03-01T01:00:00Z").jsonPath("$.updated.name")
				.isEqualTo("admin").jsonPath("$.updated.date")
				.isEqualTo("2022-04-01T01:00:00Z").jsonPath("$.frontMatter.title")
				.isEqualTo("Hello World!").jsonPath("$.frontMatter.tags.length()")
				.isEqualTo(2).jsonPath("$.frontMatter.tags[0].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Demo")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");
	}

	@ParameterizedTest
	@ValueSource(strings = { """
			text/markdown,
			---
			title: Hello World!
			tags: ["Test", "Demo"]
			categories: ["Dev", "Blog", "Test"]
			date: 2023-01-01T01:00:00Z
			updated: 2023-01-01T01:00:00Z
			---

			Hello World!
			Test Test Test!
			""", """
			application/json,
				{
				  "content": "Hello World!\\nTest Test Test!",
				  "created": {
				    "date" : "2023-01-01T01:00:00Z"
				   },
				  "updated": {
				    "date" : "2023-01-01T01:00:00Z"
				  },
				  "frontMatter": {
				    "title": "Hello World!",
				    "tags": [
				      {
				        "name": "Test"
				      },
				      {
				        "name": "Demo"
				      }
				    ],
				    "categories": [
				      {
				        "name": "Dev"
				      },
				      {
				        "name": "Blog"
				      },
				      {
				        "name": "Test"
				      }
				    ]
				  }
				}
			""" })
	void putEntry_200_fixedDate(String value) {
		final String[] vals = value.split(",", 2);
		final String contentType = vals[0];
		final String body = vals[1];
		final OffsetDateTime createdDate = OffsetDateTime.of(2022, 3, 1, 1, 0, 0, 0,
				ZoneOffset.UTC);
		given(this.entryMapper.findOne(100L, null, true))
				.willReturn(Optional.of(new EntryBuilder().withEntryId(100L)
						.withCreated(new Author("test", createdDate))
						.withUpdated(new Author("test", createdDate)).build()));
		this.webTestClient.put().uri("/entries/100")
				.header(HttpHeaders.CONTENT_TYPE, contentType)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue(body).exchange().expectStatus().isOk().expectBody()
				.jsonPath("$.entryId").isEqualTo(100).jsonPath("$.content")
				.isEqualTo("Hello World!\nTest Test Test!").jsonPath("$.created.name")
				.isEqualTo("test").jsonPath("$.created.date")
				.isEqualTo("2023-01-01T01:00:00Z").jsonPath("$.updated.name")
				.isEqualTo("admin").jsonPath("$.updated.date")
				.isEqualTo("2023-01-01T01:00:00Z").jsonPath("$.frontMatter.title")
				.isEqualTo("Hello World!").jsonPath("$.frontMatter.tags.length()")
				.isEqualTo(2).jsonPath("$.frontMatter.tags[0].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Demo")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");
	}

	@ParameterizedTest
	@ValueSource(strings = { """
			text/markdown,
			---
			---
			""", """
			application/json,
				{
				  "frontMatter": {
				    "title": "no title"
				  }
				}
			""" })
	void putEntry_400(String value) {
		final String[] vals = value.split(",", 2);
		final String contentType = vals[0];
		final String body = vals[1];
		given(this.entryMapper.findOne(100L, null, true)).willReturn(Optional.empty());
		this.webTestClient.put().uri("/entries/0")
				.header(HttpHeaders.CONTENT_TYPE, contentType)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue(body).exchange().expectStatus().isBadRequest().expectHeader()
				.contentType(MediaType.APPLICATION_PROBLEM_JSON).expectBody()
				.jsonPath("$.title").isEqualTo("Bad Request").jsonPath("$.status")
				.isEqualTo(400).jsonPath("$.detail")
				.isEqualTo("Constraint violations found!").jsonPath("$.instance")
				.isEqualTo("/entries/0").jsonPath("$.violations.length()").isEqualTo(3)
				.jsonPath("$.violations[0].defaultMessage")
				.isEqualTo("\"entryId\" must be positive")
				.jsonPath("$.violations[1].defaultMessage")
				.isEqualTo("\"content\" must not be blank")
				.jsonPath("$.violations[2].defaultMessage").isEqualTo(
						"The size of \"frontMatter.categories\" must be greater than or equal to 1. The given size is 0");
	}

	@ParameterizedTest
	@ValueSource(strings = { """
			text/markdown,
			---
			title: Hello World!
			tags: ["Test", "Demo"]
			categories: ["Dev", "Blog", "Test"]
			---

			Hello World!
			Test Test Test!
			""", """
			application/json,
				{
				  "content": "Hello World!\\nTest Test Test!",
				  "frontMatter": {
				    "title": "Hello World!",
				    "tags": [
				      {
				        "name": "Test"
				      },
				      {
				        "name": "Demo"
				      }
				    ],
				    "categories": [
				      {
				        "name": "Dev"
				      },
				      {
				        "name": "Blog"
				      },
				      {
				        "name": "Test"
				      }
				    ]
				  }
				}
			""" })
	void putEntry_401(String value) {
		final String[] vals = value.split(",", 2);
		final String contentType = vals[0];
		final String body = vals[1];
		this.webTestClient.put().uri("/entries/100")
				.header(HttpHeaders.CONTENT_TYPE, contentType)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("invalid", "invalid"))
				.bodyValue(body).exchange().expectStatus().isUnauthorized();
	}

	@ParameterizedTest
	@CsvSource({ ",admin,changeme", "demo,foo,bar", "demo,admin,changeme" })
	void deleteEntry_204(String tenantId, String username, String password) {
		given(this.entryMapper.delete(100L, tenantId)).willReturn(1);
		this.webTestClient.delete()
				.uri((tenantId == null ? "" : "/tenants/" + tenantId) + "/entries/100")
				.headers(httpHeaders -> httpHeaders.setBasicAuth(username, password))
				.exchange().expectStatus().isNoContent();
	}

	@ParameterizedTest
	@CsvSource({ ",invalid,invalid", "abc,invalid,invalid" })
	void deleteEntry_401(String tenantId, String username, String password) {
		this.webTestClient.delete()
				.uri((tenantId == null ? "" : "/tenants/" + tenantId) + "/entries/100")
				.headers(httpHeaders -> httpHeaders.setBasicAuth(username, password))
				.exchange().expectStatus().isUnauthorized();
	}

	@ParameterizedTest
	@CsvSource({ ",user,password", "demo,user,password" })
	void deleteEntry_403(String tenantId, String username, String password) {
		this.webTestClient.delete()
				.uri((tenantId == null ? "" : "/tenants/" + tenantId) + "/entries/100")
				.headers(httpHeaders -> httpHeaders.setBasicAuth(username, password))
				.exchange().expectStatus().isForbidden();
	}
}
package am.ik.blog.entry.web;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import am.ik.blog.MockConfig;
import am.ik.blog.config.SecurityConfig;
import am.ik.blog.entry.Author;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryBuilder;
import am.ik.blog.entry.EntryMapper;
import am.ik.blog.entry.FrontMatterBuilder;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@WebMvcTest
@Import({ SecurityConfig.class, MockConfig.class })
class EntryRestControllerTest {
	@Autowired
	WebTestClient webTestClient;

	@MockBean
	EntryMapper entryMapper;

	Entry entry100 = new EntryBuilder()
			.withEntryId(100L)
			.withFrontMatter(new FrontMatterBuilder()
					.withTitle("Hello")
					.build())
			.withContent("Hello World!")
			.build();

	Entry entry200 = new EntryBuilder()
			.withEntryId(200L)
			.withFrontMatter(new FrontMatterBuilder()
					.withTitle("Blog")
					.build())
			.withContent("Hello Blog!")
			.build();

	@Test
	void getEntry_200() {
		given(this.entryMapper.findOne(100L, false))
				.willReturn(Optional.of(this.entry100));
		this.webTestClient.get()
				.uri("/entries/100")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.entryId").isEqualTo(100L)
				.jsonPath("$.content").isEqualTo("Hello World!")
				.jsonPath("$.frontMatter.title").isEqualTo("Hello");
	}

	@Test
	void getEntry_404() {
		given(this.entryMapper.findOne(100L, false))
				.willReturn(Optional.empty());
		this.webTestClient.get()
				.uri("/entries/100")
				.exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void getEntries_200() {
		given(this.entryMapper.findPage(any(), any())).willReturn(new PageImpl<>(List.of(this.entry100, this.entry200)));
		this.webTestClient.get()
				.uri("/entries")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.content.length()").isEqualTo(2)
				.jsonPath("$.content[0].entryId").isEqualTo(100L)
				.jsonPath("$.content[0].content").isEqualTo("Hello World!")
				.jsonPath("$.content[0].frontMatter.title").isEqualTo("Hello")
				.jsonPath("$.content[1].entryId").isEqualTo(200L)
				.jsonPath("$.content[1].content").isEqualTo("Hello Blog!")
				.jsonPath("$.content[1].frontMatter.title").isEqualTo("Blog");
	}

	@Test
	void putEntryFromMarkdown_201() {
		given(this.entryMapper.findOne(100L, true)).willReturn(Optional.empty());
		this.webTestClient.put()
				.uri("/entries/100")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_MARKDOWN_VALUE)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue("""
						---
						title: Hello World!
						tags: ["Test", "Demo"]
						categories: ["Dev", "Blog", "Test"]
						---

						Hello World!
						Test Test Test!
						""")
				.exchange()
				.expectStatus().isCreated()
				.expectHeader()
				.location("http://localhost/entries/100")
				.expectBody()
				.jsonPath("$.entryId").isEqualTo(100)
				.jsonPath("$.content").isEqualTo("Hello World!\nTest Test Test!")
				.jsonPath("$.created.name").isEqualTo("admin")
				.jsonPath("$.created.date").isEqualTo("2022-04-01T01:00:00Z")
				.jsonPath("$.updated.name").isEqualTo("admin")
				.jsonPath("$.updated.date").isEqualTo("2022-04-01T01:00:00Z")
				.jsonPath("$.frontMatter.title").isEqualTo("Hello World!")
				.jsonPath("$.frontMatter.tags.length()").isEqualTo(2)
				.jsonPath("$.frontMatter.tags[0].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Demo")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");
		verify(this.entryMapper).save(any());
	}

	@Test
	void putEntryFromMarkdown_201_fixedDate() {
		given(this.entryMapper.findOne(100L, true)).willReturn(Optional.empty());
		this.webTestClient.put()
				.uri("/entries/100")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_MARKDOWN_VALUE)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue("""
						---
						title: Hello World!
						tags: ["Test", "Demo"]
						categories: ["Dev", "Blog", "Test"]
						date: 2023-01-01T01:00:00Z
						updated: 2023-01-01T01:00:00Z
						---

						Hello World!
						Test Test Test!
						""")
				.exchange()
				.expectStatus().isCreated()
				.expectHeader()
				.location("http://localhost/entries/100")
				.expectBody()
				.jsonPath("$.entryId").isEqualTo(100)
				.jsonPath("$.content").isEqualTo("Hello World!\nTest Test Test!")
				.jsonPath("$.created.name").isEqualTo("admin")
				.jsonPath("$.created.date").isEqualTo("2023-01-01T01:00:00Z")
				.jsonPath("$.updated.name").isEqualTo("admin")
				.jsonPath("$.updated.date").isEqualTo("2023-01-01T01:00:00Z")
				.jsonPath("$.frontMatter.title").isEqualTo("Hello World!")
				.jsonPath("$.frontMatter.tags.length()").isEqualTo(2)
				.jsonPath("$.frontMatter.tags[0].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Demo")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");
		verify(this.entryMapper).save(any());
	}

	@Test
	void putEntryFromMarkdown_201_updated() {
		final OffsetDateTime createdDate = OffsetDateTime.of(2022, 3, 1, 1, 0, 0, 0, ZoneOffset.UTC);
		given(this.entryMapper.findOne(100L, true)).willReturn(Optional.of(new EntryBuilder()
				.withEntryId(100L)
				.withCreated(new Author("test", createdDate))
				.withUpdated(new Author("test", createdDate))
				.build()));
		this.webTestClient.put()
				.uri("/entries/100")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_MARKDOWN_VALUE)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue("""
						---
						title: Hello World!
						tags: ["Test", "Demo"]
						categories: ["Dev", "Blog", "Test"]
						---

						Hello World!
						Test Test Test!
						""")
				.exchange()
				.expectStatus().isCreated()
				.expectHeader()
				.location("http://localhost/entries/100")
				.expectBody()
				.jsonPath("$.entryId").isEqualTo(100)
				.jsonPath("$.content").isEqualTo("Hello World!\nTest Test Test!")
				.jsonPath("$.created.name").isEqualTo("test")
				.jsonPath("$.created.date").isEqualTo("2022-03-01T01:00:00Z")
				.jsonPath("$.updated.name").isEqualTo("admin")
				.jsonPath("$.updated.date").isEqualTo("2022-04-01T01:00:00Z")
				.jsonPath("$.frontMatter.title").isEqualTo("Hello World!")
				.jsonPath("$.frontMatter.tags.length()").isEqualTo(2)
				.jsonPath("$.frontMatter.tags[0].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Demo")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");
		verify(this.entryMapper).save(any());
	}

	@Test
	void putEntryFromMarkdown_201_updated_fixedDate() {
		final OffsetDateTime createdDate = OffsetDateTime.of(2022, 3, 1, 1, 0, 0, 0, ZoneOffset.UTC);
		given(this.entryMapper.findOne(100L, true)).willReturn(Optional.of(new EntryBuilder()
				.withEntryId(100L)
				.withCreated(new Author("test", createdDate))
				.withUpdated(new Author("test", createdDate))
				.build()));
		this.webTestClient.put()
				.uri("/entries/100")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_MARKDOWN_VALUE)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue("""
						---
						title: Hello World!
						tags: ["Test", "Demo"]
						categories: ["Dev", "Blog", "Test"]
						date: 2023-01-01T01:00:00Z
						updated: 2023-01-01T01:00:00Z
						---

						Hello World!
						Test Test Test!
						""")
				.exchange()
				.expectStatus().isCreated()
				.expectHeader()
				.location("http://localhost/entries/100")
				.expectBody()
				.jsonPath("$.entryId").isEqualTo(100)
				.jsonPath("$.content").isEqualTo("Hello World!\nTest Test Test!")
				.jsonPath("$.created.name").isEqualTo("test")
				.jsonPath("$.created.date").isEqualTo("2023-01-01T01:00:00Z")
				.jsonPath("$.updated.name").isEqualTo("admin")
				.jsonPath("$.updated.date").isEqualTo("2023-01-01T01:00:00Z")
				.jsonPath("$.frontMatter.title").isEqualTo("Hello World!")
				.jsonPath("$.frontMatter.tags.length()").isEqualTo(2)
				.jsonPath("$.frontMatter.tags[0].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Demo")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");
		verify(this.entryMapper).save(any());
	}

	@Test
	void putEntryFromMarkdown_400() {
		given(this.entryMapper.findOne(100L, true)).willReturn(Optional.empty());
		this.webTestClient.put()
				.uri("/entries/-1")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_MARKDOWN_VALUE)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue("""
						---
						---
						""")
				.exchange()
				.expectStatus().isBadRequest()
				.expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.expectBody()
				.jsonPath("$.title").isEqualTo("Bad Request")
				.jsonPath("$.status").isEqualTo(400)
				.jsonPath("$.detail").isEqualTo("Constraint violations found!")
				.jsonPath("$.instance").isEqualTo("/entries/-1")
				.jsonPath("$.violations.length()").isEqualTo(3)
				.jsonPath("$.violations[0].defaultMessage").isEqualTo("\"entryId\" must be positive")
				.jsonPath("$.violations[1].defaultMessage").isEqualTo("\"content\" must not be blank")
				.jsonPath("$.violations[2].defaultMessage").isEqualTo("The size of \"frontMatter.categories\" must be greater than or equal to 1. The given size is 0");
		verify(this.entryMapper, never()).save(any());
	}

	@Test
	void putEntryFromMarkdown_401() {
		this.webTestClient.put()
				.uri("/entries/100")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_MARKDOWN_VALUE)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("invalid", "invalid"))
				.bodyValue("""
						---
						title: Hello World!
						tags: ["Test", "Demo"]
						categories: ["Dev", "Blog", "Test"]
						---

						Hello World!
						Test Test Test!
						""")
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void putEntryFromJson_201() {
		given(this.entryMapper.findOne(100L, true)).willReturn(Optional.empty());
		this.webTestClient.put()
				.uri("/entries/100")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue("""
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
						""")
				.exchange()
				.expectStatus().isCreated()
				.expectHeader()
				.location("http://localhost/entries/100")
				.expectBody()
				.jsonPath("$.entryId").isEqualTo(100)
				.jsonPath("$.content").isEqualTo("Hello World!\nTest Test Test!")
				.jsonPath("$.created.name").isEqualTo("admin")
				.jsonPath("$.created.date").isEqualTo("2022-04-01T01:00:00Z")
				.jsonPath("$.updated.name").isEqualTo("admin")
				.jsonPath("$.updated.date").isEqualTo("2022-04-01T01:00:00Z")
				.jsonPath("$.frontMatter.title").isEqualTo("Hello World!")
				.jsonPath("$.frontMatter.tags.length()").isEqualTo(2)
				.jsonPath("$.frontMatter.tags[0].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Demo")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");
		verify(this.entryMapper).save(any());
	}

	@Test
	void putEntryFromJson_201_fixedDate() {
		given(this.entryMapper.findOne(100L, true)).willReturn(Optional.empty());
		this.webTestClient.put()
				.uri("/entries/100")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue("""
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
						""")
				.exchange()
				.expectStatus().isCreated()
				.expectHeader()
				.location("http://localhost/entries/100")
				.expectBody()
				.jsonPath("$.entryId").isEqualTo(100)
				.jsonPath("$.content").isEqualTo("Hello World!\nTest Test Test!")
				.jsonPath("$.created.name").isEqualTo("admin")
				.jsonPath("$.created.date").isEqualTo("2023-01-01T01:00:00Z")
				.jsonPath("$.updated.name").isEqualTo("admin")
				.jsonPath("$.updated.date").isEqualTo("2023-01-01T01:00:00Z")
				.jsonPath("$.frontMatter.title").isEqualTo("Hello World!")
				.jsonPath("$.frontMatter.tags.length()").isEqualTo(2)
				.jsonPath("$.frontMatter.tags[0].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Demo")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");
		verify(this.entryMapper).save(any());
	}

	@Test
	void putEntryFromJson_201_updated() {
		final OffsetDateTime createdDate = OffsetDateTime.of(2022, 3, 1, 1, 0, 0, 0, ZoneOffset.UTC);
		given(this.entryMapper.findOne(100L, true)).willReturn(Optional.of(new EntryBuilder()
				.withEntryId(100L)
				.withCreated(new Author("test", createdDate))
				.withUpdated(new Author("test", createdDate))
				.build()));
		this.webTestClient.put()
				.uri("/entries/100")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue("""
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
						""")
				.exchange()
				.expectStatus().isCreated()
				.expectHeader()
				.location("http://localhost/entries/100")
				.expectBody()
				.jsonPath("$.entryId").isEqualTo(100)
				.jsonPath("$.content").isEqualTo("Hello World!\nTest Test Test!")
				.jsonPath("$.created.name").isEqualTo("test")
				.jsonPath("$.created.date").isEqualTo("2022-03-01T01:00:00Z")
				.jsonPath("$.updated.name").isEqualTo("admin")
				.jsonPath("$.updated.date").isEqualTo("2022-04-01T01:00:00Z")
				.jsonPath("$.frontMatter.title").isEqualTo("Hello World!")
				.jsonPath("$.frontMatter.tags.length()").isEqualTo(2)
				.jsonPath("$.frontMatter.tags[0].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Demo")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");
		verify(this.entryMapper).save(any());
	}

	@Test
	void putEntryFromJson_201_updated_fixedDate() {
		final OffsetDateTime createdDate = OffsetDateTime.of(2022, 3, 1, 1, 0, 0, 0, ZoneOffset.UTC);
		given(this.entryMapper.findOne(100L, true)).willReturn(Optional.of(new EntryBuilder()
				.withEntryId(100L)
				.withCreated(new Author("test", createdDate))
				.withUpdated(new Author("test", createdDate))
				.build()));
		this.webTestClient.put()
				.uri("/entries/100")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue("""
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
						""")
				.exchange()
				.expectStatus().isCreated()
				.expectHeader()
				.location("http://localhost/entries/100")
				.expectBody()
				.jsonPath("$.entryId").isEqualTo(100)
				.jsonPath("$.content").isEqualTo("Hello World!\nTest Test Test!")
				.jsonPath("$.created.name").isEqualTo("test")
				.jsonPath("$.created.date").isEqualTo("2023-01-01T01:00:00Z")
				.jsonPath("$.updated.name").isEqualTo("admin")
				.jsonPath("$.updated.date").isEqualTo("2023-01-01T01:00:00Z")
				.jsonPath("$.frontMatter.title").isEqualTo("Hello World!")
				.jsonPath("$.frontMatter.tags.length()").isEqualTo(2)
				.jsonPath("$.frontMatter.tags[0].name").isEqualTo("Test")
				.jsonPath("$.frontMatter.tags[1].name").isEqualTo("Demo")
				.jsonPath("$.frontMatter.categories.length()").isEqualTo(3)
				.jsonPath("$.frontMatter.categories[0].name").isEqualTo("Dev")
				.jsonPath("$.frontMatter.categories[1].name").isEqualTo("Blog")
				.jsonPath("$.frontMatter.categories[2].name").isEqualTo("Test");
		verify(this.entryMapper).save(any());
	}

	@Test
	void putEntryFromJson_400() {
		given(this.entryMapper.findOne(100L, true)).willReturn(Optional.empty());
		this.webTestClient.put()
				.uri("/entries/-1")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.bodyValue("""
						{}
						""")
				.exchange()
				.expectStatus().isBadRequest()
				.expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.expectBody()
				.jsonPath("$.title").isEqualTo("Bad Request")
				.jsonPath("$.status").isEqualTo(400)
				.jsonPath("$.detail").isEqualTo("Constraint violations found!")
				.jsonPath("$.instance").isEqualTo("/entries/-1")
				.jsonPath("$.violations.length()").isEqualTo(3)
				.jsonPath("$.violations[0].defaultMessage").isEqualTo("\"entryId\" must be positive")
				.jsonPath("$.violations[1].defaultMessage").isEqualTo("\"content\" must not be blank")
				.jsonPath("$.violations[2].defaultMessage").isEqualTo("\"frontMatter\" must not be null");
		verify(this.entryMapper, never()).save(any());
	}

	@Test
	void deleteEntry_204() {
		given(this.entryMapper.delete(100L)).willReturn(1);
		this.webTestClient.delete()
				.uri("/entries/100")
				.headers(httpHeaders -> httpHeaders.setBasicAuth("admin", "changeme"))
				.exchange()
				.expectStatus().isNoContent();
		verify(this.entryMapper).delete(100L);
	}

	@Test
	void deleteEntry_401() {
		this.webTestClient.delete()
				.uri("/entries/100")
				.headers(httpHeaders -> httpHeaders.setBasicAuth("invalid", "invalid"))
				.exchange()
				.expectStatus().isUnauthorized();
	}
}
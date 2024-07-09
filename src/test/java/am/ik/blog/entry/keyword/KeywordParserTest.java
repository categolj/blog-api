package am.ik.blog.entry.keyword;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordParserTest {

	private KeywordParser keywordParser = new QueryParser();

	@Test
	void simple() {
		KeywordParser.QueryAndParams queryAndParams = this.keywordParser.convert("hello");
		assertThat(queryAndParams.query()).isEqualTo("e.content ILIKE :keyword_0");
		assertThat(queryAndParams.params()).containsExactlyInAnyOrderEntriesOf(Map.of("keyword_0", "hello"));
	}

	@Test
	void and() {
		KeywordParser.QueryAndParams queryAndParams = this.keywordParser.convert("hello world");
		assertThat(queryAndParams.query()).isEqualTo("e.content ILIKE :keyword_0 AND e.content ILIKE :keyword_1");
		assertThat(queryAndParams.params())
			.containsExactlyInAnyOrderEntriesOf(Map.of("keyword_0", "hello", "keyword_1", "world"));
	}

	@Test
	void quoted() {
		KeywordParser.QueryAndParams queryAndParams = this.keywordParser.convert("\"hello world\"");
		assertThat(queryAndParams.query()).isEqualTo("e.content ILIKE :keyword_0");
		assertThat(queryAndParams.params()).containsExactlyInAnyOrderEntriesOf(Map.of("keyword_0", "hello world"));
	}

	@Test
	void or() {
		KeywordParser.QueryAndParams queryAndParams = this.keywordParser.convert("hello or world");
		assertThat(queryAndParams.query()).isEqualTo("e.content ILIKE :keyword_0 OR e.content ILIKE :keyword_1");
		assertThat(queryAndParams.params())
			.containsExactlyInAnyOrderEntriesOf(Map.of("keyword_0", "hello", "keyword_1", "world"));
	}

	@Test
	void not() {
		KeywordParser.QueryAndParams queryAndParams = this.keywordParser.convert("hello -world");
		assertThat(queryAndParams.query()).isEqualTo("e.content ILIKE :keyword_0 AND e.content NOT ILIKE :keyword_1");
		assertThat(queryAndParams.params())
			.containsExactlyInAnyOrderEntriesOf(Map.of("keyword_0", "hello", "keyword_1", "world"));
	}

	@Test
	void singleNot() {
		KeywordParser.QueryAndParams queryAndParams = this.keywordParser.convert("-hello");
		assertThat(queryAndParams.query()).isEqualTo("e.content NOT ILIKE :keyword_0");
		assertThat(queryAndParams.params()).containsExactlyInAnyOrderEntriesOf(Map.of("keyword_0", "hello"));
	}

	@Test
	void hyphen() {
		KeywordParser.QueryAndParams queryAndParams = this.keywordParser.convert("hello-world");
		assertThat(queryAndParams.query()).isEqualTo("e.content ILIKE :keyword_0");
		assertThat(queryAndParams.params()).containsExactlyInAnyOrderEntriesOf(Map.of("keyword_0", "hello-world"));
	}

	@Test
	void nest() {
		KeywordParser.QueryAndParams queryAndParams = this.keywordParser.convert("hello (world or java)");
		assertThat(queryAndParams.query())
			.isEqualTo("e.content ILIKE :keyword_0 AND (e.content ILIKE :keyword_1 OR e.content ILIKE :keyword_2)");
		assertThat(queryAndParams.params()).containsExactlyInAnyOrderEntriesOf(
				Map.of("keyword_0", "hello", "keyword_1", "world", "keyword_2", "java"));
	}

}
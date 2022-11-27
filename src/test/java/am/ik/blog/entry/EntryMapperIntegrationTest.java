package am.ik.blog.entry;

import java.util.Map;
import java.util.Optional;

import am.ik.blog.category.Category;
import am.ik.blog.github.Fixtures;
import am.ik.blog.tag.Tag;
import am.ik.blog.util.FileLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class EntryMapperIntegrationTest {

	@Autowired
	EntryMapper entryMapper;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@BeforeEach
	public void reset() {
		jdbcTemplate.update(FileLoader.loadAsString("sql/delete-test-data.sql"));
	}

	@Test
	@Transactional
	void insert() {
		final Entry entry = Fixtures.entry(99999L);
		final Map<String, Integer> result = this.entryMapper.save(entry);
		assertThat(result.get("upsertEntry")).isEqualTo(1);
		final Optional<Entry> one = this.entryMapper.findOne(entry.getEntryId(), false);
		assertThat(one.isPresent()).isTrue();
		final Entry found = one.get();
		assertThat(found.getEntryId()).isEqualTo(99999L);
		assertThat(found.getContent()).isEqualTo("Hello");
		final FrontMatter frontMatter = found.getFrontMatter();
		assertThat(frontMatter.getTitle()).isEqualTo("Hello");
		assertThat(frontMatter.getTags()).hasSize(3);
		assertThat(frontMatter.getTags()).containsExactly(new Tag("a"), new Tag("b"),
				new Tag("c"));
		assertThat(frontMatter.getCategories()).hasSize(3);
		assertThat(frontMatter.getCategories()).containsExactly(new Category("foo"),
				new Category("bar"), new Category("hoge"));
		assertThat(found.getCreated().getName()).isEqualTo("demo");
		assertThat(found.getCreated().getDate()).isNotNull();
		assertThat(found.getUpdated().getName()).isEqualTo("demo");
		assertThat(found.getUpdated().getDate()).isNotNull();
	}

	@Test
	@Transactional
	void update() {
		jdbcTemplate.update(FileLoader.loadAsString("sql/insert-test-data.sql"));
		final Entry entry = Fixtures.entry(99999L);
		final Map<String, Integer> result = this.entryMapper.save(entry);
		assertThat(result.get("upsertEntry")).isEqualTo(1);
		final Optional<Entry> one = this.entryMapper.findOne(entry.getEntryId(), false);
		assertThat(one.isPresent()).isTrue();
		final Entry found = one.get();
		assertThat(found.getEntryId()).isEqualTo(99999L);
		assertThat(found.getContent()).isEqualTo("Hello");
		final FrontMatter frontMatter = found.getFrontMatter();
		assertThat(frontMatter.getTitle()).isEqualTo("Hello");
		assertThat(frontMatter.getTags()).hasSize(3);
		assertThat(frontMatter.getTags()).containsExactly(new Tag("a"), new Tag("b"),
				new Tag("c"));
		assertThat(frontMatter.getCategories()).hasSize(3);
		assertThat(frontMatter.getCategories()).containsExactly(new Category("foo"),
				new Category("bar"), new Category("hoge"));
		assertThat(found.getCreated().getName()).isEqualTo("demo");
		assertThat(found.getCreated().getDate()).isNotNull();
		assertThat(found.getUpdated().getName()).isEqualTo("demo");
		assertThat(found.getUpdated().getDate()).isNotNull();
	}
}
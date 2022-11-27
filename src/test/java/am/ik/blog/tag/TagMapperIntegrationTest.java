package am.ik.blog.tag;

import java.util.List;

import am.ik.blog.util.FileLoader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TagMapperIntegrationTest {
	@Autowired
	TagMapper tagMapper;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@BeforeEach
	public void reset() {
		jdbcTemplate.update(FileLoader.loadAsString("sql/delete-test-data.sql"));
		jdbcTemplate.update(FileLoader.loadAsString("sql/insert-test-data.sql"));
	}

	@Test
	void findOrderByTagNameAsc() {
		final List<TagNameAndCount> tags = this.tagMapper.findOrderByTagNameAsc();
		assertThat(tags).hasSize(3);
		assertThat(tags).containsExactly(new TagNameAndCount("test1", 3),
				new TagNameAndCount("test2", 2), new TagNameAndCount("test3", 2));
	}
}
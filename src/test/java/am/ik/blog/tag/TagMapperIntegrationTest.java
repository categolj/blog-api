package am.ik.blog.tag;

import java.util.List;

import am.ik.blog.util.FileLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Testcontainers(disabledWithoutDocker = false)
class TagMapperIntegrationTest {

	@Autowired
	TagMapper tagMapper;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine");

	@BeforeEach
	public void reset() {
		jdbcTemplate.update(FileLoader.loadAsString("sql/delete-test-data.sql"));
		jdbcTemplate.update(FileLoader.loadAsString("sql/insert-test-data.sql"));
	}

	@Test
	void findOrderByTagNameAsc() {
		final List<TagAndCount> tags = this.tagMapper.findOrderByTagNameAsc(null);
		assertThat(tags).hasSize(3);
		assertThat(tags).containsExactly(new TagAndCount(new Tag("test1"), 3), new TagAndCount(new Tag("test2"), 2),
				new TagAndCount(new Tag("test3"), 2));
		final List<TagAndCount> demoTags = this.tagMapper.findOrderByTagNameAsc("demo");
		assertThat(demoTags).isEmpty();
	}

}
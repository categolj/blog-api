package am.ik.blog.tag;

import java.util.List;

import am.ik.blog.util.FileLoader;
import org.assertj.core.api.Assertions;
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
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Testcontainers(disabledWithoutDocker = true)
class TagMapperIntegrationTest {
	@Autowired
	TagMapper tagMapper;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
			"postgres:14-alpine");

	@BeforeEach
	public void reset() {
		jdbcTemplate.update(FileLoader.loadAsString("sql/delete-test-data.sql"));
		jdbcTemplate.update(FileLoader.loadAsString("sql/insert-test-data.sql"));
	}

	@Test
	void findOrderByTagNameAsc() {
		final List<TagNameAndCount> tags = this.tagMapper.findOrderByTagNameAsc(null);
		assertThat(tags).hasSize(3);
		assertThat(tags).containsExactly(new TagNameAndCount("test1", 3),
				new TagNameAndCount("test2", 2), new TagNameAndCount("test3", 2));
		final List<TagNameAndCount> demoTags = this.tagMapper
				.findOrderByTagNameAsc("demo");
		assertThat(demoTags).isEmpty();
	}
}
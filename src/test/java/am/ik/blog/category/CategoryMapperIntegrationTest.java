package am.ik.blog.category;

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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CategoryMapperIntegrationTest {

	@Autowired
	CategoryMapper categoryMapper;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@BeforeEach
	public void reset() {
		jdbcTemplate.update(FileLoader.loadAsString("sql/delete-test-data.sql"));
		jdbcTemplate.update(FileLoader.loadAsString("sql/insert-test-data.sql"));
	}

	@Test
	void findAll() {
		final List<List<Category>> categories = this.categoryMapper.findAll(null);
		assertThat(categories).hasSize(3);
		assertThat(categories.get(0)).containsExactly(new Category("a"),
				new Category("b"), new Category("c"));
		assertThat(categories.get(1)).containsExactly(new Category("x"),
				new Category("y"));
		assertThat(categories.get(2)).containsExactly(new Category("x"),
				new Category("y"), new Category("z"));
		final List<List<Category>> demoCategories = this.categoryMapper.findAll("demo");
		assertThat(demoCategories).isEmpty();
	}
}
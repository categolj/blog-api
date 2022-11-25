package am.ik.blog.category;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import am.ik.blog.util.FileLoader;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryMapper {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	public CategoryMapper(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<List<Category>> findAll() {
		final String sql = FileLoader
				.loadSqlAsString("am/ik/blog/category/CategoryMapper/findAll.sql");
		return this.jdbcTemplate.query(sql, Map.of(), (rs, rowNum) -> {
			final String category = rs.getString("category");
			if (category == null) {
				return List.of();
			}
			return Stream.of(category.split(",")).map(Category::new).toList();
		});
	}
}
package am.ik.blog.category;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
			final List<Category> categories = new ArrayList<>();
			final Array categoriesArray = rs.getArray("categories");
			if (categoriesArray != null) {
				for (Object category : ((Object[]) categoriesArray.getArray())) {
					categories.add(new Category((String) category));
				}
			}
			return categories;
		});
	}
}
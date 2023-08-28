package am.ik.blog.category;

import org.mybatis.scripting.thymeleaf.SqlGenerator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

import static am.ik.blog.util.FileLoader.loadSqlAsString;

@Repository
public class CategoryMapper {

	private final JdbcClient jdbcClient;

	private final SqlGenerator sqlGenerator;

	public CategoryMapper(JdbcClient jdbcClient, SqlGenerator sqlGenerator) {
		this.jdbcClient = jdbcClient;
		this.sqlGenerator = sqlGenerator;
	}

	public List<List<Category>> findAll(String tenantId) {
		final MapSqlParameterSource params = new MapSqlParameterSource().addValue("tenantId", tenantId);
		final String sql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/category/CategoryMapper/findAll.sql"),
				params.getValues(), params::addValue);
		return this.jdbcClient.sql(sql) //
			.paramSource(params) //
			.query((rs, rowNum) -> {
				final List<Category> categories = new ArrayList<>();
				final Array categoriesArray = rs.getArray("categories");
				if (categoriesArray != null) {
					for (Object category : ((Object[]) categoriesArray.getArray())) {
						categories.add(new Category((String) category));
					}
				}
				return categories;
			})
			.list();
	}

}
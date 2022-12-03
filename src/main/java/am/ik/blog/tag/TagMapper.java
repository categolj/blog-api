package am.ik.blog.tag;

import java.util.List;

import am.ik.blog.util.FileLoader;
import org.mybatis.scripting.thymeleaf.SqlGenerator;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static am.ik.blog.util.FileLoader.loadSqlAsString;

@Repository
public class TagMapper {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	private final SqlGenerator sqlGenerator;

	public TagMapper(NamedParameterJdbcTemplate jdbcTemplate, SqlGenerator sqlGenerator) {
		this.jdbcTemplate = jdbcTemplate;
		this.sqlGenerator = sqlGenerator;
	}

	public List<TagNameAndCount> findOrderByTagNameAsc(String tenantId) {
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("tenantId", tenantId);
		final String sql = this.sqlGenerator.generate(
				loadSqlAsString("am/ik/blog/tag/TagMapper/findOrderByTagNameAsc.sql"),
				params.getValues(), params::addValue);
		return this.jdbcTemplate.query(sql, params,
				(rs, rowNum) -> new TagNameAndCount(rs.getString("tag_name"),
						rs.getInt("count")));
	}
}

package am.ik.blog.tag;

import java.util.List;

import am.ik.blog.util.FileLoader;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TagMapper {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	public TagMapper(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<TagNameAndCount> findOrderByTagNameAsc() {
		final String sql = FileLoader
				.loadSqlAsString("am/ik/blog/tag/TagMapper/findOrderByTagNameAsc.sql");
		return this.jdbcTemplate.query(sql,
				(rs, rowNum) -> new TagNameAndCount(rs.getString("tag_name"),
						rs.getInt("count")));
	}
}

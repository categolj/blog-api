package am.ik.blog.tag;

import org.mybatis.scripting.thymeleaf.SqlGenerator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

import static am.ik.blog.util.FileLoader.loadSqlAsString;

@Repository
public class TagMapper {

	private final JdbcClient jdbcClient;

	private final SqlGenerator sqlGenerator;

	public TagMapper(JdbcClient jdbcClient, SqlGenerator sqlGenerator) {
		this.jdbcClient = jdbcClient;
		this.sqlGenerator = sqlGenerator;
	}

	public List<TagNameAndCount> findOrderByTagNameAsc(String tenantId) {
		final MapSqlParameterSource params = new MapSqlParameterSource().addValue("tenantId", tenantId);
		final String sql = this.sqlGenerator.generate(
				loadSqlAsString("am/ik/blog/tag/TagMapper/findOrderByTagNameAsc.sql"), params.getValues(),
				params::addValue);
		return this.jdbcClient.sql(sql) //
			.paramSource(params) //
			.query((rs, rowNum) -> new TagNameAndCount(rs.getString("tag_name"), rs.getInt("count")))
			.list();
	}

}

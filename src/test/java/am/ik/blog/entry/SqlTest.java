package am.ik.blog.entry;

import java.util.List;

import am.ik.blog.category.Category;
import am.ik.blog.util.FileLoader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mybatis.scripting.thymeleaf.SqlGenerator;
import org.mybatis.scripting.thymeleaf.SqlGeneratorConfig;
import org.mybatis.scripting.thymeleaf.processor.BindVariableRender.BuiltIn;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;

import static org.assertj.core.api.Assertions.assertThat;

class SqlTest {

	final SqlGeneratorConfig config = SqlGeneratorConfig
		.newInstanceWithCustomizer(c -> c.getDialect().setBindVariableRenderInstance(BuiltIn.SPRING_NAMED_PARAMETER));

	final SqlGenerator sqlGenerator = new SqlGenerator(config);

	@Test
	@Disabled
	public void findOneExcludeContent() {
		final MapSqlParameterSource params = new MapSqlParameterSource().addValue("entryId", 100)
			.addValue("excludeContent", true);
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/findOne.sql"),
				params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("""
				SELECT e.entry_id,
				       e.title,

				       '' AS content,
				       COALESCE(e.categories, '{}') AS categories,
				       COALESCE(e.tags, '{}')       AS tags,
				       e.created_by,
				       e.created_date,
				       e.last_modified_by,
				       e.last_modified_date
				FROM entry AS e
				WHERE e.entry_id = :entryId

				  AND e.tenant_id = '_'
				""".trim());
	}

	@Test
	@Disabled
	public void findOneIncludeContent() {
		final MapSqlParameterSource params = new MapSqlParameterSource().addValue("entryId", 100)
			.addValue("excludeContent", false);
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/findOne.sql"),
				params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("""
				SELECT e.entry_id,
				       e.title,

				       e.content,


				       COALESCE(e.categories, '{}') AS categories,
				       COALESCE(e.tags, '{}')       AS tags,
				       e.created_by,
				       e.created_date,
				       e.last_modified_by,
				       e.last_modified_date
				FROM entry AS e
				WHERE e.entry_id = :entryId

				  AND e.tenant_id = '_'
				""".trim());
	}

	@Test
	@Disabled
	public void findOneIncludeContentWithTenantId() {
		final MapSqlParameterSource params = new MapSqlParameterSource().addValue("entryId", 100)
			.addValue("excludeContent", false)
			.addValue("tenantId", "demo");
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/findOne.sql"),
				params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("""
				SELECT e.entry_id,
				       e.title,

				       e.content,


				       COALESCE(e.categories, '{}') AS categories,
				       COALESCE(e.tags, '{}')       AS tags,
				       e.created_by,
				       e.created_date,
				       e.last_modified_by,
				       e.last_modified_date
				FROM entry AS e
				WHERE e.entry_id = :entryId

				  AND e.tenant_id = :tenantId
				""".trim());
	}

	@Test
	@Disabled
	public void findAll() {
		final MapSqlParameterSource params = new MapSqlParameterSource().addValue("entryIds", List.of(1, 2, 3))
			.addValue("entryIds[0]", 1)
			.addValue("entryIds[1]", 2)
			.addValue("entryIds[2]", 3);
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/findAll.sql"),
				params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("""
				SELECT e.entry_id,
				       e.title,

				       e.content,


				       COALESCE(e.categories, '{}') AS categories,
				       COALESCE(e.tags, '{}')       AS tags,
				       e.created_by,
				       e.created_date,
				       e.last_modified_by,
				       e.last_modified_date
				FROM entry AS e
				WHERE e.entry_id IN (:entryIds[0], :entryIds[1], :entryIds[2])

				  AND e.tenant_id = '_'
				ORDER BY e.last_modified_date DESC
				""".trim());
		final String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, params);
		assertThat(sqlToUse.trim()).isEqualTo("""
				SELECT e.entry_id,
				       e.title,

				       e.content,


				       COALESCE(e.categories, '{}') AS categories,
				       COALESCE(e.tags, '{}')       AS tags,
				       e.created_by,
				       e.created_date,
				       e.last_modified_by,
				       e.last_modified_date
				FROM entry AS e
				WHERE e.entry_id IN (?, ?, ?)

				  AND e.tenant_id = '_'
				ORDER BY e.last_modified_date DESC
				""".trim());
		final ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		final List<SqlParameter> declaredParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, params);
		final Object[] buildValueArray = NamedParameterUtils.buildValueArray(parsedSql, params, declaredParameters);
		assertThat(buildValueArray).hasSize(3);
		assertThat(((SqlParameterValue) buildValueArray[0]).getName()).isEqualTo("entryIds[0]");
		assertThat(((SqlParameterValue) buildValueArray[0]).getValue()).isEqualTo(1);
		assertThat(((SqlParameterValue) buildValueArray[1]).getName()).isEqualTo("entryIds[1]");
		assertThat(((SqlParameterValue) buildValueArray[1]).getValue()).isEqualTo(2);
		assertThat(((SqlParameterValue) buildValueArray[2]).getName()).isEqualTo("entryIds[2]");
		assertThat(((SqlParameterValue) buildValueArray[2]).getValue()).isEqualTo(3);
	}

	@Test
	@Disabled
	public void deleteEntry() {
		final MapSqlParameterSource params = new MapSqlParameterSource().addValue("entryId", 100);
		final String sql = sqlGenerator.generate(
				FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/deleteEntry.sql"), params.getValues(),
				params::addValue);
		assertThat(sql.trim()).isEqualTo("""
				DELETE
				FROM entry AS e
				WHERE e.entry_id = :entryId

				  AND e.tenant_id = '_'
				""".trim());
	}

	@Test
	@Disabled
	public void entryIds() {
		final MapSqlParameterSource params = new MapSqlParameterSource();
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/entryIds.sql"),
				params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("""
				SELECT DISTINCT e.entry_id, e.last_modified_date
				FROM entry AS e
				WHERE 1 = 1






				  AND e.tenant_id = '_'
				ORDER BY e.last_modified_date DESC
				""".trim());
	}

	@Test
	@Disabled
	public void entryIdsWithKeyword() {
		final MapSqlParameterSource params = new MapSqlParameterSource().addValue("keywordsCount", 2)
			.addValue("keywords[0]", "xyz")
			.addValue("keywords[1]", "abc");
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/entryIds.sql"),
				params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("""
				SELECT DISTINCT e.entry_id, e.last_modified_date
				FROM entry AS e
				WHERE 1 = 1


				  AND e.keywords @> ARRAY[ :keywords[0] ]::character varying[]
				  AND e.keywords @> ARRAY[ :keywords[1] ]::character varying[]







				  AND e.tenant_id = '_'
				ORDER BY e.last_modified_date DESC
				""".trim());
		final String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, params);
		assertThat(sqlToUse.trim()).isEqualTo("""
				SELECT DISTINCT e.entry_id, e.last_modified_date
				FROM entry AS e
				WHERE 1 = 1


				  AND e.keywords @> ARRAY[ ? ]::character varying[]
				  AND e.keywords @> ARRAY[ ? ]::character varying[]







				  AND e.tenant_id = '_'
				ORDER BY e.last_modified_date DESC
				""".trim());
		final ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		final List<SqlParameter> declaredParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, params);
		final Object[] buildValueArray = NamedParameterUtils.buildValueArray(parsedSql, params, declaredParameters);
		assertThat(buildValueArray).hasSize(2);
		assertThat(((SqlParameterValue) buildValueArray[0]).getName()).isEqualTo("keywords[0]");
		assertThat(((SqlParameterValue) buildValueArray[0]).getValue()).isEqualTo("xyz");
		assertThat(((SqlParameterValue) buildValueArray[1]).getName()).isEqualTo("keywords[1]");
		assertThat(((SqlParameterValue) buildValueArray[1]).getValue()).isEqualTo("abc");
	}

	@Test
	@Disabled
	public void entryIdsWithTag() {
		final MapSqlParameterSource params = new MapSqlParameterSource().addValue("tag", "foo");
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/entryIds.sql"),
				params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("""
				SELECT DISTINCT e.entry_id, e.last_modified_date
				FROM entry AS e
				WHERE 1 = 1





				  AND e.tags @> ARRAY[ :tag ]::character varying[]


				  AND e.tenant_id = '_'
				ORDER BY e.last_modified_date DESC
				""".trim());
	}

	@Test
	@Disabled
	public void entryIdsWithCategoryOrders() {
		final List<Category> categories = List.of(new Category("x"), new Category("y"), new Category("z"));
		final MapSqlParameterSource params = new MapSqlParameterSource().addValue("categories", categories);
		for (int i = 0; i < categories.size(); i++) {
			params.addValue("categories[" + i + "]", categories.get(i).name());
		}
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/entryIds.sql"),
				params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("""
				SELECT DISTINCT e.entry_id, e.last_modified_date
				FROM entry AS e
				WHERE 1 = 1





				  AND e.categories[1] = :categories[0]
				  AND e.categories[2] = :categories[1]
				  AND e.categories[3] = :categories[2]




				  AND e.tenant_id = '_'
				ORDER BY e.last_modified_date DESC
				""".trim());
		final String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, params);
		assertThat(sqlToUse.trim()).isEqualTo("""
				SELECT DISTINCT e.entry_id, e.last_modified_date
				FROM entry AS e
				WHERE 1 = 1





				  AND e.categories[1] = ?
				  AND e.categories[2] = ?
				  AND e.categories[3] = ?




				  AND e.tenant_id = '_'
				ORDER BY e.last_modified_date DESC
				""".trim());
		final ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		final List<SqlParameter> declaredParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, params);
		final Object[] buildValueArray = NamedParameterUtils.buildValueArray(parsedSql, params, declaredParameters);
		assertThat(buildValueArray).hasSize(3);
		assertThat(((SqlParameterValue) buildValueArray[0]).getName()).isEqualTo("categories[0]");
		assertThat(((SqlParameterValue) buildValueArray[0]).getValue()).isEqualTo("x");
		assertThat(((SqlParameterValue) buildValueArray[1]).getName()).isEqualTo("categories[1]");
		assertThat(((SqlParameterValue) buildValueArray[1]).getValue()).isEqualTo("y");
		assertThat(((SqlParameterValue) buildValueArray[2]).getName()).isEqualTo("categories[2]");
		assertThat(((SqlParameterValue) buildValueArray[2]).getValue()).isEqualTo("z");
	}

}
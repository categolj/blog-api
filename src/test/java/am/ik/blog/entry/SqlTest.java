package am.ik.blog.entry;

import java.util.List;

import am.ik.blog.category.Category;
import am.ik.blog.entry.search.CategoryOrder;
import am.ik.blog.entry.search.CategoryOrders;
import am.ik.blog.util.FileLoader;
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
	final SqlGeneratorConfig config = SqlGeneratorConfig.newInstanceWithCustomizer(c ->
			c.getDialect().setBindVariableRender(BuiltIn.SPRING_NAMED_PARAMETER.getType()));

	final SqlGenerator sqlGenerator = new SqlGenerator(config);

	@Test
	public void findOneExcludeContent() {
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("entryId", 100)
				.addValue("excludeContent", true);
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/findOne.sql"), params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("""
				SELECT e.entry_id,
				       e.title,
				    
				       e.created_by,
				       e.created_date,
				       e.last_modified_by,
				       e.last_modified_date
				FROM entry AS e
				WHERE e.entry_id = :entryId
				""".trim());
	}

	@Test
	public void findOneIncludeContent() {
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("entryId", 100)
				.addValue("excludeContent", false);
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/findOne.sql"), params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("""
				SELECT e.entry_id,
				       e.title,

				       e.content,

				       e.created_by,
				       e.created_date,
				       e.last_modified_by,
				       e.last_modified_date
				FROM entry AS e
				WHERE e.entry_id = :entryId
								""".trim());
	}

	@Test
	public void findAll() {
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("entryIds", List.of(1, 2, 3))
				.addValue("entryIds[0]", 1)
				.addValue("entryIds[1]", 2)
				.addValue("entryIds[2]", 3);
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/findAll.sql"), params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("""
				SELECT e.entry_id,
				       e.title,
				
				       e.content,
				
				       e.created_by,
				       e.created_date,
				       e.last_modified_by,
				       e.last_modified_date
				FROM entry AS e
				WHERE e.entry_id IN (:entryIds[0], :entryIds[1], :entryIds[2])
				ORDER BY e.last_modified_date DESC
				""".trim());
		final String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, params);
		assertThat(sqlToUse.trim()).isEqualTo("""
				SELECT e.entry_id,
				       e.title,
				
				       e.content,
				
				       e.created_by,
				       e.created_date,
				       e.last_modified_by,
				       e.last_modified_date
				FROM entry AS e
				WHERE e.entry_id IN (?, ?, ?)
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
	public void deleteEntry() {
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("entryId", 100);
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/deleteEntry.sql"), params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("""
				DELETE
				FROM entry
				WHERE entry_id = :entryId
				""".trim());
	}

	@Test
	public void categoriesMap() {
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("entryIds", List.of(1, 2, 3))
				.addValue("entryIds[0]", 1)
				.addValue("entryIds[1]", 2)
				.addValue("entryIds[2]", 3);
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/categoriesMap.sql"), params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("""
				SELECT entry_id, category_name
				FROM category
				WHERE entry_id IN (:entryIds[0], :entryIds[1], :entryIds[2])
				ORDER BY category_order ASC
				""".trim());
		final String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, params);
		assertThat(sqlToUse.trim()).isEqualTo("""
				SELECT entry_id, category_name
				FROM category
				WHERE entry_id IN (?, ?, ?)
				ORDER BY category_order ASC
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
	public void tagsMap() {
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("entryIds", List.of(1, 2, 3))
				.addValue("entryIds[0]", 1)
				.addValue("entryIds[1]", 2)
				.addValue("entryIds[2]", 3);
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/tagsMap.sql"), params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("""
				SELECT entry_id, tag_name
				FROM entry_tag
				WHERE entry_id IN (:entryIds[0], :entryIds[1], :entryIds[2])
				""".trim());
		final String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, params);
		assertThat(sqlToUse.trim()).isEqualTo("""
				SELECT entry_id, tag_name
				FROM entry_tag
				WHERE entry_id IN (?, ?, ?)
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
	public void entryIds() {
		final MapSqlParameterSource params = new MapSqlParameterSource();
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/entryIds.sql"), params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("SELECT DISTINCT e.entry_id, e.last_modified_date\n"
				+ "FROM entry AS e\n"
				+ "         \n"
				+ "    \n"
				+ "WHERE 1 = 1\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "ORDER BY e.last_modified_date DESC\n".trim());
	}

	@Test
	public void entryIdsWithKeyword() {
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("keyword", "xyz");
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/entryIds.sql"), params.getValues(), params::addValue);
		assertThat(sql).isEqualTo("SELECT DISTINCT e.entry_id, e.last_modified_date\n"
				+ "FROM entry AS e\n"
				+ "         \n"
				+ "    \n"
				+ "WHERE 1 = 1\n"
				+ "\n"
				+ "\n"
				+ "  AND lower(e.content) LIKE :keywordPattern\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "ORDER BY e.last_modified_date DESC\n");
		final String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, params);
		assertThat(sqlToUse.trim()).isEqualTo("SELECT DISTINCT e.entry_id, e.last_modified_date\n"
				+ "FROM entry AS e\n"
				+ "         \n"
				+ "    \n"
				+ "WHERE 1 = 1\n"
				+ "\n"
				+ "\n"
				+ "  AND lower(e.content) LIKE ?\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "ORDER BY e.last_modified_date DESC\n".trim());
		final ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		final List<SqlParameter> declaredParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, params);
		final Object[] buildValueArray = NamedParameterUtils.buildValueArray(parsedSql, params, declaredParameters);
		assertThat(buildValueArray).hasSize(1);
		assertThat(((SqlParameterValue) buildValueArray[0]).getName()).isEqualTo("keywordPattern");
		assertThat(((SqlParameterValue) buildValueArray[0]).getValue()).isEqualTo("%xyz%");
	}

	@Test
	public void entryIdsWithTag() {
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("tag", "foo");
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/entryIds.sql"), params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("SELECT DISTINCT e.entry_id, e.last_modified_date\n"
				+ "FROM entry AS e\n"
				+ "         \n"
				+ "         LEFT JOIN entry_tag AS et ON e.entry_id = et.entry_id\n"
				+ "    \n"
				+ "    \n"
				+ "WHERE 1 = 1\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "  AND et.tag_name = :tag\n"
				+ "\n"
				+ "\n"
				+ "ORDER BY e.last_modified_date DESC\n".trim());
	}

	@Test
	public void entryIdsWithCategoryOrders() {
		final List<CategoryOrder> categoryOrders = new CategoryOrders()
				.add(new Category("x"), 0)
				.add(new Category("y"), 1)
				.add(new Category("z"), 2)
				.getValue().stream().toList();
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("categoryOrders", categoryOrders)
				.addValue("categoryOrder_0_0.category.name", "x")
				.addValue("categoryOrder_0_0.categoryOrder", 0)
				.addValue("categoryOrder_0_1.category.name", "y")
				.addValue("categoryOrder_0_1.categoryOrder", 1)
				.addValue("categoryOrder_0_2.category.name", "z")
				.addValue("categoryOrder_0_2.categoryOrder", 2);
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/entryIds.sql"), params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("SELECT DISTINCT e.entry_id, e.last_modified_date\n"
				+ "FROM entry AS e\n"
				+ "         \n"
				+ "    \n"
				+ "         LEFT JOIN category AS c ON e.entry_id = c.entry_id\n"
				+ "    \n"
				+ "WHERE 1 = 1\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "  AND c.category_name = :categoryOrder_0_0.category.name\n"
				+ "  AND c.category_order = :categoryOrder_0_0.categoryOrder\n"
				+ "  AND c.category_name = :categoryOrder_0_1.category.name\n"
				+ "  AND c.category_order = :categoryOrder_0_1.categoryOrder\n"
				+ "  AND c.category_name = :categoryOrder_0_2.category.name\n"
				+ "  AND c.category_order = :categoryOrder_0_2.categoryOrder\n"
				+ "\n"
				+ "\n"
				+ "ORDER BY e.last_modified_date DESC\n".trim());
		final String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, params);
		assertThat(sqlToUse.trim()).isEqualTo("SELECT DISTINCT e.entry_id, e.last_modified_date\n"
				+ "FROM entry AS e\n"
				+ "         \n"
				+ "    \n"
				+ "         LEFT JOIN category AS c ON e.entry_id = c.entry_id\n"
				+ "    \n"
				+ "WHERE 1 = 1\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "  AND c.category_name = ?\n"
				+ "  AND c.category_order = ?\n"
				+ "  AND c.category_name = ?\n"
				+ "  AND c.category_order = ?\n"
				+ "  AND c.category_name = ?\n"
				+ "  AND c.category_order = ?\n"
				+ "\n"
				+ "\n"
				+ "ORDER BY e.last_modified_date DESC\n".trim());
		final ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		final List<SqlParameter> declaredParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, params);
		final Object[] buildValueArray = NamedParameterUtils.buildValueArray(parsedSql, params, declaredParameters);
		assertThat(buildValueArray).hasSize(6);
		assertThat(((SqlParameterValue) buildValueArray[0]).getName()).isEqualTo("categoryOrder_0_0.category.name");
		assertThat(((SqlParameterValue) buildValueArray[0]).getValue()).isEqualTo("x");
		assertThat(((SqlParameterValue) buildValueArray[1]).getName()).isEqualTo("categoryOrder_0_0.categoryOrder");
		assertThat(((SqlParameterValue) buildValueArray[1]).getValue()).isEqualTo(0);
		assertThat(((SqlParameterValue) buildValueArray[2]).getName()).isEqualTo("categoryOrder_0_1.category.name");
		assertThat(((SqlParameterValue) buildValueArray[2]).getValue()).isEqualTo("y");
		assertThat(((SqlParameterValue) buildValueArray[3]).getName()).isEqualTo("categoryOrder_0_1.categoryOrder");
		assertThat(((SqlParameterValue) buildValueArray[3]).getValue()).isEqualTo(1);
		assertThat(((SqlParameterValue) buildValueArray[4]).getName()).isEqualTo("categoryOrder_0_2.category.name");
		assertThat(((SqlParameterValue) buildValueArray[4]).getValue()).isEqualTo("z");
		assertThat(((SqlParameterValue) buildValueArray[5]).getName()).isEqualTo("categoryOrder_0_2.categoryOrder");
		assertThat(((SqlParameterValue) buildValueArray[5]).getValue()).isEqualTo(2);
	}

	@Test
	void upsertTag() {
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("tagName", "Demo");
		final String sql = sqlGenerator.generate(FileLoader.loadAsString("am/ik/blog/entry/EntryMapper/upsertTag.sql"), params.getValues(), params::addValue);
		assertThat(sql.trim()).isEqualTo("""
				INSERT INTO tag (tag_name)
				VALUES (:tagName)
				ON CONFLICT ON CONSTRAINT tag_pkey DO UPDATE SET tag_name = :tagName
				""".trim());
		final String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, params);
		assertThat(sqlToUse.trim()).isEqualTo("""
				INSERT INTO tag (tag_name)
				VALUES (?)
				ON CONFLICT ON CONSTRAINT tag_pkey DO UPDATE SET tag_name = ?
				""".trim());
		final ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		final List<SqlParameter> declaredParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, params);
		final Object[] buildValueArray = NamedParameterUtils.buildValueArray(parsedSql, params, declaredParameters);
		assertThat(buildValueArray).hasSize(2);
		assertThat(((SqlParameterValue) buildValueArray[0]).getName()).isEqualTo("tagName");
		assertThat(((SqlParameterValue) buildValueArray[0]).getValue()).isEqualTo("Demo");
		assertThat(((SqlParameterValue) buildValueArray[1]).getName()).isEqualTo("tagName");
		assertThat(((SqlParameterValue) buildValueArray[1]).getValue()).isEqualTo("Demo");
	}
}
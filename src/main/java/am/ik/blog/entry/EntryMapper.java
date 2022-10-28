package am.ik.blog.entry;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import am.ik.blog.category.Category;
import am.ik.blog.entry.search.SearchCriteria;
import am.ik.blog.tag.Tag;
import am.ik.blog.util.FileLoader;
import org.mybatis.scripting.thymeleaf.SqlGenerator;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static am.ik.blog.util.FileLoader.loadSqlAsString;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Repository
public class EntryMapper {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	private final SqlGenerator sqlGenerator;


	public EntryMapper(NamedParameterJdbcTemplate jdbcTemplate, SqlGenerator sqlGenerator) {
		this.jdbcTemplate = jdbcTemplate;
		this.sqlGenerator = sqlGenerator;
	}

	static RowMapper<Entry> rowMapper(boolean excludeContent, Map<Long, List<Category>> categoriesMap, Map<Long, List<Tag>> tagsMap) {
		return (rs, rowNum) -> {
			final long entryId = rs.getLong("entry_id");
			return new EntryBuilder()
					.withEntryId(entryId)
					.withContent(excludeContent ? "" : rs.getString("content"))
					.withFrontMatter(new FrontMatterBuilder()
							.withTitle(rs.getString("title"))
							.withCategories(categoriesMap.getOrDefault(entryId, List.of()))
							.withTags(tagsMap.getOrDefault(entryId, List.of()))
							.build())
					.withCreated(new Author(rs.getString("created_by"),
							rs.getTimestamp("created_date").toInstant().atOffset(ZoneOffset.UTC)))
					.withUpdated(new Author(rs.getString("last_modified_by"),
							rs.getTimestamp("last_modified_date").toInstant().atOffset(ZoneOffset.UTC)))
					.build();
		};
	}

	public Optional<Entry> findOne(Long entryId, boolean excludeContent) {
		final List<Long> ids = List.of(entryId);
		final Map<Long, List<Category>> categoriesMap = this.categoriesMap(ids);
		final Map<Long, List<Tag>> tagsMap = this.tagsMap(ids);
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("entryId", entryId)
				.addValue("excludeContent", excludeContent);
		final String sql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/findOne.sql"), params.getValues(), params::addValue);
		try {
			final Entry entry = this.jdbcTemplate.queryForObject(sql, params, rowMapper(excludeContent, categoriesMap, tagsMap));
			return Optional.ofNullable(entry);
		}
		catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public List<Entry> findAll(SearchCriteria searchCriteria, Pageable pageable) {
		final List<Long> ids = this.entryIds(searchCriteria, pageable);
		final Map<Long, List<Category>> categoriesMap = this.categoriesMap(ids);
		final Map<Long, List<Tag>> tagsMap = this.tagsMap(ids);
		final MapSqlParameterSource params = entryIdsParameterSource(ids);
		final String sql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/findAll.sql"), params.getValues(), params::addValue);
		return this.jdbcTemplate.query(sql, params, rowMapper(true, categoriesMap, tagsMap));
	}

	public Page<Entry> findPage(SearchCriteria searchCriteria, Pageable pageable) {
		final List<Entry> content = this.findAll(searchCriteria, pageable);
		final long total = this.count(searchCriteria);
		return new PageImpl<>(content, pageable, total);
	}

	public long count(SearchCriteria searchCriteria) {
		final MapSqlParameterSource params = searchCriteria.toParameterSource();
		final String sql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/count.sql"), params.getValues(), params::addValue);
		final Long count = this.jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> rs.getLong("count"));
		return Objects.<Long>requireNonNullElse(count, 0L);
	}

	@Transactional
	public int delete(Long entryId) {
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("entryId", entryId);
		final String sql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/deleteEntry.sql"), params.getValues(), params::addValue);
		return this.jdbcTemplate.update(sql, params);
	}

	@Transactional
	public Map<String, Integer> save(Entry entry) {
		final Map<String, Integer> result = new LinkedHashMap<>();
		final FrontMatter frontMatter = entry.getFrontMatter();
		final Author created = entry.getCreated();
		final Author updated = entry.getUpdated();
		final Long entryId = entry.getEntryId();
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("entryId", entryId)
				.addValue("title", frontMatter.getTitle())
				.addValue("content", entry.getContent())
				.addValue("createdBy", created.getName())
				.addValue("createdDate", Timestamp.from(created.getDate().toInstant()))
				.addValue("lastModifiedBy", updated.getName())
				.addValue("lastModifiedDate", Timestamp.from(updated.getDate().toInstant()));
		final String upsertEntrySql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/upsertEntry.sql"), params.getValues(), params::addValue);
		final int upsertEntryCount = this.jdbcTemplate.update(upsertEntrySql, params);
		result.put("upsertEntry", upsertEntryCount);
		final String deleteCategorySql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/deleteCategory.sql"), params.getValues(), params::addValue);
		final int deleteCategoryCount = this.jdbcTemplate.update(deleteCategorySql, params);
		result.put("deleteCategory", deleteCategoryCount);
		final String insertCategorySql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/insertCategory.sql"), params.getValues(), params::addValue);
		final List<Category> categories = frontMatter.getCategories();
		final SqlParameterSource[] insertCategoryParams = new SqlParameterSource[categories.size()];
		for (int i = 0; i < categories.size(); i++) {
			final Category category = categories.get(i);
			insertCategoryParams[i] = new MapSqlParameterSource()
					.addValue("entryId", entryId)
					.addValue("categoryName", category.name())
					.addValue("categoryOrder", i);
		}
		final int[] insertCategoryCount = this.jdbcTemplate.batchUpdate(insertCategorySql, insertCategoryParams);
		result.put("insertCategory", Arrays.stream(insertCategoryCount).sum());
		final String deleteEntryTagSql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/deleteEntryTag.sql"), params.getValues(), params::addValue);
		final int deleteEntryTagCount = this.jdbcTemplate.update(deleteEntryTagSql, params);
		result.put("deleteEntryTag", deleteEntryTagCount);
		final String upsertTagSql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/upsertTag.sql"), params.getValues(), params::addValue);
		final String insertEntryTagSql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/insertEntryTag.sql"), params.getValues(), params::addValue);
		final List<Tag> tags = frontMatter.getTags();
		final SqlParameterSource[] tagParams = new SqlParameterSource[tags.size()];
		for (int i = 0; i < tags.size(); i++) {
			final Tag tag = tags.get(i);
			tagParams[i] = new MapSqlParameterSource()
					.addValue("entryId", entryId)
					.addValue("tagName", tag.name());
		}
		final int[] upsertTagCount = this.jdbcTemplate.batchUpdate(upsertTagSql, tagParams);
		result.put("upsertTag", Arrays.stream(upsertTagCount).sum());
		final int[] insertEntryTagCount = this.jdbcTemplate.batchUpdate(insertEntryTagSql, tagParams);
		result.put("insertEntryTag", Arrays.stream(insertEntryTagCount).sum());
		return result;
	}

	private List<Long> entryIds(SearchCriteria searchCriteria, Pageable pageable) {
		final MapSqlParameterSource params = searchCriteria.toParameterSource();
		final String sql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/entryIds.sql"), params.getValues(), params::addValue)
				+ " LIMIT %d OFFSET %d".formatted(pageable.getPageSize(), pageable.getOffset());
		return this.jdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getLong("entry_id"));
	}

	private Map<Long, List<Tag>> tagsMap(List<Long> ids) {
		final MapSqlParameterSource params = entryIdsParameterSource(ids);
		final String sql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/tagsMap.sql"), params.getValues(), params::addValue);
		final List<Tuple2<Long, Tag>> list = this.jdbcTemplate.query(sql, params,
				(rs, rowNum) -> Tuples.of(rs.getLong("entry_id"), new Tag(rs.getString("tag_name"))));
		return aggregateByKey(list);
	}

	private Map<Long, List<Category>> categoriesMap(List<Long> ids) {
		final MapSqlParameterSource params = entryIdsParameterSource(ids);
		final String sql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/categoriesMap.sql"), params.getValues(), params::addValue);
		final List<Tuple2<Long, Category>> list = this.jdbcTemplate.query(sql, params,
				(rs, rowNum) -> Tuples.of(rs.getLong("entry_id"), new Category(rs.getString("category_name"))));
		return aggregateByKey(list);
	}

	private static <K, V> Map<K, List<V>> aggregateByKey(List<Tuple2<K, V>> list) {
		return list.stream()
				.collect(groupingBy(Tuple2::getT1))
				.entrySet()
				.stream()
				.map(e -> Tuples.of(e.getKey(), e.getValue()
						.stream()
						.map(Tuple2::getT2)
						.collect(toList())))
				.collect(toMap(Tuple2::getT1, Tuple2::getT2));
	}

	private static MapSqlParameterSource entryIdsParameterSource(List<Long> ids) {
		final MapSqlParameterSource params = new MapSqlParameterSource().addValue("entryIds", ids);
		for (int i = 0; i < ids.size(); i++) {
			params.addValue("entryIds[%d]".formatted(i), ids.get(i));
		}
		return params;
	}
}

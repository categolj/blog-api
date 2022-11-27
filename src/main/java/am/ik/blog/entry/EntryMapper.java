package am.ik.blog.entry;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import am.ik.blog.category.Category;
import am.ik.blog.entry.search.SearchCriteria;
import am.ik.blog.tag.Tag;
import am.ik.yavi.core.ConstraintViolationsException;
import org.mybatis.scripting.thymeleaf.SqlGenerator;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static am.ik.blog.util.FileLoader.loadSqlAsString;
import static java.util.stream.Collectors.joining;

@Repository
public class EntryMapper {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	private final SqlGenerator sqlGenerator;

	private final RowMapper<Entry> rowMapper = (rs, rowNum) -> {
		final long entryId = rs.getLong("entry_id");
		return new EntryBuilder().withEntryId(entryId)
				.withContent(rs.getString("content"))
				.withFrontMatter(new FrontMatterBuilder().withTitle(rs.getString("title"))
						.withCategories(Arrays
								.stream((Object[]) rs.getArray("categories").getArray())
								.map(String.class::cast).map(Category::new).toList())
						.withTags(Arrays.stream((Object[]) rs.getArray("tags").getArray())
								.map(String.class::cast).sorted().map(Tag::new).toList())
						.build())
				.withCreated(new Author(rs.getString("created_by"),
						rs.getTimestamp("created_date").toInstant()
								.atOffset(ZoneOffset.UTC)))
				.withUpdated(new Author(rs.getString("last_modified_by"),
						rs.getTimestamp("last_modified_date").toInstant()
								.atOffset(ZoneOffset.UTC)))
				.build();
	};

	public EntryMapper(NamedParameterJdbcTemplate jdbcTemplate,
			SqlGenerator sqlGenerator) {
		this.jdbcTemplate = jdbcTemplate;
		this.sqlGenerator = sqlGenerator;
	}

	@Transactional(readOnly = true)
	public Optional<Entry> findOne(Long entryId, boolean excludeContent) {
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("entryId", entryId).addValue("excludeContent", excludeContent);
		final String sql = this.sqlGenerator.generate(
				loadSqlAsString("am/ik/blog/entry/EntryMapper/findOne.sql"),
				params.getValues(), params::addValue);
		try {
			final Entry entry = this.jdbcTemplate.queryForObject(sql, params, rowMapper);
			return Optional.ofNullable(entry);
		}
		catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Transactional(readOnly = true)
	public List<Entry> findAll(SearchCriteria searchCriteria, Pageable pageable) {
		final List<Long> ids = this.entryIds(searchCriteria, pageable);
		if (ids.isEmpty()) {
			return List.of();
		}
		final MapSqlParameterSource params = entryIdsParameterSource(ids)
				.addValue("excludeContent", searchCriteria.isExcludeContent());
		final String sql = this.sqlGenerator.generate(
				loadSqlAsString("am/ik/blog/entry/EntryMapper/findAll.sql"),
				params.getValues(), params::addValue);
		return this.jdbcTemplate.query(sql, params, rowMapper);
	}

	@Transactional(readOnly = true)
	public Page<Entry> findPage(SearchCriteria searchCriteria, Pageable pageable) {
		final List<Entry> content = this.findAll(searchCriteria, pageable);
		final long total = this.count(searchCriteria);
		return new PageImpl<>(content, pageable, total);
	}

	public long count(SearchCriteria searchCriteria) {
		final MapSqlParameterSource params = searchCriteria.toParameterSource();
		final String sql = this.sqlGenerator.generate(
				loadSqlAsString("am/ik/blog/entry/EntryMapper/count.sql"),
				params.getValues(), params::addValue);
		final Long count = this.jdbcTemplate.queryForObject(sql, params,
				(rs, rowNum) -> rs.getLong("count"));
		return Objects.<Long> requireNonNullElse(count, 0L);
	}

	public long nextId() {
		final String sql = loadSqlAsString("am/ik/blog/entry/EntryMapper/nextId.sql");
		final Long nextId = this.jdbcTemplate.queryForObject(sql, Map.of(),
				(rs, i) -> rs.getLong("next"));
		return Objects.<Long> requireNonNullElse(nextId, -1L);
	}

	@Transactional
	public int delete(Long entryId) {
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("entryId", entryId);
		final String sql = this.sqlGenerator.generate(
				loadSqlAsString("am/ik/blog/entry/EntryMapper/deleteEntry.sql"),
				params.getValues(), params::addValue);
		return this.jdbcTemplate.update(sql, params);
	}

	@Transactional
	public Map<String, Integer> save(Entry entry) {
		Entry.validator.validate(entry)
				.throwIfInvalid(ConstraintViolationsException::new);
		final Map<String, Integer> result = new LinkedHashMap<>();
		final FrontMatter frontMatter = entry.getFrontMatter();
		final Author created = entry.getCreated();
		final Author updated = entry.getUpdated();
		final Long entryId = entry.getEntryId();
		final List<Category> categories = frontMatter.getCategories();
		final List<Tag> tags = frontMatter.getTags();
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("entryId", entryId).addValue("title", frontMatter.getTitle())
				.addValue("content", entry.getContent())
				.addValue("categories",
						categories.stream().map(Category::name).collect(joining(",")))
				.addValue("tags", tags.stream().map(Tag::name).collect(joining(",")))
				.addValue("createdBy", created.getName())
				.addValue("createdDate", Timestamp.from(created.getDate().toInstant()))
				.addValue("lastModifiedBy", updated.getName())
				.addValue("lastModifiedDate",
						Timestamp.from(updated.getDate().toInstant()));
		final String upsertEntrySql = this.sqlGenerator.generate(
				loadSqlAsString("am/ik/blog/entry/EntryMapper/upsertEntry.sql"),
				params.getValues(), params::addValue);
		final int upsertEntryCount = this.jdbcTemplate.update(upsertEntrySql, params);
		result.put("upsertEntry", upsertEntryCount);
		return result;
	}

	private List<Long> entryIds(SearchCriteria searchCriteria, Pageable pageable) {
		final MapSqlParameterSource params = searchCriteria.toParameterSource();
		final String sql = this.sqlGenerator.generate(
				loadSqlAsString("am/ik/blog/entry/EntryMapper/entryIds.sql"),
				params.getValues(), params::addValue)
				+ " LIMIT %d OFFSET %d".formatted(pageable.getPageSize(),
						pageable.getOffset());
		return this.jdbcTemplate.query(sql, params,
				(rs, rowNum) -> rs.getLong("entry_id"));
	}

	private static MapSqlParameterSource entryIdsParameterSource(List<Long> ids) {
		if (ids.isEmpty()) {
			return new MapSqlParameterSource();
		}
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("entryIds", ids);
		for (int i = 0; i < ids.size(); i++) {
			params.addValue("entryIds[%d]".formatted(i), ids.get(i));
		}
		return params;
	}
}

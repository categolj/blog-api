package am.ik.blog.entry;

import java.io.UncheckedIOException;
import java.sql.Array;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import am.ik.blog.category.Category;
import am.ik.blog.entry.keyword.KeywordParser;
import am.ik.blog.entry.search.SearchCriteria;
import am.ik.blog.tag.Tag;
import am.ik.pagination.CursorPage;
import am.ik.pagination.CursorPageRequest;
import am.ik.pagination.OffsetPage;
import am.ik.pagination.OffsetPageRequest;
import am.ik.yavi.core.ConstraintViolationsException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mybatis.scripting.thymeleaf.SqlGenerator;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static am.ik.blog.util.FileLoader.loadAsString;
import static am.ik.blog.util.FileLoader.loadSqlAsString;
import static java.util.stream.Collectors.joining;

@Repository
public class EntryMapper {

	private final JdbcClient jdbcClient;

	private ObjectMapper objectMapper;

	private final SqlGenerator sqlGenerator;

	private final KeywordParser keywordParser;

	private final RowMapper<Entry> rowMapper = (rs, rowNum) -> {
		final long entryId = rs.getLong("entry_id");
		final OffsetDateTime createdDate = rs.getObject("created_date", OffsetDateTime.class);
		final OffsetDateTime lastModifiedDate = rs.getObject("last_modified_date", OffsetDateTime.class);
		final Array categories = rs.getArray("categories");
		final String tags = rs.getString("tags");
		try {
			return new EntryBuilder().withEntryId(entryId)
				.withContent(rs.getString("content"))
				.withFrontMatter(new FrontMatterBuilder().withTitle(rs.getString("title"))
					.withCategories(categories == null ? Collections.emptyList()
							: Arrays.stream((Object[]) categories.getArray())
								.map(String.class::cast)
								.map(Category::new)
								.toList())
					.withTags(tags == null ? Collections.emptyList()
							: this.objectMapper.readValue(tags, new TypeReference<>() {
							}))
					.build())
				.withCreated(new Author(rs.getString("created_by"), createdDate))
				.withUpdated(new Author(rs.getString("last_modified_by"), lastModifiedDate))
				.build();
		}
		catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	};

	public EntryMapper(JdbcClient jdbcClient, ObjectMapper objectMapper, SqlGenerator sqlGenerator,
			KeywordParser keywordParser) {
		this.jdbcClient = jdbcClient;
		this.objectMapper = objectMapper;
		this.sqlGenerator = sqlGenerator;
		this.keywordParser = keywordParser;
	}

	@Transactional(readOnly = true)
	public Optional<Entry> findOne(Long entryId, @Nullable String tenantId, boolean excludeContent) {
		final MapSqlParameterSource params = new MapSqlParameterSource().addValue("entryId", entryId)
			.addValue("tenantId", tenantId)
			.addValue("excludeContent", excludeContent);
		final String sql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/findOne.sql"),
				params.getValues(), params::addValue);
		return this.jdbcClient.sql(sql).paramSource(params).query(rowMapper).optional();
	}

	@Transactional(readOnly = true)
	public List<Entry> findAll(SearchCriteria searchCriteria, @Nullable String tenantId,
			OffsetPageRequest pageRequest) {
		final List<Long> ids = this.entryIds(searchCriteria, tenantId, pageRequest);
		if (ids.isEmpty()) {
			return List.of();
		}
		final MapSqlParameterSource params = entryIdsParameterSource(ids)
			.addValue("excludeContent", searchCriteria.isExcludeContent())
			.addValue("tenantId", tenantId);
		final String sql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/findAll.sql"),
				params.getValues(), params::addValue);
		return this.jdbcClient.sql(sql).paramSource(params).query(rowMapper).list();
	}

	@Transactional(readOnly = true)
	public OffsetPage<Entry> findPage(SearchCriteria searchCriteria, @Nullable String tenantId,
			OffsetPageRequest pageRequest) {
		final List<Entry> content = this.findAll(searchCriteria, tenantId, pageRequest);
		final long total = this.count(searchCriteria, tenantId);
		return new OffsetPage<>(content, pageRequest.pageSize(), pageRequest.pageNumber(), total);
	}

	@Transactional(readOnly = true)
	public CursorPage<Entry, Instant> findPage(SearchCriteria searchCriteria, @Nullable String tenantId,
			CursorPageRequest<Instant> pageRequest) {
		final Optional<Instant> cursor = pageRequest.cursorOptional();
		final int pageSizePlus1 = pageRequest.pageSize() + 1;
		final MapSqlParameterSource params = searchCriteria.toParameterSource(this.keywordParser)
			.addValue("tenantId", tenantId)
			.addValue("cursor", cursor.map(instant -> instant.atOffset(ZoneOffset.UTC)).orElse(null));
		final String sql = "%s LIMIT %d"
			.formatted(this.sqlGenerator.generate(loadAsString("am/ik/blog/entry/EntryMapper/findAllCursorNext.sql"),
					params.getValues(), params::addValue), pageSizePlus1);
		final List<Entry> contentPlus1 = this.jdbcClient.sql(sql).paramSource(params).query(this.rowMapper).list();
		final boolean hasPrevious = cursor.isPresent();
		final boolean hasNext = contentPlus1.size() == pageSizePlus1;
		final List<Entry> content = hasNext ? contentPlus1.subList(0, pageRequest.pageSize()) : contentPlus1;
		return new CursorPage<>(content, pageRequest.pageSize(), entry -> {
			OffsetDateTime updated = entry.getUpdated().date();
			if (updated == null) {
				return null;
			}
			return updated.toInstant();
		}, hasPrevious, hasNext);
	}

	public long count(SearchCriteria searchCriteria, @Nullable String tenantId) {
		final MapSqlParameterSource params = searchCriteria.toParameterSource(this.keywordParser)
			.addValue("tenantId", tenantId);
		final String sql = this.sqlGenerator.generate(loadAsString("am/ik/blog/entry/EntryMapper/count.sql"),
				params.getValues(), params::addValue);
		final Long count = this.jdbcClient.sql(sql) //
			.paramSource(params) //
			.query((rs, rowNum) -> rs.getLong("count")) //
			.single();
		return Objects.<Long>requireNonNullElse(count, 0L);
	}

	public long nextId(@Nullable String tenantId) {
		final MapSqlParameterSource params = new MapSqlParameterSource().addValue("tenantId", tenantId);
		final String sql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/nextId.sql"),
				params.getValues(), params::addValue);
		final Long nextId = this.jdbcClient.sql(sql) //
			.query((rs, i) -> rs.getLong("next"))
			.single();
		return Objects.<Long>requireNonNullElse(nextId, 1L);
	}

	@Transactional
	public int delete(Long entryId, @Nullable String tenantId) {
		final MapSqlParameterSource params = new MapSqlParameterSource().addValue("entryId", entryId)
			.addValue("tenantId", tenantId);
		final String sql = this.sqlGenerator.generate(loadSqlAsString("am/ik/blog/entry/EntryMapper/deleteEntry.sql"),
				params.getValues(), params::addValue);
		return this.jdbcClient.sql(sql).paramSource(params).update();
	}

	@Transactional
	public Map<String, Integer> save(Entry entry, @Nullable String tenantId) {
		Entry.validator.validate(entry).throwIfInvalid(ConstraintViolationsException::new);
		final Map<String, Integer> result = new LinkedHashMap<>();
		final FrontMatter frontMatter = entry.getFrontMatter();
		final Author created = entry.getCreated();
		final Author updated = entry.getUpdated();
		final Long entryId = entry.getEntryId();
		final List<Category> categories = frontMatter.categories();
		final List<Tag> tags = frontMatter.tags();
		try {
			final MapSqlParameterSource params = new MapSqlParameterSource().addValue("entryId", entryId)
				.addValue("title", frontMatter.title())
				.addValue("tenantId", tenantId)
				.addValue("content", entry.getContent())
				.addValue("categories", categories.stream().map(Category::name).collect(joining(",")))
				.addValue("tags", this.objectMapper.writeValueAsString(tags))
				.addValue("createdBy", created.name())
				.addValue("createdDate", created.date())
				.addValue("lastModifiedBy", updated.name())
				.addValue("lastModifiedDate", updated.date());
			final String upsertEntrySql = this.sqlGenerator.generate(
					loadSqlAsString("am/ik/blog/entry/EntryMapper/upsertEntry.sql"), params.getValues(),
					params::addValue);
			final int upsertEntryCount = this.jdbcClient.sql(upsertEntrySql) //
				.paramSource(params) //
				.update();
			result.put("upsertEntry", upsertEntryCount);
			return result;
		}
		catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}

	private List<Long> entryIds(SearchCriteria searchCriteria, @Nullable String tenantId,
			OffsetPageRequest pageRequest) {
		final MapSqlParameterSource params = searchCriteria.toParameterSource(this.keywordParser)
			.addValue("tenantId", tenantId);
		final String sql = this.sqlGenerator.generate(loadAsString("am/ik/blog/entry/EntryMapper/entryIds.sql"),
				params.getValues(), params::addValue)
				+ " LIMIT %d OFFSET %d".formatted(pageRequest.pageSize(), pageRequest.offset());
		return this.jdbcClient.sql(sql) //
			.paramSource(params) //
			.query((rs, rowNum) -> rs.getLong("entry_id")) //
			.list();
	}

	private static MapSqlParameterSource entryIdsParameterSource(List<Long> ids) {
		if (ids.isEmpty()) {
			return new MapSqlParameterSource();
		}
		final MapSqlParameterSource params = new MapSqlParameterSource().addValue("entryIds", ids);
		for (int i = 0; i < ids.size(); i++) {
			params.addValue("entryIds[%d]".formatted(i), ids.get(i));
		}
		return params;
	}

}

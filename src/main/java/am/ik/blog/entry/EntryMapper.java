package am.ik.blog.entry;

import am.ik.blog.category.Category;
import am.ik.blog.entry.keyword.KeywordExtractor;
import am.ik.blog.entry.search.SearchCriteria;
import am.ik.blog.tag.Tag;
import am.ik.pagination.CursorPage;
import am.ik.pagination.CursorPageRequest;
import am.ik.pagination.OffsetPage;
import am.ik.pagination.OffsetPageRequest;
import am.ik.yavi.core.ConstraintViolationsException;
import org.mybatis.scripting.thymeleaf.SqlGenerator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static am.ik.blog.util.FileLoader.loadSqlAsString;
import static java.util.stream.Collectors.joining;

@Repository
public class EntryMapper {
	private final JdbcClient jdbcClient;

	private final SqlGenerator sqlGenerator;

	private final KeywordExtractor keywordExtractor;

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

	public EntryMapper(NamedParameterJdbcTemplate jdbcTemplate, SqlGenerator sqlGenerator,
			KeywordExtractor keywordExtractor) {
		this.jdbcClient = JdbcClient.create(jdbcTemplate);
		this.sqlGenerator = sqlGenerator;
		this.keywordExtractor = keywordExtractor;
	}

	@Transactional(readOnly = true)
	public Optional<Entry> findOne(Long entryId, String tenantId,
			boolean excludeContent) {
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("entryId", entryId).addValue("tenantId", tenantId)
				.addValue("excludeContent", excludeContent);
		final String sql = this.sqlGenerator.generate(
				loadSqlAsString("am/ik/blog/entry/EntryMapper/findOne.sql"),
				params.getValues(), params::addValue);
		return this.jdbcClient.sql(sql).paramSource(params).query(rowMapper).optional();
	}

	@Transactional(readOnly = true)
	public List<Entry> findAll(SearchCriteria searchCriteria, String tenantId,
			OffsetPageRequest pageRequest) {
		final List<Long> ids = this.entryIds(searchCriteria, tenantId, pageRequest);
		if (ids.isEmpty()) {
			return List.of();
		}
		final MapSqlParameterSource params = entryIdsParameterSource(ids)
				.addValue("excludeContent", searchCriteria.isExcludeContent())
				.addValue("tenantId", tenantId);
		final String sql = this.sqlGenerator.generate(
				loadSqlAsString("am/ik/blog/entry/EntryMapper/findAll.sql"),
				params.getValues(), params::addValue);
		return this.jdbcClient.sql(sql).paramSource(params).query(rowMapper).list();
	}

	@Transactional(readOnly = true)
	public OffsetPage<Entry> findPage(SearchCriteria searchCriteria, String tenantId,
			OffsetPageRequest pageRequest) {
		final List<Entry> content = this.findAll(searchCriteria, tenantId, pageRequest);
		final long total = this.count(searchCriteria, tenantId);
		return new OffsetPage<>(content, pageRequest.pageSize(), pageRequest.pageNumber(),
				total);
	}

	@Transactional(readOnly = true)
	public CursorPage<Entry, Instant> findPage(SearchCriteria searchCriteria,
			String tenantId, CursorPageRequest<Instant> pageRequest) {
		final Optional<Instant> cursor = pageRequest.cursorOptional();
		final int pageSizePlus1 = pageRequest.pageSize() + 1;
		final MapSqlParameterSource params = searchCriteria
				.toParameterSource(this.keywordExtractor).addValue("tenantId", tenantId)
				.addValue("cursor", cursor.map(Timestamp::from).orElse(null));
		final String sql = "%s LIMIT %d".formatted(this.sqlGenerator.generate(
				loadSqlAsString("am/ik/blog/entry/EntryMapper/findAllCursorNext.sql"),
				params.getValues(), params::addValue), pageSizePlus1);
		final List<Entry> contentPlus1 = this.jdbcClient.sql(sql).paramSource(params)
				.query(this.rowMapper).list();
		final boolean hasPrevious = cursor.isPresent();
		final boolean hasNext = contentPlus1.size() == pageSizePlus1;
		final List<Entry> content = hasNext
				? contentPlus1.subList(0, pageRequest.pageSize())
				: contentPlus1;
		return new CursorPage<>(content, pageRequest.pageSize(),
				entry -> entry.getUpdated().getDate().toInstant(), hasPrevious, hasNext);
	}

	public long count(SearchCriteria searchCriteria, String tenantId) {
		final MapSqlParameterSource params = searchCriteria
				.toParameterSource(this.keywordExtractor).addValue("tenantId", tenantId);
		final String sql = this.sqlGenerator.generate(
				loadSqlAsString("am/ik/blog/entry/EntryMapper/count.sql"),
				params.getValues(), params::addValue);
		final Long count = this.jdbcClient.sql(sql) //
				.paramSource(params) //
				.query((rs, rowNum) -> rs.getLong("count")) //
				.single();
		return Objects.<Long> requireNonNullElse(count, 0L);
	}

	public long nextId(String tenantId) {
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("tenantId", tenantId);
		final String sql = this.sqlGenerator.generate(
				loadSqlAsString("am/ik/blog/entry/EntryMapper/nextId.sql"),
				params.getValues(), params::addValue);
		final Long nextId = this.jdbcClient.sql(sql) //
				.query((rs, i) -> rs.getLong("next")).single();
		return Objects.<Long> requireNonNullElse(nextId, 1L);
	}

	@Transactional
	public int delete(Long entryId, String tenantId) {
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("entryId", entryId).addValue("tenantId", tenantId);
		final String sql = this.sqlGenerator.generate(
				loadSqlAsString("am/ik/blog/entry/EntryMapper/deleteEntry.sql"),
				params.getValues(), params::addValue);
		return this.jdbcClient.sql(sql).paramSource(params).update();
	}

	@Transactional
	public Map<String, Integer> save(Entry entry, String tenantId) {
		Entry.validator.validate(entry)
				.throwIfInvalid(ConstraintViolationsException::new);
		final Map<String, Integer> result = new LinkedHashMap<>();
		final FrontMatter frontMatter = entry.getFrontMatter();
		final Author created = entry.getCreated();
		final Author updated = entry.getUpdated();
		final Long entryId = entry.getEntryId();
		final List<Category> categories = frontMatter.getCategories();
		final List<Tag> tags = frontMatter.getTags();
		final List<String> keywords = this.keywordExtractor.extract(entry.getContent());
		final MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("entryId", entryId).addValue("title", frontMatter.getTitle())
				.addValue("tenantId", tenantId).addValue("content", entry.getContent())
				.addValue("categories",
						categories.stream().map(Category::name).collect(joining(",")))
				.addValue("tags", tags.stream().map(Tag::name).collect(joining(",")))
				.addValue("keywords", String.join(",", keywords))
				.addValue("createdBy", created.getName())
				.addValue("createdDate", Timestamp.from(created.getDate().toInstant()))
				.addValue("lastModifiedBy", updated.getName())
				.addValue("lastModifiedDate",
						Timestamp.from(updated.getDate().toInstant()));
		final String upsertEntrySql = this.sqlGenerator.generate(
				loadSqlAsString("am/ik/blog/entry/EntryMapper/upsertEntry.sql"),
				params.getValues(), params::addValue);
		final int upsertEntryCount = this.jdbcClient.sql(upsertEntrySql) //
				.paramSource(params) //
				.update();
		result.put("upsertEntry", upsertEntryCount);
		return result;
	}

	private List<Long> entryIds(SearchCriteria searchCriteria, String tenantId,
			OffsetPageRequest pageRequest) {
		final MapSqlParameterSource params = searchCriteria
				.toParameterSource(this.keywordExtractor).addValue("tenantId", tenantId);
		final String sql = this.sqlGenerator.generate(
				loadSqlAsString("am/ik/blog/entry/EntryMapper/entryIds.sql"),
				params.getValues(), params::addValue)
				+ " LIMIT %d OFFSET %d".formatted(pageRequest.pageSize(),
						pageRequest.offset());
		return this.jdbcClient.sql(sql) //
				.paramSource(params) //
				.query((rs, rowNum) -> rs.getLong("entry_id")) //
				.list();
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

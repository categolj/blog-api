package am.ik.blog.entry;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import am.ik.blog.entry.criteria.SearchCriteria;
import io.r2dbc.spi.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class EntryMapper {
	private final DatabaseClient databaseClient;
	private final TransactionalOperator transactionalOperator;
	private final Logger log = LoggerFactory.getLogger(EntryMapper.class);

	public EntryMapper(DatabaseClient databaseClient,
			TransactionalOperator transactionalOperator) {
		this.databaseClient = databaseClient;
		this.transactionalOperator = transactionalOperator;
	}

	public Mono<Long> count(SearchCriteria criteria) {
		SearchCriteria.ClauseAndParams clauseAndParams = criteria.toWhereClause();
		String sql = String.format(
				"SELECT count(e.entry_id) AS count FROM entry AS e %s WHERE 1=1 %s",
				criteria.toJoinClause(), clauseAndParams.clauseForEntryId());
		DatabaseClient.GenericExecuteSpec executeSpec = this.databaseClient.execute(sql);
		Map<String, Object> params = clauseAndParams.params();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			executeSpec = executeSpec.bind(entry.getKey(), entry.getValue());
		}
		return executeSpec //
				.map((row, meta) -> row.get("count", Long.class)) //
				.one();
	}

	public Mono<List<Entry>> findAll(SearchCriteria criteria, Pageable pageable) {
		return this.collectAll(criteria, pageable).collectList();
	}

	public Mono<Page<Entry>> findPage(SearchCriteria criteria, Pageable pageable) {
		return this.count(criteria)
				.zipWith(this.findAll(criteria, pageable)
						.switchIfEmpty(Mono.fromCallable(Collections::emptyList))) //
				.doOnError(e -> log.error("Failed to find a page!", e))
				.map(tpl -> new PageImpl<>(tpl.getT2(), pageable, tpl.getT1()));
	}

	public Mono<Entry> findOne(EntryId entryId, boolean excludeContent) {
		List<Long> ids = Collections.singletonList(entryId.getValue());
		Mono<Tags> tagsMono = this.tagsMap(ids)
				.map(x -> x.getOrDefault(entryId, new Tags()));
		Mono<Categories> categoriesMono = this.categoriesMap(ids)
				.map(x -> x.get(entryId));
		String sql = String.format(
				"SELECT e.entry_id, e.title%s, e.created_by, e.created_date, e.last_modified_by, e.last_modified_date FROM entry AS e  WHERE e.entry_id = $1",
				(excludeContent ? "" : ", e.content"));
		Mono<Entry> entryMono = this.databaseClient.execute(sql) //
				.bind("$1", entryId.getValue()) //
				.as(Entry.class) //
				.map((row, meta) -> this.mapRow(row, excludeContent)) //
				.one();
		return entryMono.flatMap(entry -> Mono.zip(categoriesMono, tagsMono) //
				.map(tpl -> {
					FrontMatter fm = entry.getFrontMatter();
					return entry.copy() //
							.frontMatter(new FrontMatter(fm.title(), tpl.getT1(),
									tpl.getT2(), fm.date(), fm.updated()))
							.build();
				})) //
				.doOnError(e -> log.error("Failed to fetch an entry!", e));
	}

	public Mono<EventTime> findLastModifiedDate(EntryId entryId) {
		return this.databaseClient
				.execute("SELECT last_modified_date FROM entry WHERE entry_id = $1") //
				.bind("$1", entryId.getValue()) //
				.map((row, meta) -> new EventTime(
						row.get("last_modified_date", OffsetDateTime.class)))
				.one();
	}

	public Mono<EventTime> findLatestModifiedDate() {
		return this.databaseClient.execute(
				"SELECT last_modified_date FROM entry ORDER BY last_modified_date DESC LIMIT 1") //
				.map((row, meta) -> new EventTime(
						row.get("last_modified_date", OffsetDateTime.class)))
				.one();
	}

	public Flux<Entry> collectAll(SearchCriteria criteria, Pageable pageable) {
		SearchCriteria.ClauseAndParams clauseAndParams = criteria.toWhereClause();
		return this.entryIds(criteria, pageable, clauseAndParams) //
				.collectList() //
				.flatMapMany(ids -> this.tagsMap(ids) //
						.zipWith(this.categoriesMap(ids)) //
						.flatMapMany(tpl -> {
							Map<EntryId, Tags> tagsMap = tpl.getT1();
							Map<EntryId, Categories> categoriesMap = tpl.getT2();
							String sql = String.format(
									"SELECT e.entry_id, e.title, e.created_by, e.created_date, e.last_modified_by, e.last_modified_date FROM entry AS e WHERE e.entry_id IN (%s) ORDER BY e.last_modified_date DESC",
									IntStream.rangeClosed(1, ids.size())
											.mapToObj(i -> "$" + i)
											.collect(Collectors.joining(",")));
							DatabaseClient.GenericExecuteSpec executeSpec = this.databaseClient
									.execute(sql);
							for (int i = 0; i < ids.size(); i++) {
								executeSpec = executeSpec.bind("$" + (i + 1), ids.get(i));
							}
							return executeSpec //
									.as(Entry.class) //
									.map((row, meta) -> this.mapRow(row, true)).all()
									.map(e -> {
										FrontMatter frontMatter = e.getFrontMatter();
										return e.copy()
												.frontMatter(new FrontMatter(
														frontMatter.title(),
														categoriesMap.get(e.entryId()),
														tagsMap.get(e.getEntryId())))
												.build();
									});
						}));
	}

	public Mono<Entry> save(Entry entry) {
		FrontMatter frontMatter = entry.frontMatter();
		Author created = entry.getCreated();
		Author updated = entry.getUpdated();
		Long entryId = entry.entryId().getValue();
		Mono<Integer> upsertEntry = this.databaseClient.execute(
				"INSERT INTO entry (entry_id, title, content, created_by, created_date, last_modified_by, last_modified_date)"
						+ " VALUES (:entry_id, :title, :content, :created_by, :created_date, :last_modified_by, :last_modified_date)"
						+ " ON CONFLICT ON CONSTRAINT entry_pkey" //
						+ " DO UPDATE SET" //
						+ " title = :title2," //
						+ " content = :content2," //
						+ " created_by = :created_by2," //
						+ " created_date = :created_date2," //
						+ " last_modified_by = :last_modified_by2," //
						+ " last_modified_date = :last_modified_date2") //
				.bind("entry_id", entryId) //
				.bind("title", frontMatter.title().getValue()) //
				.bind("content", entry.content().getValue()) //
				.bind("created_by", created.getName().getValue()) //
				.bind("created_date", created.getDate().getValue()) //
				.bind("last_modified_by", updated.getName().getValue()) //
				.bind("last_modified_date", updated.getDate().getValue()) //
				.bind("title2", frontMatter.title().getValue()) //
				.bind("content2", entry.content().getValue()) //
				.bind("created_by2", created.getName().getValue()) //
				.bind("created_date2", created.getDate().getValue()) //
				.bind("last_modified_by2", updated.getName().getValue()) //
				.bind("last_modified_date2", updated.getDate().getValue()) //
				.fetch().rowsUpdated() //
				.log("upsertEntry") //
		;

		Mono<Integer> deleteCategory = this.databaseClient
				.execute("DELETE FROM category WHERE entry_id = $1") //
				.bind("$1", entryId) //
				.fetch().rowsUpdated() //
				.log("deleteCategory") //
		;

		// TODO Batch Update
		AtomicInteger order = new AtomicInteger(0);
		Flux<Integer> insertCategory = Flux
				.fromIterable(frontMatter.categories().getValue())
				.flatMap(category -> this.databaseClient.execute(
						"INSERT INTO category (category_name, category_order, entry_id) VALUES ($1, $2, $3)") //
						.bind("$1", category.getValue()) //
						.bind("$2", order.getAndIncrement()) //
						.bind("$3", entryId) //
						.fetch().rowsUpdated()) //
				.log("insertCategory") //
		;

		Mono<Integer> deleteEntryTag = this.databaseClient
				.execute("DELETE FROM entry_tag WHERE entry_id = $1") //
				.bind("$1", entryId) //
				.fetch().rowsUpdated() //
				.log("deleteEntryTag") //
		;
		// TODO Batch Update
		Flux<Integer> upsertTag = Flux.fromIterable(frontMatter.tags().getValue()) //
				.flatMap(tag -> this.databaseClient
						.execute("INSERT INTO tag (tag_name) VALUES (:tag_name)" //
								+ " ON CONFLICT ON CONSTRAINT tag_pkey" //
								+ " DO UPDATE SET tag_name = :tag_name2") //
						.bind("tag_name", tag.getValue()) //
						.bind("tag_name2", tag.getValue()) //
						.fetch().rowsUpdated()) //
				.log("upsertTag") //
		;
		Flux<Integer> insertEntryTag = Flux.fromIterable(frontMatter.tags().getValue()) //
				.flatMap(tag -> this.databaseClient.execute(
						"INSERT INTO entry_tag (entry_id, tag_name) VALUES ($1, $2)") //
						.bind("$1", entryId) //
						.bind("$2", tag.getValue()) //
						.fetch().rowsUpdated()) //
				.log("insertEntryTag") //
		;
		return upsertEntry //
				.and(deleteCategory.thenMany(insertCategory)) //
				.and(deleteEntryTag.thenMany(upsertTag).thenMany(insertEntryTag)) //
				.as(this.transactionalOperator::transactional) //
				.log("tx") //
				.then(Mono.just(entry));
	}

	public Mono<EntryId> delete(EntryId entryId) {
		return this.databaseClient.execute("DELETE FROM entry WHERE entry_id = $1") //
				.bind("$1", entryId.getValue()) //
				.fetch().rowsUpdated() //
				.as(this.transactionalOperator::transactional) //
				.log("delete") //
				.then(Mono.just(entryId));
	}

	Entry mapRow(Row row, boolean excludeContent) {
		return Entry.builder().entryId(new EntryId(row.get("entry_id", Long.class)))
				.content(new Content(
						excludeContent ? "" : row.get("content", String.class))) //
				.frontMatter(new FrontMatter(new Title(row.get("title", String.class)),
						null, null, EventTime.UNSET, EventTime.UNSET))
				.created(new Author(new Name(row.get("created_by", String.class)),
						new EventTime(row.get("created_date", OffsetDateTime.class)))) //
				.updated(new Author(new Name(row.get("last_modified_by", String.class)),
						new EventTime(
								row.get("last_modified_date", OffsetDateTime.class)))) //
				.build();
	}

	Mono<Map<EntryId, Tags>> tagsMap(List<Long> ids) {
		if (ids.isEmpty()) {
			return Mono.empty();
		}
		String sql = String.format(
				"SELECT entry_id, tag_name FROM entry_tag WHERE entry_id IN (%s)",
				IntStream.rangeClosed(1, ids.size()).mapToObj(i -> "$" + i)
						.collect(Collectors.joining(",")));
		DatabaseClient.GenericExecuteSpec executeSpec = this.databaseClient.execute(sql);
		for (int i = 0; i < ids.size(); i++) {
			executeSpec = executeSpec.bind("$" + (i + 1), ids.get(i));
		}
		return executeSpec
				.map((row, rowMetadata) -> Tuples.of(
						new EntryId(row.get("entry_id", Long.class)),
						new Tag(row.get("tag_name", String.class)))) //
				.all() //
				.collectList() //
				.map(list -> list.stream() //
						.collect(groupingBy(Tuple2::getT1)) //
						.entrySet() //
						.stream() //
						.map(e -> Tuples.of(e.getKey(), new Tags(e.getValue() //
								.stream() //
								.map(Tuple2::getT2) //
								.collect(toList()))))
						.collect(toMap(Tuple2::getT1, Tuple2::getT2)));
	}

	Mono<Map<EntryId, Categories>> categoriesMap(List<Long> ids) {
		if (ids.isEmpty()) {
			return Mono.empty();
		}
		String sql = String.format(
				"SELECT entry_id, category_name FROM category WHERE entry_id IN (%s) ORDER BY category_order ASC",
				IntStream.rangeClosed(1, ids.size()).mapToObj(i -> "$" + i)
						.collect(Collectors.joining(",")));
		DatabaseClient.GenericExecuteSpec executeSpec = this.databaseClient.execute(sql);
		for (int i = 0; i < ids.size(); i++) {
			executeSpec = executeSpec.bind("$" + (i + 1), ids.get(i));
		}
		return executeSpec
				.map((row, rowMetadata) -> Tuples.of(
						new EntryId(row.get("entry_id", Long.class)),
						new Category(row.get("category_name", String.class)))) //
				.all() //
				.collectList() //
				.map(list -> list.stream() //
						.collect(groupingBy(Tuple2::getT1)) //
						.entrySet() //
						.stream() //
						.map(e -> Tuples.of(e.getKey(), new Categories(e.getValue() //
								.stream() //
								.map(Tuple2::getT2) //
								.collect(toList()))))
						.collect(toMap(Tuple2::getT1, Tuple2::getT2)));
	}

	Flux<Long> entryIds(SearchCriteria searchCriteria, Pageable pageable,
			SearchCriteria.ClauseAndParams clauseAndParams) {
		String sql = String.format(
				"SELECT e.entry_id FROM entry AS e %s WHERE 1=1 %s ORDER BY e.last_modified_date DESC LIMIT %d OFFSET %d",
				searchCriteria.toJoinClause(), clauseAndParams.clauseForEntryId(),
				pageable.getPageSize(), pageable.getOffset());
		DatabaseClient.GenericExecuteSpec executeSpec = this.databaseClient.execute(sql);
		Map<String, Object> params = clauseAndParams.params();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			executeSpec = executeSpec.bind(entry.getKey(), entry.getValue());
		}
		return executeSpec.map((row, meta) -> row.get("entry_id", Long.class)) //
				.all();
	}
}

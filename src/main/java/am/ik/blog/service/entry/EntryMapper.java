package am.ik.blog.service.entry;


import am.ik.blog.model.Author;
import am.ik.blog.model.Category;
import am.ik.blog.model.Entry;
import am.ik.blog.model.EntryBuilder;
import am.ik.blog.model.FrontMatter;
import am.ik.blog.model.FrontMatterBuilder;
import am.ik.blog.model.Tag;
import am.ik.blog.service.entry.search.SearchCriteria;
import io.r2dbc.spi.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public Mono<Long> nextId() {
        return this.databaseClient.execute("SELECT max(entry_id) + 1 FROM entry")
            .as(Long.class).fetch().one();
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


    public Mono<Page<Entry>> findPage(SearchCriteria criteria, Pageable pageable) {
        return this.count(criteria)
            .zipWith(this.findAll(criteria, pageable).collectList()
                .switchIfEmpty(Mono.fromCallable(List::of))) //
            .doOnError(e -> log.error("Failed to find a page!", e))
            .map(tpl -> new PageImpl<>(tpl.getT2(), pageable, tpl.getT1()));
    }

    public Mono<Entry> findOne(Long entryId, boolean excludeContent) {
        List<Long> ids = Collections.singletonList(entryId);
        Mono<List<Tag>> tagsMono = this.tagsMap(ids)
            .map(x -> x.getOrDefault(entryId, List.of()));
        Mono<List<Category>> categoriesMono = this.categoriesMap(ids)
            .map(x -> x.get(entryId));
        String sql = String.format(
            "SELECT e.entry_id, e.title%s, e.created_by, e.created_date, e.last_modified_by, e.last_modified_date FROM entry AS e  WHERE e.entry_id = $1",
            (excludeContent ? "" : ", e.content"));
        Mono<Entry> entryMono = this.databaseClient.execute(sql) //
            .bind("$1", entryId) //
            .as(Entry.class) //
            .map((row, meta) -> this.mapRow(row, excludeContent)) //
            .one();
        return entryMono.flatMap(entry -> Mono.zip(categoriesMono, tagsMono) //
            .map(tpl -> {
                FrontMatter fm = entry.getFrontMatter();
                return entry.copy() //
                    .withFrontMatter(
                        new FrontMatterBuilder()
                            .withTitle(fm.getTitle())
                            .withCategories(tpl.getT1())
                            .withTags(tpl.getT2())
                            // TODO: fm.date(), fm.updated()
                            .build())
                    .build();
            })) //
            .doOnError(e -> log.error("Failed to fetch an entry!", e));
    }

    public Mono<OffsetDateTime> findLastModifiedDate(Long entryId) {
        return this.databaseClient
            .execute("SELECT last_modified_date FROM entry WHERE entry_id = $1") //
            .bind("$1", entryId) //
            .map((row, meta) -> row.get("last_modified_date", OffsetDateTime.class))
            .one();
    }

    public Mono<OffsetDateTime> findLatestModifiedDate() {
        return this.databaseClient.execute(
            "SELECT last_modified_date FROM entry ORDER BY last_modified_date DESC LIMIT 1") //
            .map((row, meta) -> row.get("last_modified_date", OffsetDateTime.class))
            .one();
    }

    public Flux<Entry> findAll(SearchCriteria criteria, Pageable pageable) {
        SearchCriteria.ClauseAndParams clauseAndParams = criteria.toWhereClause();
        return this.entryIds(criteria, pageable, clauseAndParams) //
            .collectList() //
            .flatMapMany(ids -> this.tagsMap(ids) //
                .zipWith(this.categoriesMap(ids)) //
                .flatMapMany(tpl -> {
                    Map<Long, List<Tag>> tagsMap = tpl.getT1();
                    Map<Long, List<Category>> categoriesMap = tpl.getT2();
                    String sql = String.format(
                        "SELECT e.entry_id, e.title, e.created_by, e.created_date, e.last_modified_by, e.last_modified_date FROM entry AS e WHERE e.entry_id IN (%s) ORDER BY e.last_modified_date " +
                            "DESC",
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
                                .withFrontMatter(new FrontMatterBuilder()
                                    .withTitle(frontMatter.getTitle())
                                    .withCategories(categoriesMap.get(e.getEntryId()))
                                    .withTags(tagsMap.get(e.getEntryId()))
                                    .build())
                                .build();
                        }).doOnRequest(n -> log.debug("request({})", n))
                        ;
                }));
    }

    public Mono<Entry> save(Entry entry) {
        FrontMatter frontMatter = entry.getFrontMatter();
        Author created = entry.getCreated();
        Author updated = entry.getUpdated();
        Long entryId = entry.getEntryId();
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
            .bind("title", frontMatter.getTitle()) //
            .bind("content", entry.getContent()) //
            .bind("created_by", created.getName()) //
            .bind("created_date", created.getDate()) //
            .bind("last_modified_by", updated.getName()) //
            .bind("last_modified_date", updated.getDate()) //
            .bind("title2", frontMatter.getTitle()) //
            .bind("content2", entry.getContent()) //
            .bind("created_by2", created.getName()) //
            .bind("created_date2", created.getDate()) //
            .bind("last_modified_by2", updated.getName()) //
            .bind("last_modified_date2", updated.getDate()) //
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
            .fromIterable(frontMatter.getCategories())
            .flatMap(category -> this.databaseClient.execute(
                "INSERT INTO category (category_name, category_order, entry_id) VALUES ($1, $2, $3)") //
                .bind("$1", category.getName()) //
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
        Flux<Integer> upsertTag = Flux.fromIterable(frontMatter.getTags()) //
            .flatMap(tag -> this.databaseClient
                .execute("INSERT INTO tag (tag_name) VALUES (:tag_name)" //
                    + " ON CONFLICT ON CONSTRAINT tag_pkey" //
                    + " DO UPDATE SET tag_name = :tag_name2") //
                .bind("tag_name", tag.getName()) //
                .bind("tag_name2", tag.getName()) //
                .fetch().rowsUpdated()) //
            .log("upsertTag") //
            ;
        Flux<Integer> insertEntryTag = Flux.fromIterable(frontMatter.getTags()) //
            .flatMap(tag -> this.databaseClient.execute(
                "INSERT INTO entry_tag (entry_id, tag_name) VALUES ($1, $2)") //
                .bind("$1", entryId) //
                .bind("$2", tag.getName()) //
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

    public Mono<Long> delete(Long entryId) {
        return this.databaseClient.execute("DELETE FROM entry WHERE entry_id = $1") //
            .bind("$1", entryId) //
            .fetch().rowsUpdated() //
            .as(this.transactionalOperator::transactional) //
            .log("delete") //
            .then(Mono.just(entryId));
    }

    Entry mapRow(Row row, boolean excludeContent) {
        return new EntryBuilder().withEntryId(row.get("entry_id", Long.class))
            .withContent(excludeContent ? "" : row.get("content", String.class)) //
            .withFrontMatter(new FrontMatterBuilder()
                .withTitle(row.get("title", String.class))
                .build())
            .withCreated(new Author(row.get("created_by", String.class),
                row.get("created_date", OffsetDateTime.class))) //
            .withUpdated(new Author(row.get("last_modified_by", String.class),
                row.get("last_modified_date", OffsetDateTime.class))) //
            .build();
    }

    Mono<Map<Long, List<Tag>>> tagsMap(List<Long> ids) {
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
                Objects.requireNonNull(row.get("entry_id", Long.class)),
                Tag.of(row.get("tag_name", String.class)))) //
            .all() //
            .collectList() //
            .map(list -> list.stream() //
                .collect(groupingBy(Tuple2::getT1)) //
                .entrySet() //
                .stream() //
                .map(e -> Tuples.of(e.getKey(), e.getValue() //
                    .stream() //
                    .map(Tuple2::getT2) //
                    .collect(toList())))
                .collect(toMap(Tuple2::getT1, Tuple2::getT2)));
    }

    Mono<Map<Long, List<Category>>> categoriesMap(List<Long> ids) {
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
                Objects.requireNonNull(row.get("entry_id", Long.class)),
                Category.of(row.get("category_name", String.class)))) //
            .all() //
            .collectList() //
            .map(list -> list.stream() //
                .collect(groupingBy(Tuple2::getT1)) //
                .entrySet() //
                .stream() //
                .map(e -> Tuples.of(e.getKey(), e.getValue() //
                    .stream() //
                    .map(Tuple2::getT2) //
                    .collect(toList())))
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

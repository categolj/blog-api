package am.ik.blog.category;

import java.util.List;
import java.util.stream.Stream;

import reactor.core.publisher.Flux;

import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toList;

@Component
public class CategoryMapper {

	private final DatabaseClient databaseClient;

	public CategoryMapper(DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
	}

	@NewSpan
	public Flux<List<Category>> findAll() {
		return this.databaseClient.sql(
				"SELECT DISTINCT ARRAY_TO_STRING(ARRAY(SELECT category_name FROM category WHERE category.entry_id = e.entry_id ORDER BY category_order ASC), ',') AS category FROM entry AS e ORDER BY " +
						"category")
				.map(row -> row.get("category", String.class)).all()
				.map(s -> Stream.of(s.split(","))
						.map(Category::of)
						.collect(toList()));
	}
}
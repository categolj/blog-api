package am.ik.blog.entry;

import java.util.List;
import java.util.stream.Stream;

import am.ik.blog.entry.Categories;
import am.ik.blog.entry.Category;
import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toList;

@Component
public class CategoryMapper {
	private final DatabaseClient databaseClient;

	public CategoryMapper(DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
	}

	public Mono<List<Categories>> findAll() {
		return this.databaseClient.execute().sql(
				"SELECT DISTINCT ARRAY_TO_STRING(ARRAY(SELECT category_name FROM category WHERE category.entry_id = e.entry_id ORDER BY category_order ASC), ',') AS category FROM entry AS e ORDER BY category")
				.exchange() //
				.flatMap(
						result -> result
								.extract((row, meta) -> row.get("category", String.class))
								.all()
								.map(s -> new Categories(Stream.of(s.split(","))
										.map(Category::new).collect(toList())))
								.collectList());
	}
}

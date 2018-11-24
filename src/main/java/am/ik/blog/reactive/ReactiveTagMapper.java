package am.ik.blog.reactive;

import java.util.List;

import am.ik.blog.entry.Tag;
import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.stereotype.Component;

@Component
public class ReactiveTagMapper {
	private final DatabaseClient databaseClient;

	public ReactiveTagMapper(DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
	}

	public Mono<List<Tag>> findOrderByTagNameAsc() {
		return this.databaseClient.execute()
				.sql("SELECT tag_name FROM tag ORDER BY tag_name ASC").exchange()
				.flatMap(result -> result
						.extract(
								(row, meta) -> new Tag(row.get("tag_name", String.class)))
						.all().collectList());
	}
}

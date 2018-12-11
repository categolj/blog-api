package am.ik.blog.entry;

import java.util.List;

import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {
	private final DatabaseClient databaseClient;

	public TagMapper(DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
	}

	public Mono<List<Tag>> findOrderByTagNameAsc() {
		return this.databaseClient.execute()
				.sql("SELECT tag_name FROM tag ORDER BY tag_name ASC")
				.map((row, meta) -> new Tag(row.get("tag_name", String.class))).all()
				.collectList();
	}
}

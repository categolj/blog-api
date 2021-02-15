package am.ik.blog.tag;

import reactor.core.publisher.Flux;

import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

	private final DatabaseClient databaseClient;

	public TagMapper(DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
	}

	@NewSpan
	public Flux<Tag> findOrderByTagNameAsc() {
		return this.databaseClient
				.sql("SELECT tag_name FROM tag ORDER BY tag_name ASC")
				.map(row -> Tag.of(row.get("tag_name", String.class)))
				.all();
	}
}

package am.ik.blog.service.tag;

import am.ik.blog.model.Tag;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class TagMapper {

    private final DatabaseClient databaseClient;

    public TagMapper(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Flux<Tag> findOrderByTagNameAsc() {
        return this.databaseClient
            .execute("SELECT tag_name FROM tag ORDER BY tag_name ASC")
            .map((row, meta) -> Tag.of(row.get("tag_name", String.class)))
            .all();
    }
}

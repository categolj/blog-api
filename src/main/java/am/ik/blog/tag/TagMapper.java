package am.ik.blog.tag;

import am.ik.blog.tag.Tag;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class TagMapper {

    private final DatabaseClient databaseClient;

    public TagMapper(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @NewSpan
    public Flux<Tag> findOrderByTagNameAsc() {
        return this.databaseClient
            .execute("SELECT tag_name FROM tag ORDER BY tag_name ASC")
            .map((row, meta) -> Tag.of(row.get("tag_name", String.class)))
            .all();
    }
}

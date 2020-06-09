package am.ik.blog.entry;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.springframework.core.io.Resource;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

@JsonPOJOBuilder
public class EntryBuilder {

    private String content;

    private Author created;

    private Long entryId;

    private FrontMatter frontMatter;

    private Author updated;


    public Entry build() {
        return new Entry(entryId, frontMatter, content, created, updated);
    }

    public EntryBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    public EntryBuilder withCreated(Author created) {
        this.created = created;
        return this;
    }

    public EntryBuilder withEntryId(Long entryId) {
        this.entryId = entryId;
        return this;
    }

    public EntryBuilder withFrontMatter(FrontMatter frontMatter) {
        this.frontMatter = frontMatter;
        return this;
    }

    public EntryBuilder withUpdated(Author updated) {
        this.updated = updated;
        return this;
    }

    public static Optional<Tuple3<EntryBuilder, Optional<OffsetDateTime>, Optional<OffsetDateTime>>> createFromYamlFile(Resource file) {
        Long entryId = Entry.parseId(Objects.requireNonNull(file.getFilename()));
        try (InputStream stream = file.getInputStream()) {
            return parseBody(entryId, stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Optional<Tuple3<EntryBuilder, Optional<OffsetDateTime>, Optional<OffsetDateTime>>> parseBody(Long entryId, InputStream body) {
        return parseBody(entryId,
            new InputStreamReader(body, StandardCharsets.UTF_8));
    }

    public static Optional<Tuple3<EntryBuilder, Optional<OffsetDateTime>, Optional<OffsetDateTime>>> parseBody(Long entryId, String body) {
        return parseBody(entryId, new StringReader(body));
    }

    private static Optional<Tuple3<EntryBuilder, Optional<OffsetDateTime>, Optional<OffsetDateTime>>> parseBody(Long entryId, Reader r) {
        try (final BufferedReader reader = new BufferedReader(r)) {
            final EntryBuilder builder = new EntryBuilder().withEntryId(entryId);
            final StringBuilder yaml = new StringBuilder();
            final String firstLine = reader.readLine();
            Optional<OffsetDateTime> created;
            Optional<OffsetDateTime> updated;
            if (FrontMatter.SEPARATOR.equals(firstLine)) {
                for (String line = reader.readLine(); line != null
                    && !FrontMatter.SEPARATOR.equals(line); line = reader
                    .readLine()) {
                    yaml.append(line);
                    yaml.append(System.lineSeparator());
                }
                final Tuple3<FrontMatter, Optional<OffsetDateTime>, Optional<OffsetDateTime>> parsed = FrontMatterBuilder.parseYaml(yaml.toString());
                builder.withFrontMatter(parsed.getT1());
                created = parsed.getT2();
                updated = parsed.getT3();
            } else {
                return Optional.empty();
            }
            final StringBuilder content = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader
                .readLine()) {
                content.append(line);
                content.append(System.lineSeparator());
            }
            builder.withContent(content.toString().trim());
            return Optional.of(Tuples.of(builder, created, updated));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
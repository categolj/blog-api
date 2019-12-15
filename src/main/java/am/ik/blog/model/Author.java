package am.ik.blog.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.OffsetDateTime;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

@JsonDeserialize(builder = AuthorBuilder.class)
public class Author {

    private final String name;

    private final OffsetDateTime date;

    public Author(String name, OffsetDateTime date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public OffsetDateTime getDate() {
        return date;
    }


    public Author changeDate(OffsetDateTime date) {
        return new Author(this.name, date);
    }

    public String rfc1123DateTime() {
        if (this.date == null) {
            return "";
        }
        return this.date.format(RFC_1123_DATE_TIME);
    }
}

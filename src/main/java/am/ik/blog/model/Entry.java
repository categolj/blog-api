package am.ik.blog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.StringJoiner;

@JsonDeserialize(builder = EntryBuilder.class)
public class Entry {

    private final Long entryId;

    private final FrontMatter frontMatter;

    private final String content;

    private final Author created;

    private final Author updated;

    Entry(Long entryId, FrontMatter frontMatter, String content, Author created, Author updated) {
        this.entryId = entryId;
        this.frontMatter = frontMatter;
        this.content = content;
        this.created = created;
        this.updated = updated;
    }

    public static Long parseId(String fileName) {
        return Long.parseLong(fileName.replace(".md", "").replace(".markdown", ""));
    }

    public Long getEntryId() {
        return entryId;
    }

    public FrontMatter getFrontMatter() {
        return frontMatter;
    }

    public String getContent() {
        return content;
    }

    public Author getCreated() {
        return created;
    }

    public Author getUpdated() {
        return updated;
    }

    @JsonIgnore
    public boolean isOverHalfYearOld() {
        return this.isOld(6, ChronoUnit.MONTHS);
    }

    @JsonIgnore
    public boolean isOverOneYearOld() {
        return this.isOld(1, ChronoUnit.YEARS);
    }

    @JsonIgnore
    public boolean isOverThreeYearsOld() {
        return this.isOld(3, ChronoUnit.YEARS);
    }

    @JsonIgnore
    public boolean isOverFiveYearsOld() {
        return this.isOld(5, ChronoUnit.YEARS);
    }

    private boolean isOld(long amount, TemporalUnit unit) {
        return this.getUpdated().getDate().plus(amount, unit) //
            .isBefore(OffsetDateTime.now());
    }

    public EntryBuilder copy() {
        return new EntryBuilder()
            .withEntryId(this.entryId)
            .withContent(this.content)
            .withFrontMatter(this.frontMatter)
            .withCreated(this.created)
            .withUpdated(this.updated);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Entry.class.getSimpleName() + "[", "]")
            .add("entryId=" + entryId).toString();
    }
}

package am.ik.blog.entry;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collections;
import java.util.Objects;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@JsonDeserialize(builder = EntryBuilder.class)
@SuppressWarnings("JavaTimeDefaultTimeZone")
public class Entry {

	static public Validator<Entry> validator = ValidatorBuilder.<Entry>of()
		.constraint(Entry::getEntryId, "entryId", c -> c.notNull().positive())
		.constraint(Entry::getContent, "content", c -> c.notBlank().asByteArray().lessThanOrEqual(1024 * 1024 * 1024))
		.nest(Entry::getFrontMatter, "frontMatter", FrontMatter.validator)
		.nest(Entry::getCreated, "created", Author.validator)
		.nest(Entry::getUpdated, "updated", Author.validator)
		.build();

	private final Long entryId;

	private final FrontMatter frontMatter;

	private final String content;

	private final Author created;

	private final Author updated;

	Entry(@Nullable Long entryId, @Nullable FrontMatter frontMatter, @Nullable String content, @Nullable Author created,
			@Nullable Author updated) {
		this.entryId = Objects.requireNonNullElse(entryId, -1L);
		this.frontMatter = Objects.requireNonNullElseGet(frontMatter,
				() -> new FrontMatter("", Collections.emptyList(), Collections.emptyList()));
		this.content = Objects.requireNonNullElse(content, "");
		this.created = Objects.requireNonNullElse(created, Author.NULL_AUTHOR);
		this.updated = Objects.requireNonNullElse(updated, Author.NULL_AUTHOR);
	}

	public static Long parseId(String fileName) {
		return Long.parseLong(fileName.replace(".md", "").replace(".markdown", ""));
	}

	@NonNull
	public Long getEntryId() {
		return entryId;
	}

	public String formatId() {
		return "%05d".formatted(entryId);
	}

	@NonNull
	public FrontMatter getFrontMatter() {
		return frontMatter;
	}

	@NonNull
	public String getContent() {
		return content;
	}

	@NonNull
	public Author getCreated() {
		return created;
	}

	@NonNull
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
		OffsetDateTime date = this.getUpdated().date();
		if (date == null) {
			return false;
		}
		return date.plus(amount, unit) //
			.isBefore(OffsetDateTime.now());
	}

	@Override
	public String toString() {
		return "Entry{" + "entryId=" + entryId + ", frontMatter=" + frontMatter + ", created=" + created + ", updated="
				+ updated + '}';
	}

	public String toMarkdown() {
		return """
				---
				title: %s
				tags: %s
				categories: %s%s%s
				---

				%s
				""".formatted(frontMatter.title(),
				frontMatter.tags().stream().map(t -> "\"%s\"".formatted(t.name())).toList(),
				frontMatter.categories().stream().map(c -> "\"%s\"".formatted(c.name())).toList(),
				created.date() == null ? "" : "%ndate: %s".formatted(created.date()),
				updated.date() == null ? "" : "%nupdated: %s".formatted(updated.date()), content);
	}

}

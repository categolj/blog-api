package am.ik.blog.entry;

import am.ik.blog.category.Category;
import am.ik.blog.tag.Tag;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.StringJoiner;

@JsonDeserialize(builder = EntryBuilder.class)
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

	public String formatId() {
		return "%05d".formatted(entryId);
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

	@Override
	public String toString() {
		return "Entry{" +
			   "entryId=" + entryId +
			   ", frontMatter=" + frontMatter +
			   ", created=" + created +
			   ", updated=" + updated +
			   '}';
	}

	public String toMarkdown() {
		return """
				---
				title: %s
				tags: %s
				categories: %s%s%s
				---
								
				%s
				""".formatted(frontMatter.getTitle(),
				frontMatter.getTags().stream().map(t -> "\"%s\"".formatted(t.name())).toList(),
				frontMatter.getCategories().stream().map(c -> "\"%s\"".formatted(c.name())).toList(),
				created.getDate() == null ? "" : "%ndate: %s".formatted(created.getDate()),
				updated.getDate() == null ? "" : "%nupdated: %s".formatted(updated.getDate()),
				content);
	}
}

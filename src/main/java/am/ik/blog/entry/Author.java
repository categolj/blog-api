package am.ik.blog.entry;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

@JsonDeserialize(builder = AuthorBuilder.class)
public class Author {
	public static Validator<Author> validator = ValidatorBuilder.<Author>of()
			.constraint(Author::getName, "name", c -> c.notBlank().lessThanOrEqual(128))
			.constraint(Author::getDate, "date", c -> c.notNull().afterOrEqual(() -> OffsetDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault())))
			.build();

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

	@Override
	public String toString() {
		return "Author{" +
				"name='" + name + '\'' +
				", date=" + date +
				'}';
	}
}

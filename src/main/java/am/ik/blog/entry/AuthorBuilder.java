package am.ik.blog.entry;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.OffsetDateTime;

import org.springframework.lang.Nullable;

@JsonPOJOBuilder
public class AuthorBuilder {

	@Nullable
	private OffsetDateTime date;

	@Nullable
	private String name;

	public Author build() {
		return new Author(name, date);
	}

	public AuthorBuilder withDate(OffsetDateTime date) {
		this.date = date;
		return this;
	}

	public AuthorBuilder withName(String name) {
		this.name = name;
		return this;
	}

}
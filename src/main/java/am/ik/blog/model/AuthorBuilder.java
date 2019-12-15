package am.ik.blog.model;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.OffsetDateTime;

@JsonPOJOBuilder
public class AuthorBuilder {

	private OffsetDateTime date;

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
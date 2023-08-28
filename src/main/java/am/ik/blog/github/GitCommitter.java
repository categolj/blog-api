package am.ik.blog.github;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

public record GitCommitter(String name, String email, OffsetDateTime date) {

	@Override
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public OffsetDateTime date() {
		return date;
	}
}

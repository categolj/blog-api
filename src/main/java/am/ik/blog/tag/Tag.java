package am.ik.blog.tag;

import com.fasterxml.jackson.annotation.JsonInclude;

public record Tag(String name, @JsonInclude(JsonInclude.Include.NON_EMPTY) String version) {
	public Tag(String name) {
		this(name, null);
	}
}

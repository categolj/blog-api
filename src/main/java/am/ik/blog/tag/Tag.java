package am.ik.blog.tag;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.lang.Nullable;

public record Tag(String name, @Nullable @JsonInclude(JsonInclude.Include.NON_EMPTY) String version) {
	public Tag(String name) {
		this(name, null);
	}
}

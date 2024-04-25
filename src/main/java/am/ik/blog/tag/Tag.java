package am.ik.blog.tag;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record Tag(@NonNull String name, @Nullable @JsonInclude(JsonInclude.Include.NON_EMPTY) String version) {
	public Tag(String name) {
		this(name, null);
	}
}

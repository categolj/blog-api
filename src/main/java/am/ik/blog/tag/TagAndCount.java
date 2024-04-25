package am.ik.blog.tag;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import org.springframework.lang.NonNull;

public record TagAndCount(@NonNull @JsonUnwrapped Tag tag,
		@SuppressWarnings("NullablePrimitive") @NonNull /* for OpenAPI */ int count) {
}

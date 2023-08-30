package am.ik.blog.tag;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record TagAndCount(@JsonUnwrapped Tag tag, int count) {
}

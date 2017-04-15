package am.ik.blog.exception;

import java.util.function.Supplier;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}

	public static Supplier<ResourceNotFoundException> defer(String message) {
		return () -> new ResourceNotFoundException(message);
	}
}

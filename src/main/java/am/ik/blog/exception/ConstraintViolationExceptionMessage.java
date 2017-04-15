/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.blog.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

/**
 * @author Jon Brisbin
 * @author Oliver Gierke
 */
public class ConstraintViolationExceptionMessage {

	private final List<ValidationError> errors = new ArrayList<>();

	/**
	 * Creates a new {@link ConstraintViolationExceptionMessage} for the given
	 * {@link org.springframework.validation.BindException} and
	 * {@link MessageSourceAccessor}.
	 *
	 * @param exception must not be {@literal null}.
	 * @param accessor must not be {@literal null}.
	 */
	public ConstraintViolationExceptionMessage(BindException exception,
			MessageSourceAccessor accessor) {

		Assert.notNull(exception, "BindException must not be null!");
		Assert.notNull(accessor, "MessageSourceAccessor must not be null!");

		for (FieldError fieldError : exception.getFieldErrors()) {
			this.errors.add(ValidationError.of(fieldError.getObjectName(),
					fieldError.getField(), fieldError.getRejectedValue(),
					accessor.getMessage(fieldError)));
		}
	}

	public ConstraintViolationExceptionMessage(MethodArgumentNotValidException exception,
			MessageSourceAccessor accessor) {
		Assert.notNull(exception, "MethodArgumentNotValidException must not be null!");
		Assert.notNull(accessor, "MessageSourceAccessor must not be null!");

		for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
			this.errors.add(ValidationError.of(fieldError.getObjectName(),
					fieldError.getField(), fieldError.getRejectedValue(),
					accessor.getMessage(fieldError)));
		}
	}

	@JsonProperty("errors")
	public List<ValidationError> getErrors() {
		return errors;
	}

	@Value(staticConstructor = "of")
	public static class ValidationError {
		String entity, property;
		Object invalidValue;
		String message;
	}
}
package am.ik.blog.exception;

import java.util.Locale;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {
	private final MessageSourceAccessor messageSourceAccessor;

	public RestExceptionHandler(MessageSource messageSource) {
		this.messageSourceAccessor = new MessageSourceAccessor(messageSource,
				Locale.getDefault());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	SimpleExceptionMessage handleResourceNotFoundException(ResourceNotFoundException e) {
		return new SimpleExceptionMessage(e.getMessage());
	}

	@ExceptionHandler(NotSubscribedException.class)
	@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
	SimpleExceptionMessage handleNotSubscribedException(NotSubscribedException e) {
		return new SimpleExceptionMessage(e.getMessage());
	}

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ConstraintViolationExceptionMessage handleException(BindException e) {
		return new ConstraintViolationExceptionMessage(e, messageSourceAccessor);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ConstraintViolationExceptionMessage handleException(
			MethodArgumentNotValidException e) {
		return new ConstraintViolationExceptionMessage(e, messageSourceAccessor);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	SimpleExceptionMessage handleException(MethodArgumentTypeMismatchException e) {
		return new SimpleExceptionMessage("The given request (" + e.getName() + " = "
				+ e.getValue() + ") is not valid.");
	}

	@ExceptionHandler(TypeMismatchException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	SimpleExceptionMessage handleException(TypeMismatchException e) {
		return new SimpleExceptionMessage(
				"The given request (" + e.getValue() + ") is not valid.");
	}

	@ExceptionHandler(DataAccessException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	SimpleExceptionMessage handleException(DataAccessException e) {
		log.error("DB Error", e);
		return new SimpleExceptionMessage("DB Error");
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	SimpleExceptionMessage handleException(RuntimeException e) {
		log.error("Unexpected Exception", e);
		return new SimpleExceptionMessage("Unexpected Exception");
	}
}
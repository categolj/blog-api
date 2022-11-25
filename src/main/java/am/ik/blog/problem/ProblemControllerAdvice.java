package am.ik.blog.problem;

import am.ik.yavi.core.ConstraintViolationsException;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ProblemControllerAdvice {
	private final Logger log = LoggerFactory.getLogger(ProblemControllerAdvice.class);

	private final Tracer tracer;

	public ProblemControllerAdvice(Tracer tracer) {
		this.tracer = tracer;
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ProblemDetail handleResponseStatusException(ResponseStatusException e) {
		final ProblemDetail problemDetail = ProblemDetail.forStatus(e.getStatusCode());
		problemDetail.setDetail(e.getReason());
		return setTraceId(problemDetail);
	}

	@ExceptionHandler(ConstraintViolationsException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProblemDetail handleConstraintViolationsException(
			ConstraintViolationsException e) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST, "Constraint violations found!");
		problemDetail.setProperty("violations", e.violations().details());
		return setTraceId(problemDetail);
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ProblemDetail handleRuntimeException(RuntimeException e) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred!");
		log.error("Unexpected error occurred!", e);
		return setTraceId(problemDetail);
	}

	private ProblemDetail setTraceId(ProblemDetail problemDetail) {
		final Span currentSpan = tracer.currentSpan();
		if (currentSpan != null) {
			problemDetail.setProperty("traceId", currentSpan.context().traceId());
		}
		return problemDetail;
	}
}

package am.ik.blog.problem;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ProblemControllerAdvice {
	@ExceptionHandler(ResponseStatusException.class)
	public ProblemDetail handleResponseStatusException(ResponseStatusException e) {
		final ProblemDetail problemDetail = ProblemDetail.forStatus(e.getStatusCode());
		problemDetail.setDetail(e.getReason());
		return problemDetail;
	}
}

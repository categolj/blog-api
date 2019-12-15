package am.ik.blog.error;


import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class ErrorResponseBuilder {

    private Map<String, List<String>> details;

    private String error;

    private String message;

    private Integer status;

    public ErrorResponse build() {
        return new ErrorResponse(status, error, message, details);
    }

    public ErrorResponseBuilder withDetails(Map<String, List<String>> details) {
        this.details = details;
        return this;
    }

    public ErrorResponseBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public ErrorResponseBuilder withMessage(String format, Object... args) {
        this.message = String.format(format, args);
        return this;
    }

    public ErrorResponseBuilder withStatus(HttpStatus status) {
        this.status = status.value();
        this.error = status.getReasonPhrase();
        return this;
    }
}

package am.ik.blog.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

public class ErrorResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Integer status;

    private final String error;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, List<String>> details;

    public ErrorResponse(Integer status, String error, String message, Map<String, List<String>> details) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.details = details;
    }

    public Integer getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, List<String>> getDetails() {
        return details;
    }
}

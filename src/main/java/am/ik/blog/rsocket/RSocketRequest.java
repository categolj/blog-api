package am.ik.blog.rsocket;

import java.util.Map;

public class RSocketRequest {
	private final Map<String, String> pathVariables;
	private final RSocketQueryParams queryParams;

	public RSocketRequest(Map<String, String> pathVariables,
			RSocketQueryParams queryParams) {
		this.pathVariables = pathVariables;
		this.queryParams = queryParams;
	}

	public Map<String, String> getPathVariables() {
		return pathVariables;
	}

	public RSocketQueryParams getQueryParams() {
		return queryParams;
	}
}

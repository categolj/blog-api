package am.ik.blog.github;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class CommitParameter {
	private String sha;

	private String path;

	private String author;

	private ZonedDateTime since;

	private ZonedDateTime until;

	public CommitParameter() {

	}

	public CommitParameter sha(String sha) {
		this.sha = sha;
		return this;
	}

	public CommitParameter path(String path) {
		this.path = path;
		return this;
	}

	public CommitParameter author(String author) {
		this.author = author;
		return this;
	}

	public CommitParameter since(ZonedDateTime since) {
		this.since = since;
		return this;
	}

	public CommitParameter until(ZonedDateTime until) {
		this.until = until;
		return this;
	}

	public MultiValueMap<String, String> queryParams() {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		if (sha != null) {
			queryParams.add("sha", sha);
		}
		if (path != null) {
			queryParams.add("path", path);
		}
		if (author != null) {
			queryParams.add("author", author);
		}
		if (since != null) {
			queryParams.add("since",
					since.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		}
		if (until != null) {
			queryParams.add("until",
					until.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		}
		return queryParams;
	}
}

package am.ik.blog.github;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Parent {

	private final String sha;

	private final String url;

	private final String htmlUrl;

	@JsonCreator
	public Parent(@JsonProperty("sha") String sha, @JsonProperty("url") String url,
			@JsonProperty("html_url") String htmlUrl) {
		this.sha = sha;
		this.url = url;
		this.htmlUrl = htmlUrl;
	}

	public String sha() {
		return sha;
	}

	public String url() {
		return url;
	}

	public String htmlUrl() {
		return htmlUrl;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Parent))
			return false;
		Parent parent = (Parent) o;
		return Objects.equals(sha, parent.sha);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sha);
	}

	@Override
	public String toString() {
		return "Parent[" + "sha=" + sha + ", " + "url=" + url + ", " + "htmlUrl=" + htmlUrl + ']';
	}

}

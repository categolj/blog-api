package am.ik.blog.github;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Commit extends Parent {

	private final String commentsUrl;

	private final GitCommit commit;

	private final Committer author;

	private final Committer committer;

	private final List<Parent> parents;

	@JsonCreator
	public Commit(@JsonProperty("sha") String sha, @JsonProperty("url") String url,
			@JsonProperty("html_url") String htmlUrl, @JsonProperty("comments_url") String commentsUrl,
			@JsonProperty("commit") GitCommit commit, @JsonProperty("author") Committer author,
			@JsonProperty("committer") Committer committer, @JsonProperty("parents") List<Parent> parents) {
		super(sha, url, htmlUrl);
		this.commentsUrl = commentsUrl;
		this.commit = commit;
		this.author = author;
		this.committer = committer;
		this.parents = parents;
	}

	public String commentsUrl() {
		return commentsUrl;
	}

	public GitCommit commit() {
		return commit;
	}

	public Committer author() {
		return author;
	}

	public Committer committer() {
		return committer;
	}

	public List<Parent> parents() {
		return parents;
	}

	@Override
	public String toString() {
		return "Commit[" + "sha=" + sha() + ", " + "url=" + url() + ", " + "htmlUrl=" + htmlUrl() + ", "
				+ "commentsUrl=" + commentsUrl + ", " + "commit=" + commit + ", " + "author=" + author + ", "
				+ "committer=" + committer + ", " + "parents=" + parents + ']';
	}

}

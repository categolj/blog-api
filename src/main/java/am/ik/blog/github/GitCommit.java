package am.ik.blog.github;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GitCommit extends Parent {
	private final GitCommitter author;

	private final GitCommitter committer;

	private final Tree tree;

	private final String message;

	private final List<Parent> parents;

	@JsonCreator
	public GitCommit(
			@JsonProperty("sha") String sha,
			@JsonProperty("url") String url,
			@JsonProperty("html_url") String htmlUrl,
			@JsonProperty("author") GitCommitter author,
			@JsonProperty("committer") GitCommitter committer,
			@JsonProperty("tree") Tree tree,
			@JsonProperty("message") String message,
			@JsonProperty("parents") List<Parent> parents) {
		super(sha, url, htmlUrl);
		this.author = author;
		this.committer = committer;
		this.tree = tree;
		this.message = message;
		this.parents = parents;
	}

	public GitCommitter author() {
		return author;
	}

	public GitCommitter committer() {
		return committer;
	}

	public Tree tree() {
		return tree;
	}

	public String message() {
		return message;
	}

	public List<Parent> parents() {
		return parents;
	}

	@Override
	public String toString() {
		return "GitCommit{" +
				"author=" + author +
				", committer=" + committer +
				", tree=" + tree +
				", message='" + message + '\'' +
				", parents=" + parents +
				'}';
	}
}
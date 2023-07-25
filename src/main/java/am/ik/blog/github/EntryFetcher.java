package am.ik.blog.github;

import am.ik.blog.entry.Author;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryBuilder;
import am.ik.blog.util.Tuple3;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class EntryFetcher {

	private final GitHubClient gitHubClient;

	public final Map<String, GitHubClient> tenantsGitHubClient;

	public EntryFetcher(GitHubClient gitHubClient,
			Map<String, GitHubClient> tenantsGitHubClient) {
		this.gitHubClient = gitHubClient;
		this.tenantsGitHubClient = tenantsGitHubClient;
	}

	@Deprecated
	public Optional<Entry> fetch(String owner, String repo, String path) {
		return this.fetch(null, owner, repo, path);
	}

	public Optional<Entry> fetch(String tenantId, String owner, String repo,
			String path) {
		GitHubClient gitHubClient;
		if (tenantId == null) {
			gitHubClient = this.gitHubClient;
		}
		else {
			gitHubClient = this.tenantsGitHubClient.getOrDefault(tenantId,
					this.gitHubClient);
		}
		Long entryId = Entry.parseId(Paths.get(path).getFileName().toString());
		File file = gitHubClient.getFile(owner, repo, path);
		List<Commit> commits = gitHubClient.getCommits(owner, repo,
				new CommitParameter().path(path).queryParams());
		Optional<Tuple3<EntryBuilder, Optional<OffsetDateTime>, Optional<OffsetDateTime>>> parsed = this
				.parse(entryId, file);
		Author created = commits.isEmpty() ? null
				: toAuthor(commits.get(commits.size() - 1));
		Author updated = commits.isEmpty() ? null : toAuthor(commits.get(0));
		return parsed.map(tpl -> {
			EntryBuilder entryBuilder = tpl.getT1();
			return entryBuilder //
					.withCreated(tpl.getT2().map(created::withDate).orElse(created)) //
					.withUpdated(tpl.getT3().map(updated::withDate).orElse(updated)) //
					.build();
		});
	}

	private Optional<Tuple3<EntryBuilder, Optional<OffsetDateTime>, Optional<OffsetDateTime>>> parse(
			Long entryId, File file) {
		return EntryBuilder.parseBody(entryId, file.decode());
	}

	private Author toAuthor(Commit commit) {
		GitCommitter committer = commit.commit().author();
		return new Author(committer.name(), committer.date());
	}
}
package am.ik.blog.github;

import am.ik.blog.entry.Author;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryBuilder;
import am.ik.blog.util.Tuple3;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class EntryFetcher {

	private final GitHubClient gitHubClient;

	public final Map<String, GitHubClient> tenantsGitHubClient;

	public EntryFetcher(GitHubClient gitHubClient, Map<String, GitHubClient> tenantsGitHubClient) {
		this.gitHubClient = gitHubClient;
		this.tenantsGitHubClient = tenantsGitHubClient;
	}

	public Optional<Entry> fetch(@Nullable String tenantId, String owner, String repo, String path) {
		GitHubClient gitHubClient;
		if (tenantId == null) {
			gitHubClient = this.gitHubClient;
		}
		else {
			gitHubClient = this.tenantsGitHubClient.getOrDefault(tenantId, this.gitHubClient);
		}
		Long entryId = Entry.parseId(Paths.get(path).getFileName().toString());
		ResponseEntity<File> fileResponse = gitHubClient.getFile(owner, repo, path);
		if (!fileResponse.getStatusCode().is2xxSuccessful() || fileResponse.getBody() == null) {
			return Optional.empty();
		}
		File file = fileResponse.getBody();
		List<Commit> commits = gitHubClient.getCommits(owner, repo, new CommitParameter().path(path).queryParams());
		Optional<Tuple3<EntryBuilder, Optional<OffsetDateTime>, Optional<OffsetDateTime>>> parsed = this.parse(entryId,
				file);
		Author created = commits.isEmpty() ? null : toAuthor(commits.get(commits.size() - 1));
		Author updated = commits.isEmpty() ? null : toAuthor(commits.get(0));
		return parsed.map(tpl -> {
			EntryBuilder entryBuilder = tpl.getT1();
			if (created != null) {
				entryBuilder.withCreated(tpl.getT2().map(created::withDate).orElse(created));
			}
			if (updated != null) {
				entryBuilder.withUpdated(tpl.getT3().map(updated::withDate).orElse(updated));
			}
			return entryBuilder.build();
		});
	}

	private Optional<Tuple3<EntryBuilder, Optional<OffsetDateTime>, Optional<OffsetDateTime>>> parse(Long entryId,
			File file) {
		return EntryBuilder.parseBody(entryId, file.decode());
	}

	private Author toAuthor(Commit commit) {
		GitCommitter committer = commit.commit().author();
		return new Author(committer.name(), committer.date());
	}

}
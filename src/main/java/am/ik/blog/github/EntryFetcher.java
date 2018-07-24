package am.ik.blog.github;

import java.nio.file.Paths;
import java.util.Optional;

import am.ik.blog.entry.*;
import am.ik.blog.entry.Entry.EntryBuilder;
import am.ik.blog.entry.factory.EntryFactory;
import am.ik.github.GitHubClient;
import am.ik.github.core.Committer;
import am.ik.github.repositories.commits.CommitsResponse.Commit;
import am.ik.github.repositories.contents.ContentsResponse.File;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import org.springframework.stereotype.Component;

@Component
public class EntryFetcher {
	private final GitHubClient gitHubClient;

	public EntryFetcher(GitHubClient gitHubClient) {
		this.gitHubClient = gitHubClient;
	}

	public Mono<Entry> fetch(String owner, String repo, String path) {
		EntryId entryId = EntryId.fromFilePath(Paths.get(path));
		Mono<File> file = this.gitHubClient.file(owner, repo, path).get();
		Flux<Commit> commits = this.gitHubClient.commits(owner, repo)
				.get(p -> p.path(path));
		Mono<EntryBuilder> entryBuilder = file
				.flatMap(f -> this.toEntryBuilder(entryId, f));

		Mono<Tuple2<Author, Author>> authors = commits.collectList()
				.filter(l -> !l.isEmpty())
				.map(l -> Tuples.of(toAuthor(l.get(0)) /* updated */,
						toAuthor(l.get(l.size() - 1)) /* created */));

		return Mono.zip(entryBuilder, authors) //
				.map(tpl -> tpl.getT1() //
						.created(tpl.getT2().getT2()) //
						.updated(tpl.getT2().getT1()) //
						.build()) //
				.map(Entry::useFrontMatterDate);
	}

	private Mono<EntryBuilder> toEntryBuilder(EntryId entryId, File file) {
		Optional<EntryBuilder> entryBuilder = new EntryFactory() //
				.parseBody(entryId, file.decode());
		return Mono.justOrEmpty(entryBuilder);
	}

	private Author toAuthor(Commit commit) {
		Committer committer = commit.getCommit().getAuthor();
		return new Author(new Name(committer.getName()),
				new EventTime(committer.getDate()));
	}
}
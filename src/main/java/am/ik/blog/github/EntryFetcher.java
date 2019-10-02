package am.ik.blog.github;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

import am.ik.blog.entry.Author;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.Entry.EntryBuilder;
import am.ik.blog.entry.EntryId;
import am.ik.blog.entry.EventTime;
import am.ik.blog.entry.Name;
import am.ik.blog.entry.factory.EntryFactory;
import am.ik.github.GitHubClient;
import am.ik.github.core.Committer;
import am.ik.github.repositories.commits.CommitsResponse.Commit;
import am.ik.github.repositories.contents.ContentsResponse.File;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

@Component
public class EntryFetcher {
	private final GitHubClient gitHubClient;
	private final Jackson2JsonDecoder jsonDecoder;

	public EntryFetcher(GitHubClient gitHubClient, ObjectMapper objectMapper) {
		this.gitHubClient = gitHubClient;
		this.jsonDecoder = new Jackson2JsonDecoder(objectMapper);
	}

	public Mono<Entry> fetch(String owner, String repo, String path) {
		EntryId entryId = EntryId.fromFilePath(Paths.get(path));
		Mono<File> file = this.gitHubClient.file(owner, repo, path).get();
		Flux<DataBuffer> commits = this.gitHubClient.commits(owner, repo).get(p -> p.path(path))
				.cast(DataBuffer.class /* TODO why?? */);
		Mono<EntryBuilder> entryBuilder = file.flatMap(f -> this.toEntryBuilder(entryId, f));

		Mono<Tuple2<Author, Author>> authors = this.jsonDecoder
				.decode(commits, ResolvableType.forClass(Commit.class), MimeTypeUtils.APPLICATION_JSON,
						Collections.emptyMap())
				.cast(Commit.class) //
				.collectList() //
				.filter(l -> !l.isEmpty()) //
				.map(l -> Tuples.of(toAuthor(l.get(0)) /* updated */, toAuthor(l.get(l.size() - 1)) /* created */));
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
		return new Author(new Name(committer.getName()), new EventTime(committer.getDate()));
	}
}
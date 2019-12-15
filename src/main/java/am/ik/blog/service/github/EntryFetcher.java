package am.ik.blog.service.github;

import am.ik.blog.model.Author;
import am.ik.blog.model.Entry;
import am.ik.blog.model.EntryBuilder;
import am.ik.github.GitHubClient;
import am.ik.github.core.Committer;
import am.ik.github.repositories.commits.CommitsResponse.Commit;
import am.ik.github.repositories.contents.ContentsResponse.File;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Optional;

@Component
public class EntryFetcher {

    private final GitHubClient gitHubClient;

    public EntryFetcher(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    public Mono<Entry> fetch(String owner, String repo, String path) {
        Long entryId = Entry.parseId(Paths.get(path).getFileName().toString());
        Mono<File> file = this.gitHubClient.file(owner, repo, path).get();
        Flux<Commit> commits = this.gitHubClient.commits(owner, repo).get(p -> p.path(path));
        final Mono<Tuple3<EntryBuilder, Optional<OffsetDateTime>, Optional<OffsetDateTime>>> parsed = file.flatMap(f -> this.parse(entryId, f));

        Mono<Tuple2<Author, Author>> authors = commits
            .collectList() //
            .filter(l -> !l.isEmpty()) //
            .map(l -> Tuples.of(toAuthor(l.get(l.size() - 1)) /* created */, toAuthor(l.get(0)) /* updated */));
        return Mono.zip(parsed, authors) //
            .map(tpl -> {
                final EntryBuilder entryBuilder = tpl.getT1() //
                    .getT1();
                final Author created = tpl.getT1().getT2()
                    // use frontMatter date if present
                    .map(date -> tpl.getT2().getT1().changeDate(date))
                    // otherwise use commit date
                    .orElseGet(() -> tpl.getT2().getT1());
                final Author updated = tpl.getT1().getT2()
                    .map(date -> tpl.getT2().getT2().changeDate(date))
                    .orElseGet(() -> tpl.getT2().getT2());
                return entryBuilder
                    .withCreated(created) //
                    .withUpdated(updated) //
                    .build();
            });
    }

    private Mono<Tuple3<EntryBuilder, Optional<OffsetDateTime>, Optional<OffsetDateTime>>> parse(Long entryId, File file) {
        Optional<Tuple3<EntryBuilder, Optional<OffsetDateTime>, Optional<OffsetDateTime>>> parsed = EntryBuilder.parseBody(entryId, file.decode());
        return Mono.justOrEmpty(parsed);
    }

    private Author toAuthor(Commit commit) {
        Committer committer = commit.getCommit().getAuthor();
        return new Author(committer.getName(), committer.getDate());
    }
}
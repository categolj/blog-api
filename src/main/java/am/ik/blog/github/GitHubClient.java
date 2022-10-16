package am.ik.blog.github;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "/repos/{owner}/{repo}")
public interface GitHubClient {
	@GetExchange(url = "/contents/{path}")
	Mono<File> getFile(@PathVariable("owner") String owner, @PathVariable("repo") String repo, @PathVariable("path") String path);

	@GetExchange(url = "/commits")
	Flux<Commit> getCommits(@PathVariable("owner") String owner, @PathVariable("repo") String repo, @RequestParam MultiValueMap<String, String> params);
}

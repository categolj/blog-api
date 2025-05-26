package am.ik.blog.github;

import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange(url = "/repos/{owner}/{repo}")
public interface GitHubClient {

	@GetExchange(url = "/contents/{path}")
	ResponseEntity<File> getFile(@PathVariable("owner") String owner, @PathVariable("repo") String repo,
			@PathVariable("path") String path);

	@GetExchange(url = "/commits")
	List<Commit> getCommits(@PathVariable("owner") String owner, @PathVariable("repo") String repo,
			@RequestParam MultiValueMap<String, String> params);

}

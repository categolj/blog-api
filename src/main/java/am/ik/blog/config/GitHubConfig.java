package am.ik.blog.config;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import am.ik.blog.github.Committer;
import am.ik.blog.github.GitCommit;
import am.ik.blog.github.GitCommitter;
import am.ik.blog.github.GitHubClient;
import am.ik.blog.github.GitHubProps;
import am.ik.blog.github.GitHubUserContentClient;
import am.ik.blog.github.Parent;
import am.ik.blog.github.Tree;
import am.ik.blog.github.web.WebhookController;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.http.HttpHeaders;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.support.RestTemplateAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(GitHubConfig.RuntimeHints.class)
public class GitHubConfig {

	@Bean
	public GitHubClient gitHubClient(GitHubProps props,
			RestTemplateBuilder restTemplateBuilder) {
		final RestTemplateAdapter adapter = RestTemplateAdapter
				.create(restTemplateBuilder.rootUri("https://api.github.com")
						.defaultHeader(HttpHeaders.AUTHORIZATION,
								"token %s".formatted(props.getAccessToken()))
						.build());
		final HttpServiceProxyFactory factory = HttpServiceProxyFactory
				.builderFor(adapter).build();
		return factory.createClient(GitHubClient.class);
	}

	@Bean
	public Map<String, GitHubClient> tenantsGitHubClient(GitHubProps props,
			RestTemplateBuilder restTemplateBuilder) {
		return props.getTenants().entrySet().stream()
				.collect(Collectors.toUnmodifiableMap(Entry::getKey, e -> {
					final RestTemplateAdapter adapter = RestTemplateAdapter
							.create(restTemplateBuilder.rootUri("https://api.github.com")
									.defaultHeader(HttpHeaders.AUTHORIZATION,
											"token %s".formatted(
													e.getValue().getAccessToken()))
									.build());
					final HttpServiceProxyFactory factory = HttpServiceProxyFactory
							.builderFor(adapter).build();
					return factory.createClient(GitHubClient.class);
				}));
	}

	@Bean
	public GitHubUserContentClient gitHubUserContentClient(GitHubProps props,
			RestTemplateBuilder restTemplateBuilder) {
		final RestTemplateAdapter adapter = RestTemplateAdapter
				.create(restTemplateBuilder.rootUri("https://raw.githubusercontent.com")
						.defaultHeader(HttpHeaders.AUTHORIZATION,
								"token %s".formatted(props.getAccessToken()))
						.build());
		final HttpServiceProxyFactory factory = HttpServiceProxyFactory
				.builderFor(adapter).build();
		return factory.createClient(GitHubUserContentClient.class);
	}

	static class RuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(org.springframework.aot.hint.RuntimeHints hints,
				ClassLoader classLoader) {
			hints.reflection()
					.registerMethod(
							Objects.requireNonNull(ReflectionUtils.findMethod(
									WebhookController.class, "node", String.class)),
							ExecutableMode.INVOKE)
					.registerMethod(
							Objects.requireNonNull(ReflectionUtils.findMethod(
									WebhookController.class, "paths", JsonNode.class)),
							ExecutableMode.INVOKE);
			hints.reflection()
					.registerConstructor(GitCommit.class.getDeclaredConstructors()[0],
							ExecutableMode.INVOKE)
					.registerConstructor(GitCommitter.class.getDeclaredConstructors()[0],
							ExecutableMode.INVOKE)
					.registerConstructor(Committer.class.getDeclaredConstructors()[0],
							ExecutableMode.INVOKE)
					.registerConstructor(Parent.class.getDeclaredConstructors()[0],
							ExecutableMode.INVOKE)
					.registerConstructor(Tree.class.getDeclaredConstructors()[0],
							ExecutableMode.INVOKE);
		}
	}
}

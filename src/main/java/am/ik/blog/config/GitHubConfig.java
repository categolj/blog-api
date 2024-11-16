package am.ik.blog.config;

import java.util.List;
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
import am.ik.spring.http.client.RetryableClientHttpRequestInterceptor;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Nullable;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestTemplateAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(GitHubConfig.RuntimeHints.class)
public class GitHubConfig {

	@Bean
	public RestTemplateCustomizer restTemplateCustomizer(GitHubProps props,
			LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor) {
		return restTemplate -> {
			restTemplate.setInterceptors(List.of(logbookClientHttpRequestInterceptor,
					new RetryableClientHttpRequestInterceptor(props.getBackOff())));
			restTemplate.setRequestFactory(new JdkClientHttpRequestFactory());
		};
	}

	@Bean
	public GitHubClient gitHubClient(GitHubProps props, RestTemplateBuilder restTemplateBuilder) {
		final RestTemplate restTemplate = restTemplateBuilder //
			.rootUri(props.getApiUrl()) //
			.setConnectTimeout(props.getConnectTimeout()) //
			.setReadTimeout(props.getReadTimeout()) //
			.defaultHeader(HttpHeaders.AUTHORIZATION, "token %s".formatted(props.getAccessToken())) //
			.build();
		final RestTemplateAdapter adapter = RestTemplateAdapter.create(restTemplate);
		final HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
		return factory.createClient(GitHubClient.class);
	}

	@Bean
	public Map<String, GitHubClient> tenantsGitHubClient(GitHubProps props, RestTemplateBuilder restTemplateBuilder) {
		return props.getTenants().entrySet().stream().collect(Collectors.toUnmodifiableMap(Entry::getKey, e -> {
			final GitHubProps tenantProps = e.getValue();
			final RestTemplate restTemplate = restTemplateBuilder //
				.rootUri(props.getApiUrl()) //
				.setConnectTimeout(tenantProps.getConnectTimeout()) //
				.setReadTimeout(tenantProps.getReadTimeout()) //
				.defaultHeader(HttpHeaders.AUTHORIZATION, "token %s".formatted(tenantProps.getAccessToken())) //
				.build();
			final RestTemplateAdapter adapter = RestTemplateAdapter.create(restTemplate);
			final HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
			return factory.createClient(GitHubClient.class);
		}));
	}

	@Bean
	public GitHubUserContentClient gitHubUserContentClient(GitHubProps props, RestTemplateBuilder restTemplateBuilder) {
		final RestTemplate restTemplate = restTemplateBuilder //
			.rootUri("https://raw.githubusercontent.com") //
			.setConnectTimeout(props.getConnectTimeout()) //
			.setReadTimeout(props.getReadTimeout()) //
			.defaultHeader(HttpHeaders.AUTHORIZATION, "token %s".formatted(props.getAccessToken())) //
			.build();
		final RestTemplateAdapter adapter = RestTemplateAdapter.create(restTemplate);
		final HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
		return factory.createClient(GitHubUserContentClient.class);
	}

	static class RuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(org.springframework.aot.hint.RuntimeHints hints, @Nullable ClassLoader classLoader) {
			hints.reflection()
				.registerMethod(
						Objects
							.requireNonNull(ReflectionUtils.findMethod(WebhookController.class, "node", String.class)),
						ExecutableMode.INVOKE)
				.registerMethod(
						Objects.requireNonNull(
								ReflectionUtils.findMethod(WebhookController.class, "paths", JsonNode.class)),
						ExecutableMode.INVOKE);
			hints.reflection()
				.registerConstructor(GitCommit.class.getDeclaredConstructors()[0], ExecutableMode.INVOKE)
				.registerConstructor(GitCommitter.class.getDeclaredConstructors()[0], ExecutableMode.INVOKE)
				.registerConstructor(Committer.class.getDeclaredConstructors()[0], ExecutableMode.INVOKE)
				.registerConstructor(Parent.class.getDeclaredConstructors()[0], ExecutableMode.INVOKE)
				.registerConstructor(Tree.class.getDeclaredConstructors()[0], ExecutableMode.INVOKE);
		}

	}

}

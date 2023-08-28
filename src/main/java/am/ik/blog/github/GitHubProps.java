package am.ik.blog.github;

import am.ik.yavi.builder.ValidatorBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.time.Duration;
import java.util.Map;
import java.util.function.BiConsumer;

import static am.ik.yavi.constraint.charsequence.codepoints.AsciiCodePoints.ASCII_PRINTABLE_CHARS;

@ConfigurationProperties(prefix = "blog.github")
@Component
public class GitHubProps implements org.springframework.validation.Validator {

	private String accessToken;

	private String webhookSecret;

	private String contentOwner;

	private String contentRepo;

	private Map<String, GitHubProps> tenants = Map.of();

	private Duration retryInterval = Duration.ofMillis(500);

	private Duration retryMaxElapsedTime = Duration.ofSeconds(4);

	private Duration connectTimeout = Duration.ofSeconds(5);

	private Duration readTimeout = Duration.ofSeconds(5);

	private final BiConsumer<GitHubProps, Errors> validator = ValidatorBuilder.<GitHubProps>of()
		.constraint(GitHubProps::getAccessToken, "accessToken", c -> c.codePoints(ASCII_PRINTABLE_CHARS).asWhiteList())
		.constraint(GitHubProps::getWebhookSecret, "webhookSecret",
				c -> c.codePoints(ASCII_PRINTABLE_CHARS).asWhiteList())
		.constraint(GitHubProps::getContentOwner, "contentOwner",
				c -> c.codePoints(ASCII_PRINTABLE_CHARS).asWhiteList())
		.constraint(GitHubProps::getContentRepo, "contentRepo", c -> c.codePoints(ASCII_PRINTABLE_CHARS).asWhiteList())
		.constraint(GitHubProps::getTenants, "tenants", c -> c.notNull())
		.constraintOnObject(GitHubProps::getRetryInterval, "retryInterval", c -> c.notNull())
		.constraintOnObject(GitHubProps::getRetryMaxElapsedTime, "retryMaxElapsedTime", c -> c.notNull())
		.constraintOnObject(GitHubProps::getReadTimeout, "readTimeout", c -> c.notNull())
		.constraintOnObject(GitHubProps::getConnectTimeout, "connectTimeout", c -> c.notNull())
		.constraintOnObject(GitHubProps::getConnectTimeout, "connectTimeout", c -> c.notNull())
		.build() //
		.toBiConsumer(Errors::rejectValue);

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getWebhookSecret() {
		return webhookSecret;
	}

	public void setWebhookSecret(String webhookSecret) {
		this.webhookSecret = webhookSecret;
	}

	public String getContentOwner() {
		return contentOwner;
	}

	public void setContentOwner(String contentOwner) {
		this.contentOwner = contentOwner;
	}

	public String getContentRepo() {
		return contentRepo;
	}

	public void setContentRepo(String contentRepo) {
		this.contentRepo = contentRepo;
	}

	public Map<String, GitHubProps> getTenants() {
		return tenants;
	}

	public void setTenants(Map<String, GitHubProps> tenants) {
		this.tenants = tenants;
	}

	public Duration getRetryInterval() {
		return retryInterval;
	}

	public void setRetryInterval(Duration retryInterval) {
		this.retryInterval = retryInterval;
	}

	public Duration getRetryMaxElapsedTime() {
		return retryMaxElapsedTime;
	}

	public void setRetryMaxElapsedTime(Duration retryMaxElapsedTime) {
		this.retryMaxElapsedTime = retryMaxElapsedTime;
	}

	public Duration getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(Duration connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public Duration getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(Duration readTimeout) {
		this.readTimeout = readTimeout;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == GitHubProps.class;
	}

	@Override
	public void validate(Object target, Errors errors) {
		this.validator.accept((GitHubProps) target, errors);
	}

}

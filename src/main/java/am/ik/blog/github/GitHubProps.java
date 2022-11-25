package am.ik.blog.github;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.BiValidator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;

import static am.ik.yavi.constraint.charsequence.codepoints.AsciiCodePoints.ASCII_PRINTABLE_CHARS;

@ConfigurationProperties(prefix = "blog.github")
@Component
@Validated
public class GitHubProps implements org.springframework.validation.Validator {

	private String accessToken;

	private String webhookSecret;

	private String contentOwner;

	private String contentRepo;

	private final BiValidator<GitHubProps, Errors> validator = ValidatorBuilder.<GitHubProps>of()
			.constraint(GitHubProps::getAccessToken, "accessToken", c -> c.codePoints(ASCII_PRINTABLE_CHARS).asWhiteList())
			.constraint(GitHubProps::getWebhookSecret, "webhookSecret", c -> c.codePoints(ASCII_PRINTABLE_CHARS).asWhiteList())
			.constraint(GitHubProps::getContentOwner, "contentOwner", c -> c.codePoints(ASCII_PRINTABLE_CHARS).asWhiteList())
			.constraint(GitHubProps::getContentRepo, "contentRepo", c -> c.codePoints(ASCII_PRINTABLE_CHARS).asWhiteList())
			.build(Errors::rejectValue);

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

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == GitHubProps.class;
	}

	@Override
	public void validate(Object target, Errors errors) {
		this.validator.accept((GitHubProps) target, errors);
	}
}

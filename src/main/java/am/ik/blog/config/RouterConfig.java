package am.ik.blog.config;

import am.ik.blog.admin.web.EntryImportHandler;
import am.ik.blog.entry.web.EntryHandler;
import am.ik.blog.github.web.WebhookHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {

	@Bean
	public RouterFunction<ServerResponse> routes(
			EntryHandler entryHandler,
			WebhookHandler webhookHandler,
			EntryImportHandler entryImportHandler) {
		return entryHandler.routes()
				.and(webhookHandler.routes())
				.and(entryImportHandler.routes());
	}
}

package am.ik.blog.config;

import am.ik.blog.admin.EntryImportHandler;
import am.ik.blog.entry.CategoryHandler;
import am.ik.blog.entry.EntryHandler;
import am.ik.blog.entry.TagHandler;
import am.ik.blog.entry.v2.CategoryV2Handler;
import am.ik.blog.entry.v2.EntryV2Handler;
import am.ik.blog.entry.v2.TagV2Handler;
import am.ik.blog.github.WebhookHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {
	@Bean
	public RouterFunction<ServerResponse> routes(EntryHandler entryHandler,
			TagHandler tagHandler, CategoryHandler categoryHandler,
			WebhookHandler webhookHandler, EntryImportHandler entryImportHandler,
			EntryV2Handler entryV2Handler, TagV2Handler tagV2Handler,
			CategoryV2Handler categoryV2Handler) {
		return entryHandler.routes() //
				.and(tagHandler.routes()) //
				.and(categoryHandler.routes()) //
				.and(entryV2Handler.routes(entryHandler)) //
				.and(tagV2Handler.routes()) //
				.and(categoryV2Handler.routes()) //
				.and(webhookHandler.routes()) //
				.and(entryImportHandler.routes());
	}
}

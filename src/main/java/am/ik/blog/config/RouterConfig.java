package am.ik.blog.config;

import am.ik.blog.service.admin.EntryImportHandler;
import am.ik.blog.service.github.WebhookHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(
        WebhookHandler webhookHandler,
        EntryImportHandler entryImportHandler) {
        return webhookHandler.routes()
            .and(entryImportHandler.routes());
    }
}

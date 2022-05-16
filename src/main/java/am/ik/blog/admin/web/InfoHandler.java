package am.ik.blog.admin.web;

import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Component
public class InfoHandler {
	private final InfoEndpoint infoEndpoint;

	public InfoHandler(InfoEndpoint infoEndpoint) {
		this.infoEndpoint = infoEndpoint;
	}

	public RouterFunction<ServerResponse> routes() {
		return route() //
				.GET("/info", req -> ServerResponse.ok().bodyValue(infoEndpoint.info())) //
				.build();
	}
}

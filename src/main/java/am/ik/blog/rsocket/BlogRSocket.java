package am.ik.blog.rsocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.netty.buffer.PooledByteBufAllocator;
import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.util.ByteBufPayload;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.web.server.ResponseStatusException;

/**
 * [Request] <br>
 * Payload#getData -> query <br>
 * Payload#getMetadata -> path <br>
 * 
 * [Response] <br>
 * Payload#getData -> body <br>
 **/
@Component
@ConfigurationProperties(prefix = "rsocket.server")
public class BlogRSocket extends AbstractRSocket {
	private static final Logger log = LoggerFactory.getLogger(BlogRSocket.class);
	private final List<RSocketRoute> routes;
	private final NettyDataBufferFactory dataBufferFactory = new NettyDataBufferFactory(
			PooledByteBufAllocator.DEFAULT);
	private RSocketCodec codec = RSocketCodec.SMILE;

	public BlogRSocket(List<RSocketRouter> routers) {
		ArrayList<RSocketRoute> routes = new ArrayList<>();
		routers.forEach(router -> routes.addAll(router.routes()));
		this.routes = routes;
		this.routes.forEach(route -> log.info("{}", route));
	}

	@Override
	public Flux<Payload> requestStream(Payload payload) {
		String path = this.getPath(payload);
		log.debug("[requestStream] {}", path);
		return Flux.fromIterable(this.routes) //
				.filter(route -> route.matches(path)) //
				.switchIfEmpty(this.routeNotFound(path)) //
				.flatMap(route -> route.invoke(path,
						RSocketQueryParams.parse(payload.getDataUtf8())))
				.flatMap(response -> this.toPayload(response.body(), response.type(),
						this.codec.streamMediaType()));
	}

	@Override
	public Mono<Payload> requestResponse(Payload payload) {
		String path = this.getPath(payload);
		log.debug("[requestResponse] {}", path);
		return Flux.fromIterable(this.routes) //
				.filter(route -> route.matches(path)) //
				.switchIfEmpty(this.routeNotFound(path)) //
				.flatMap(route -> route.invoke(path,
						RSocketQueryParams.parse(payload.getDataUtf8())))
				.flatMap(response -> this.toPayload(response.body(), response.type(),
						this.codec.singleMediaType())) //
				.next();
	}

	public Mono<RSocketRoute> routeNotFound(String path) {
		return Mono
				.defer(() -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
						"No route matches to " + path)));
	}

	private String getPath(Payload payload) {
		String path = payload.getMetadataUtf8();
		return path.startsWith("/") ? path : "/" + path;
	}

	private Flux<Payload> toPayload(Publisher<?> entries, ResolvableType elementType,
			MimeType mimeType) {
		return this.codec.encoder()
				.encode(entries, dataBufferFactory, elementType, mimeType,
						Collections.emptyMap())
				.cast(NettyDataBuffer.class)
				.map(dataBuffer -> ByteBufPayload.create(dataBuffer.getNativeBuffer()));
	}

	public RSocketCodec getCodec() {
		return codec;
	}

	public void setCodec(RSocketCodec codec) {
		this.codec = codec;
	}
}

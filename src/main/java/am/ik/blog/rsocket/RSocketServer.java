package am.ik.blog.rsocket;

import javax.annotation.PreDestroy;

import io.rsocket.RSocketFactory;
import io.rsocket.transport.ServerTransport;
import io.rsocket.transport.netty.server.NettyContextCloseable;
import io.rsocket.transport.netty.server.TcpServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rsocket.server")
public class RSocketServer {
	private static final Logger log = LoggerFactory.getLogger(RSocketServer.class);
	private final BlogRSocket blogRSocket;
	private int port = 7000;
	private NettyContextCloseable nettyContextCloseable;

	public RSocketServer(BlogRSocket blogRSocket) {
		this.blogRSocket = blogRSocket;
	}

	@Async
	@EventListener(ContextRefreshedEvent.class)
	public void start() {
		ServerTransport<NettyContextCloseable> transport = TcpServerTransport
				.create("0.0.0.0", port);
		Mono<NettyContextCloseable> start = RSocketFactory.receive() //
				.acceptor((setupPayload, reactiveSocket) -> Mono.just(this.blogRSocket)) //
				.transport(transport) //
				.start();
		this.nettyContextCloseable = start
				.doOnSuccess(x -> log.info("Started RSocketServer on {} (transport={})",
						x.address(), transport))
				.block();
		this.nettyContextCloseable.onClose().block();
	}

	@PreDestroy
	public void stop() {
		if (this.nettyContextCloseable != null) {
			this.nettyContextCloseable.dispose();
		}
		log.info("shutdown");
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}

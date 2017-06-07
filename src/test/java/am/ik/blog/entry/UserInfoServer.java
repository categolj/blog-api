package am.ik.blog.entry;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.remoting.support.SimpleHttpServerFactoryBean;
import org.springframework.util.StringUtils;

public class UserInfoServer {
	private final int port;

	public UserInfoServer(int port) {
		this.port = port;
	}

	SimpleHttpServerFactoryBean factoryBean;

	public void start() {
		Map<String, String> users = new HashMap<>();
		users.put("foo",
				"{\"login\":\"foo\",\"id\":0,\"name\":\"Taro Foo\",\"email\":\"foo@example.com\"}");
		users.put("test-user-1",
				"{\"login\":\"test-user-1\",\"id\":1,\"name\":\"Ichiro Test\",\"email\":\"test-user-1@example.com\"}");
		users.put("test-user-2",
				"{\"login\":\"test-user-2\",\"id\":2,\"name\":\"Jiro Test\",\"email\":\"test-user-2@example.com\"}");
		users.put("test-user-3",
				"{\"login\":\"test-user-3\",\"id\":3,\"name\":\"Saburo Test\",\"email\":\"test-user-3@example.com\"}");

		factoryBean = new SimpleHttpServerFactoryBean();
		factoryBean.setPort(port);
		factoryBean.setContexts(Collections.singletonMap("/user", (exec) -> {
			String s = exec.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION);
			if (StringUtils.isEmpty(s)) {
				exec.sendResponseHeaders(401, 0);
				return;
			}

			String token = s.substring(7);
			String json = users.get(token);
			exec.getResponseHeaders().add("Content-Type",
					"application/json;charset=UTF-8");
			exec.sendResponseHeaders(200, json.length());
			try (OutputStream stream = exec.getResponseBody()) {
				stream.write(json.getBytes());
			}
		}));
		try {
			factoryBean.afterPropertiesSet();
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public void shutdown() {
		factoryBean.destroy();
	}

	public static void main(String[] args) {
		UserInfoServer server = new UserInfoServer(34539);
		server.start();
	}
}

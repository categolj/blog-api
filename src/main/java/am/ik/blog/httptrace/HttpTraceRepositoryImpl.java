package am.ik.blog.httptrace;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HttpTraceRepositoryImpl implements HttpTraceRepository {
	private final HttpTraceMapper httpTraceMapper;

	public HttpTraceRepositoryImpl(HttpTraceMapper httpTraceMapper) {
		this.httpTraceMapper = httpTraceMapper;
	}


	@Override
	public List<HttpTrace> findAll() {
		return this.httpTraceMapper.findAll().collectList().block();
	}

	@Override
	public void add(HttpTrace trace) {
		final String path = trace.getRequest().getUri().getPath();
		if (path.startsWith("/actuator") || path.startsWith("/livez") || path.startsWith("/readyz")) {
			return;
		}
		this.httpTraceMapper.insert(trace).subscribe();
	}

	@Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
	void housekeeping() {
		this.httpTraceMapper.dropOldTraces().subscribe();
	}
}

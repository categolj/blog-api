package am.ik.blog.point;

import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import am.ik.blog.BlogProperties;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryId;
import am.ik.blog.exception.NotSubscribedException;

@Component
public class PointService {
	private final OAuth2RestTemplate oauth2RestTemplate;
	private final BlogProperties props;

	public PointService(OAuth2RestTemplate oauth2RestTemplate, BlogProperties props) {
		this.oauth2RestTemplate = oauth2RestTemplate;
		this.props = props;
	}

	@HystrixCommand(fallbackMethod = "subscribedIdsFallback")
	public Set<EntryId> subscribedIds() {
		JsonNode response = this.oauth2RestTemplate
				.getForObject(props.getPoint().getUrl() + "/v1/user", JsonNode.class);
		return StreamSupport.stream(response.get("entryIds").spliterator(), false)
				.map(x -> new EntryId(x.asLong())).collect(toSet());
	}

	public Set<EntryId> subscribedIdsFallback() {
		return Collections.emptySet();
	}

	public void checkIfSubscribed(Entry entry) {
		Set<EntryId> entryIds = subscribedIds();
		EntryId entryId = entry.entryId();
		if (!entryIds.contains(entryId)) {
			throw new NotSubscribedException("entry " + entryId + " is not subscribed.");
		}
	}
}

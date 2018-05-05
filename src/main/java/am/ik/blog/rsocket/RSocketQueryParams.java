package am.ik.blog.rsocket;

import java.util.Optional;
import java.util.OptionalInt;

import org.springframework.data.domain.PageRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class RSocketQueryParams {
	private final MultiValueMap<String, String> params;

	public static RSocketQueryParams parse(String query) {
		return new RSocketQueryParams(query);
	}

	private RSocketQueryParams(String query) {
		this.params = UriComponentsBuilder.newInstance().query(query).build()
				.getQueryParams();
	}

	public PageRequest pageRequest() {
		return PageRequest.of(this.page(), this.size());
	}

	public int page() {
		return this.asInt("page").orElse(0);
	}

	public int size() {
		return this.asInt("size").orElse(20);
	}

	public Optional<String> asString(String name) {
		String s = this.params.getFirst(name);
		if (StringUtils.isEmpty(s)) {
			return Optional.empty();
		}
		return Optional.of(s);

	}

	public Optional<Boolean> asBoolean(String name) {
		return this.asString(name).map(Boolean::parseBoolean);
	}

	public OptionalInt asInt(String name) {
		String s = this.params.getFirst(name);
		if (StringUtils.isEmpty(s)) {
			return OptionalInt.empty();
		}
		try {
			return OptionalInt.of(Integer.parseInt(s));
		}
		catch (NumberFormatException e) {
			return OptionalInt.empty();
		}
	}
}

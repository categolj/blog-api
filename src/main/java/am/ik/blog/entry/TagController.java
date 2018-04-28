package am.ik.blog.entry;

import java.util.List;

import am.ik.blog.reactive.ReactiveTagMapper;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(path = "api/tags")
public class TagController {
	private final ReactiveTagMapper tagMapper;

	public TagController(ReactiveTagMapper tagMapper) {
		this.tagMapper = tagMapper;
	}

	@GetMapping
	public Mono<List<String>> getTags() {
		return tagMapper.findOrderByTagNameAsc()
				.map(tags -> tags.stream().map(Tag::getValue).collect(toList()));
	}
}

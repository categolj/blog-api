package am.ik.blog.entry;

import java.util.List;

import am.ik.blog.reactive.ReactiveTagMapper;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(path = "api/tags")
@RequiredArgsConstructor
public class TagController {
	private final ReactiveTagMapper tagMapper;

	@GetMapping
	public Mono<List<String>> getTags() {
		return tagMapper.findOrderByTagNameAsc()
				.map(tags -> tags.stream().map(Tag::getValue).collect(toList()));
	}
}

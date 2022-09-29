package am.ik.blog.tag.rsocket;

import java.util.List;

import am.ik.blog.tag.Tag;
import am.ik.blog.tag.TagMapper;
import reactor.core.publisher.Mono;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TagRSocketController {

	private final TagMapper tagMapper;

	public TagRSocketController(TagMapper tagMapper) {
		this.tagMapper = tagMapper;
	}

	@MessageMapping("tags")
	public Mono<List<Tag>> tags() {
		return this.tagMapper.findOrderByTagNameAsc().collectList();
	}
}

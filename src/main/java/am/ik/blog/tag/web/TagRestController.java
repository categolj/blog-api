package am.ik.blog.tag.web;

import java.util.List;

import am.ik.blog.tag.Tag;
import am.ik.blog.tag.TagMapper;
import am.ik.blog.tag.TagNameAndCount;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TagRestController {
	private final TagMapper tagMapper;

	public TagRestController(TagMapper tagMapper) {
		this.tagMapper = tagMapper;
	}

	@GetMapping(path = "/tags")
	public List<TagNameAndCount> tags() {
		return this.tagMapper.findOrderByTagNameAsc();
	}
}

package am.ik.blog.tag.web;

import java.util.List;

import am.ik.blog.tag.TagMapper;
import am.ik.blog.tag.TagNameAndCount;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TagRestController {
	private final TagMapper tagMapper;

	public TagRestController(TagMapper tagMapper) {
		this.tagMapper = tagMapper;
	}

	@GetMapping(path = "/tags")
	public List<TagNameAndCount> tags() {
		return this.tags(null);
	}

	@GetMapping(path = "/tenants/{tenantId}/tags")
	public List<TagNameAndCount> tags(
			@PathVariable(name = "tenantId", required = false) String tenantId) {
		return this.tagMapper.findOrderByTagNameAsc(tenantId);
	}
}

package am.ik.blog.tag.web;

import java.util.List;

import am.ik.blog.tag.TagMapper;
import am.ik.blog.tag.TagAndCount;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "tag")
public class TagRestController {

	private final TagMapper tagMapper;

	public TagRestController(TagMapper tagMapper) {
		this.tagMapper = tagMapper;
	}

	@GetMapping(path = "/tags")
	public List<TagAndCount> tags() {
		return this.tagsForTenant(null);
	}

	@GetMapping(path = "/tenants/{tenantId}/tags")
	public List<TagAndCount> tagsForTenant(
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId) {
		return this.tagMapper.findOrderByTagNameAsc(tenantId);
	}

}

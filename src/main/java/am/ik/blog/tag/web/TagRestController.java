package am.ik.blog.tag.web;

import java.util.List;

import am.ik.blog.proto.ProtoUtils;
import am.ik.blog.proto.TagsResponse;
import am.ik.blog.tag.TagAndCount;
import am.ik.blog.tag.TagMapper;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
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

	@GetMapping(path = "/tags", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
	public TagsResponse tagsAsProtobuf() {
		return this.tagsAsProtobufForTenant(null);
	}

	@GetMapping(path = "/tenants/{tenantId}/tags", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
	public TagsResponse tagsAsProtobufForTenant(
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId) {
		return ProtoUtils.toProtoTagsResponse(this.tagMapper.findOrderByTagNameAsc(tenantId));
	}

}

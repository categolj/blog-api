package am.ik.blog.category.web;

import java.util.List;

import am.ik.blog.category.Category;
import am.ik.blog.category.CategoryMapper;
import am.ik.blog.proto.CategoriesResponse;
import am.ik.blog.proto.ProtoUtils;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "category")
public class CategoryRestController {

	private final CategoryMapper categoryMapper;

	public CategoryRestController(CategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
	}

	@GetMapping(path = "/categories")
	public List<List<Category>> categories() {
		return this.categoriesForTenant(null);
	}

	@GetMapping(path = "/tenants/{tenantId}/categories")
	public List<List<Category>> categoriesForTenant(
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId) {
		return this.categoryMapper.findAll(tenantId);
	}

	@GetMapping(path = "/categories", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
	public CategoriesResponse categoriesAsProtobuf() {
		return this.categoriesAsProtobufForTenant(null);
	}

	@GetMapping(path = "/tenants/{tenantId}/categories", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
	public CategoriesResponse categoriesAsProtobufForTenant(
			@Nullable @PathVariable(name = "tenantId", required = false) String tenantId) {
		return ProtoUtils.toProtoCategoriesResponse(this.categoryMapper.findAll(tenantId));
	}

}

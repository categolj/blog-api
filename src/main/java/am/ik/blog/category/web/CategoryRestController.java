package am.ik.blog.category.web;

import java.util.List;

import am.ik.blog.category.Category;
import am.ik.blog.category.CategoryMapper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryRestController {
	private final CategoryMapper categoryMapper;

	public CategoryRestController(CategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
	}

	@GetMapping(path = "/categories")
	public List<List<Category>> categories() {
		return this.categories(null);
	}

	@GetMapping(path = "/tenants/{tenantId}/categories")
	public List<List<Category>> categories(
			@PathVariable(name = "tenantId", required = false) String tenantId) {
		return this.categoryMapper.findAll(tenantId);
	}
}

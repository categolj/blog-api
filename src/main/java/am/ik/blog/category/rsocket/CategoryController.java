package am.ik.blog.category.rsocket;

import java.util.List;

import am.ik.blog.category.Category;
import am.ik.blog.category.CategoryMapper;
import reactor.core.publisher.Mono;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {

	private final CategoryMapper categoryMapper;

	public CategoryController(CategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
	}

	@MessageMapping("categories")
	public Mono<List<List<Category>>> categories() {
		return this.categoryMapper.findAll().collectList();
	}
}

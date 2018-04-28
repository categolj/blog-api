package am.ik.blog.entry;

import java.util.List;

import am.ik.blog.reactive.ReactiveCategoryMapper;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(path = "api/categories")
public class CategoryController {
	private final ReactiveCategoryMapper categoryMapper;

	public CategoryController(ReactiveCategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
	}

	@GetMapping
	public Mono<List<List<String>>> getCategories() {
		return categoryMapper.findAll().map(categories -> categories.stream()
				.map(c -> c.getValue().stream().map(Category::getValue).collect(toList()))
				.collect(toList()));
	}
}

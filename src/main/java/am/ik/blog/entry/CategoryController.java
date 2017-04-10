package am.ik.blog.entry;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "api/categories")
@RequiredArgsConstructor
public class CategoryController {
	private final CategoryMapper categoryMapper;

	@GetMapping
	List<List<String>> getCategories() {
		return categoryMapper.findAll().stream()
				.map(c -> c.getValue().stream().map(Category::getValue).collect(toList()))
				.collect(toList());
	}
}

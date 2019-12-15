package am.ik.blog.service.category;

import am.ik.blog.model.Category;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

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

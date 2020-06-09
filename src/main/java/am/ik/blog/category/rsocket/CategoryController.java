package am.ik.blog.category.rsocket;

import am.ik.blog.category.Category;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import am.ik.blog.category.CategoryMapper;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class CategoryController {

    private final CategoryMapper categoryMapper;

    public CategoryController(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @MessageMapping("categories")
    @NewSpan
    public Mono<List<List<Category>>> categories() {
        return this.categoryMapper.findAll().collectList();
    }
}

package am.ik.blog.service.tag;

import am.ik.blog.model.Tag;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class TagController {

    private final TagMapper tagMapper;

    public TagController(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    @MessageMapping("tags")
    public Mono<List<Tag>> tags() {
        return this.tagMapper.findOrderByTagNameAsc().collectList();
    }
}

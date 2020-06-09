package am.ik.blog.tag.rsocket;

import am.ik.blog.tag.Tag;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import am.ik.blog.tag.TagMapper;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class TagController {

    private final TagMapper tagMapper;

    public TagController(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    @MessageMapping("tags")
    @NewSpan
    public Mono<List<Tag>> tags() {
        return this.tagMapper.findOrderByTagNameAsc().collectList();
    }
}

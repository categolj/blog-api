package am.ik.blog.tag;

import reactor.core.publisher.Flux;

public interface TagMapper {
	//@NewSpan
	Flux<Tag> findOrderByTagNameAsc();
}

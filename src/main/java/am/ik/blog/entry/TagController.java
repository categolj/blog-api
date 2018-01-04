package am.ik.blog.entry;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "api/tags")
@RequiredArgsConstructor
public class TagController {
	private final TagMapper tagMapper;

	@GetMapping
	public List<String> getTags() {
		return tagMapper.findOrderByTagNameAsc().stream().map(Tag::getValue)
				.collect(toList());
	}
}

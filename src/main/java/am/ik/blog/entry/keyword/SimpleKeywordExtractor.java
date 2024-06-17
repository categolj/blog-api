package am.ik.blog.entry.keyword;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SimpleKeywordExtractor implements KeywordExtractor {

	@Override
	public List<String> extract(String text) {
		return Arrays.asList(text.split("[\\s\\u3000]+"));
	}

}

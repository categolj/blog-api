package am.ik.blog.entry.keyword;

import java.util.List;

public interface KeywordExtractor {
	List<String> extract(String text);
}

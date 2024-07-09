package am.ik.blog.entry.keyword;

import am.ik.query.RootNode;

import org.springframework.stereotype.Component;

@Component
public class QueryParser implements KeywordParser {

	@Override
	public RootNode parse(String text) {
		return am.ik.query.QueryParser.parseQuery(text);
	}

}

package am.ik.blog.entry.keyword;

import java.util.List;

import am.ik.query.RootNode;
import am.ik.query.TokenNode;
import am.ik.query.TokenType;

@Deprecated(forRemoval = true)
public interface KeywordExtractor extends KeywordParser {

	List<String> extract(String text);

	@Override
	default RootNode parse(String text) {
		RootNode root = new RootNode();
		this.extract(text).stream().map(t -> new TokenNode(TokenType.KEYWORD, t)).forEach(root.children()::add);
		return root;
	}

}

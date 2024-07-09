package am.ik.blog.entry.keyword;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import am.ik.query.Node;
import am.ik.query.RootNode;
import am.ik.query.TokenNode;
import am.ik.query.TokenType;

public interface KeywordParser {

	RootNode parse(String text);

	default QueryAndParams convert(String text) {
		return this.convert(this.parse(text), new AtomicInteger(0));
	}

	String COLUMN_NAME = "e.content";

	default QueryAndParams convert(RootNode root, AtomicInteger index) {
		StringBuilder builder = new StringBuilder();
		Map<String, String> params = new LinkedHashMap<>();
		Iterator<? extends Node> iterator = root.children().iterator();
		while (iterator.hasNext()) {
			Node node = iterator.next();
			if (Objects.requireNonNull(node) instanceof TokenNode token) {
				if (!builder.isEmpty()) {
					if (Objects.requireNonNull(token.type()) == TokenType.OR) {
						builder.append(" OR ");
					}
					else {
						builder.append(" AND ");
					}
				}
				String paramName = "keyword_" + index.getAndIncrement();
				if (token.type() == TokenType.OR) {
					if (iterator.hasNext()) {
						builder.append(COLUMN_NAME).append(" ILIKE :").append(paramName);
						params.put(paramName, iterator.next().value());
					}
				}
				else if (token.type() == TokenType.EXCLUDE) {
					builder.append(COLUMN_NAME).append(" NOT ILIKE :").append(paramName);
					params.put(paramName, token.value());
				}
				else {
					builder.append(COLUMN_NAME).append(" ILIKE :").append(paramName);
					params.put(paramName, token.value());
				}
			}
			else if (node instanceof RootNode nest) {
				if (!builder.isEmpty()) {
					builder.append(" AND ");
				}
				QueryAndParams queryAndParams = convert(nest, index);
				builder.append("(").append(queryAndParams.query).append(")");
				params.putAll(queryAndParams.params);
			}
		}
		return new QueryAndParams(builder.toString(), params);
	}

	record QueryAndParams(String query, Map<String, String> params) {
	}

}

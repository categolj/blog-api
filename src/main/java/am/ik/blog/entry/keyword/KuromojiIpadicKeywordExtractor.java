package am.ik.blog.entry.keyword;

import java.util.List;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

import org.springframework.stereotype.Component;

@Component
public class KuromojiIpadicKeywordExtractor implements KeywordExtractor {
	@Override
	public List<String> extract(String text) {
		final Tokenizer tokenizer = new Tokenizer();
		return tokenizer.tokenize(text).stream().filter(token -> {
			final String surface = token.getSurface();
			final String partOfSpeechLevel1 = token.getPartOfSpeechLevel1();
			final String partOfSpeechLevel2 = token.getPartOfSpeechLevel2();
			return surface.length() > 1 && partOfSpeechLevel1.equals("名詞")
					&& (partOfSpeechLevel2.equals("一般")
							|| partOfSpeechLevel2.equals("固有名詞"));
		}).map(Token::getSurface).map(String::toUpperCase).sorted().distinct().toList();
	}
}

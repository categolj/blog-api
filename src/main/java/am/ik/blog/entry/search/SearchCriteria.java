package am.ik.blog.entry.search;

import am.ik.blog.category.Category;
import am.ik.blog.entry.keyword.KeywordExtractor;
import am.ik.blog.tag.Tag;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.util.StringUtils;

import java.util.List;

public class SearchCriteria {

	public static final SearchCriteria DEFAULT = defaults().build();

	public static final String SERIES = "Series";

	private boolean excludeContent;

	private String createdBy;

	private String lastModifiedBy;

	private Tag tag;

	private List<Category> categories;

	private String keyword;

	SearchCriteria(boolean excludeContent, String createdBy, String lastModifiedBy,
			Tag tag, List<Category> categories, String keyword) {
		this.excludeContent = excludeContent;
		this.createdBy = createdBy;
		this.lastModifiedBy = lastModifiedBy;
		this.tag = tag;
		this.categories = categories;
		this.keyword = keyword;
	}

	public static SearchCriteriaBuilder builder() {
		return new SearchCriteriaBuilder();
	}

	public static SearchCriteria.SearchCriteriaBuilder defaults() {
		return SearchCriteria.builder().excludeContent();
	}

	public List<Category> getCategories() {
		return categories;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public String getKeyword() {
		return this.keyword;
	}

	public String getLastModifiedBy() {
		return this.lastModifiedBy;
	}

	public Tag getTag() {
		return this.tag;
	}

	public boolean isExcludeContent() {
		return this.excludeContent;
	}

	boolean hasKeywords() {
		return StringUtils.hasText(this.keyword);
	}

	public MapSqlParameterSource toParameterSource(KeywordExtractor keywordExtractor) {
		final MapSqlParameterSource params = new MapSqlParameterSource();
		if (this.hasKeywords()) {
			final List<String> keywords = keywordExtractor.extract(this.keyword);
			params.addValue("keywordsCount", keywords.size());
			for (int i = 0; i < keywords.size(); i++) {
				params.addValue("keywords[%d]".formatted(i), keywords.get(i));
			}
		}
		else {
			params.addValue("keywordsCount", 0);
		}
		if (this.createdBy != null) {
			params.addValue("createdBy", this.createdBy);
		}
		if (this.lastModifiedBy != null) {
			params.addValue("lastModifiedBy", this.createdBy);
		}
		if (this.tag != null) {
			params.addValue("tag", tag.name());
		}
		if (this.categories != null) {
			params.addValue("categories", this.categories);
			for (int i = 0; i < categories.size(); i++) {
				params.addValue("categories[%d]".formatted(i), categories.get(i).name());
			}
		}
		params.addValue("excludeContent", this.excludeContent);
		return params;
	}

	@Override
	public String toString() {
		return "SearchCriteria{" + "excludeContent=" + excludeContent + ", createdBy='"
				+ createdBy + '\'' + ", lastModifiedBy='" + lastModifiedBy + '\''
				+ ", tag=" + tag + ", categories=" + categories + ", keyword='" + keyword
				+ '\'' + '}';
	}

	public static class SearchCriteriaBuilder {

		private List<Category> categories;

		private String createdBy;

		private boolean excludeContent;

		private String keyword;

		private String lastModifiedBy;

		private Tag tag;

		SearchCriteriaBuilder() {
		}

		public SearchCriteria build() {
			return new SearchCriteria(excludeContent, createdBy, lastModifiedBy, tag,
					categories, keyword);
		}

		public SearchCriteriaBuilder categories(List<Category> categories) {
			this.categories = categories;
			return this;
		}

		public SearchCriteriaBuilder createdBy(String createdBy) {
			this.createdBy = createdBy;
			return this;
		}

		public SearchCriteriaBuilder excludeContent(boolean excludeContent) {
			this.excludeContent = excludeContent;
			return this;
		}

		public SearchCriteriaBuilder excludeContent() {
			return this.excludeContent(true);
		}

		public SearchCriteriaBuilder includeContent() {
			return this.excludeContent(false);
		}

		public SearchCriteriaBuilder keyword(String keyword) {
			this.keyword = keyword;
			return this;
		}

		public SearchCriteriaBuilder lastModifiedBy(String lastModifiedBy) {
			this.lastModifiedBy = lastModifiedBy;
			return this;
		}

		public SearchCriteriaBuilder tag(Tag tag) {
			this.tag = tag;
			return this;
		}
	}
}

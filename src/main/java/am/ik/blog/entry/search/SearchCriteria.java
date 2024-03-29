package am.ik.blog.entry.search;

import am.ik.blog.category.Category;
import am.ik.blog.entry.keyword.KeywordExtractor;
import am.ik.blog.tag.Tag;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.List;

public class SearchCriteria {

	public static final SearchCriteria DEFAULT = defaults().build();

	public static final String SERIES = "Series";

	private final boolean excludeEntryId;

	private final boolean excludeTitle;

	private final boolean excludeContent;

	private final boolean excludeCategories;

	private final boolean excludeTags;

	private final boolean excludeCreatedBy;

	private final boolean excludeCreatedDate;

	private final boolean excludeLastModifiedBy;

	private final boolean excludeLastModifiedDate;

	@Nullable
	private final String createdBy;

	@Nullable
	private final String lastModifiedBy;

	@Nullable
	private final Tag tag;

	@Nullable
	private final List<Category> categories;

	@Nullable
	private final String keyword;

	SearchCriteria(boolean excludeEntryId, boolean excludeTitle, boolean excludeContent, boolean excludeCategories,
			boolean excludeTags, boolean excludeCreatedBy, boolean excludeCreatedDate, boolean excludeLastModifiedBy,
			boolean excludeLastModifiedDate, @Nullable String createdBy, @Nullable String lastModifiedBy,
			@Nullable Tag tag, @Nullable List<Category> categories, @Nullable String keyword) {
		this.excludeEntryId = excludeEntryId;
		this.excludeTitle = excludeTitle;
		this.excludeContent = excludeContent;
		this.excludeCategories = excludeCategories;
		this.excludeTags = excludeTags;
		this.excludeCreatedBy = excludeCreatedBy;
		this.excludeCreatedDate = excludeCreatedDate;
		this.excludeLastModifiedBy = excludeLastModifiedBy;
		this.excludeLastModifiedDate = excludeLastModifiedDate;
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

	@Nullable
	public List<Category> getCategories() {
		return categories;
	}

	@Nullable
	public String getCreatedBy() {
		return this.createdBy;
	}

	@Nullable
	public String getKeyword() {
		return this.keyword;
	}

	@Nullable
	public String getLastModifiedBy() {
		return this.lastModifiedBy;
	}

	@Nullable
	public Tag getTag() {
		return this.tag;
	}

	public boolean isExcludeEntryId() {
		return excludeEntryId;
	}

	public boolean isExcludeTitle() {
		return excludeTitle;
	}

	public boolean isExcludeContent() {
		return this.excludeContent;
	}

	public boolean isExcludeCategories() {
		return excludeCategories;
	}

	public boolean isExcludeTags() {
		return excludeTags;
	}

	public boolean isExcludeCreatedBy() {
		return excludeCreatedBy;
	}

	public boolean isExcludeCreatedDate() {
		return excludeCreatedDate;
	}

	public boolean isExcludeLastModifiedBy() {
		return excludeLastModifiedBy;
	}

	public boolean isExcludeLastModifiedDate() {
		return excludeLastModifiedDate;
	}

	public MapSqlParameterSource toParameterSource(KeywordExtractor keywordExtractor) {
		final MapSqlParameterSource params = new MapSqlParameterSource();
		if (StringUtils.hasText(this.keyword)) {
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
		params.addValue("excludeEntryId", this.excludeEntryId);
		params.addValue("excludeTitle", this.excludeTitle);
		params.addValue("excludeContent", this.excludeContent);
		params.addValue("excludeCategories", this.excludeCategories);
		params.addValue("excludeTags", this.excludeTags);
		params.addValue("excludeCreatedBy", this.excludeCreatedBy);
		params.addValue("excludeCreatedDate", this.excludeCreatedDate);
		params.addValue("excludeLastModifiedBy", this.excludeLastModifiedBy);
		params.addValue("excludeLastModifiedDate", this.excludeLastModifiedDate);
		return params;
	}

	@Override
	public String toString() {
		return "SearchCriteria{" + "excludeEntryId=" + excludeEntryId + ", excludeTitle=" + excludeTitle
				+ ", excludeContent=" + excludeContent + ", excludeCategories=" + excludeCategories + ", excludeTags="
				+ excludeTags + ", excludeCreatedBy=" + excludeCreatedBy + ", excludeCreatedDate=" + excludeCreatedDate
				+ ", excludeLastModifiedBy=" + excludeLastModifiedBy + ", excludeLastModifiedDate="
				+ excludeLastModifiedDate + ", createdBy='" + createdBy + '\'' + ", lastModifiedBy='" + lastModifiedBy
				+ '\'' + ", tag=" + tag + ", categories=" + categories + ", keyword='" + keyword + '\'' + '}';
	}

	public static class SearchCriteriaBuilder {

		@Nullable
		private List<Category> categories;

		@Nullable
		private String createdBy;

		private boolean excludeEntryId = false;

		private boolean excludeTitle = false;

		private boolean excludeContent = false;

		private boolean excludeCategories = false;

		private boolean excludeTags = false;

		private boolean excludeCreatedBy = false;

		private boolean excludeCreatedDate = false;

		private boolean excludeLastModifiedBy = false;

		private boolean excludeLastModifiedDate = false;

		@Nullable
		private String keyword;

		@Nullable
		private String lastModifiedBy;

		@Nullable
		private Tag tag;

		SearchCriteriaBuilder() {
		}

		public SearchCriteria build() {
			return new SearchCriteria(excludeEntryId, excludeTitle, excludeContent, excludeCategories, excludeTags,
					excludeCreatedBy, excludeCreatedDate, excludeLastModifiedBy, excludeLastModifiedDate, createdBy,
					lastModifiedBy, tag, categories, keyword);
		}

		public SearchCriteriaBuilder categories(List<Category> categories) {
			this.categories = categories;
			return this;
		}

		public SearchCriteriaBuilder stringCategories(List<String> categories) {
			this.categories = (categories == null) ? List.of() : categories.stream().map(Category::new).toList();
			return this;
		}

		public SearchCriteriaBuilder createdBy(String createdBy) {
			this.createdBy = createdBy;
			return this;
		}

		public SearchCriteriaBuilder excludeEntryId(boolean excludeEntryId) {
			this.excludeEntryId = excludeEntryId;
			return this;
		}

		public SearchCriteriaBuilder excludeTitle(boolean excludeTitle) {
			this.excludeTitle = excludeTitle;
			return this;
		}

		public SearchCriteriaBuilder excludeContent(boolean excludeContent) {
			this.excludeContent = excludeContent;
			return this;
		}

		public SearchCriteriaBuilder excludeCategories(boolean excludeCategories) {
			this.excludeCategories = excludeCategories;
			return this;
		}

		public SearchCriteriaBuilder excludeTags(boolean excludeTags) {
			this.excludeTags = excludeTags;
			return this;
		}

		public SearchCriteriaBuilder excludeCreatedBy(boolean excludeCreatedBy) {
			this.excludeCreatedBy = excludeCreatedBy;
			return this;
		}

		public SearchCriteriaBuilder excludeCreatedDate(boolean excludeCreatedDate) {
			this.excludeCreatedDate = excludeCreatedDate;
			return this;
		}

		public SearchCriteriaBuilder excludeLastModifiedBy(boolean excludeLastModifiedBy) {
			this.excludeLastModifiedBy = excludeLastModifiedBy;
			return this;
		}

		public SearchCriteriaBuilder excludeLastModifiedDate(boolean excludeLastModifiedDate) {
			this.excludeLastModifiedDate = excludeLastModifiedDate;
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

		public SearchCriteriaBuilder tag(@Nullable String tag) {
			this.tag = (tag == null) ? null : new Tag(tag);
			return this;
		}

	}

}

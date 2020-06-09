package am.ik.blog.entry.search;

import am.ik.blog.tag.Tag;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchCriteria {

    public static final SearchCriteria DEFAULT = defaults().build();
    public static final String SERIES = "Series";

    private boolean excludeContent;

    private String createdBy;

    private String lastModifiedBy;

    private Tag tag;

    private CategoryOrders categoryOrders;

    private String keyword;

    SearchCriteria(boolean excludeContent, String createdBy, String lastModifiedBy, Tag tag,
                   CategoryOrders categoryOrders, String keyword) {
        this.excludeContent = excludeContent;
        this.createdBy = createdBy;
        this.lastModifiedBy = lastModifiedBy;
        this.tag = tag;
        this.categoryOrders = categoryOrders;
        this.keyword = keyword;
    }

    public static SearchCriteriaBuilder builder() {
        return new SearchCriteriaBuilder();
    }

    public static SearchCriteria.SearchCriteriaBuilder defaults() {
        return SearchCriteria.builder().excludeContent(true);
    }

    public CategoryOrders getCategoryOrders() {
        return this.categoryOrders;
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

    boolean isExcludeSeries() {
        return this.tag == null && this.categoryOrders == null && !this.hasKeyword();
    }

    boolean hasKeyword() {
        return !StringUtils.isEmpty(this.keyword);
    }

    public String toJoinClause() {
        StringBuilder sb = new StringBuilder();
        if (this.tag != null) {
            sb.append("LEFT JOIN entry_tag AS et ON e.entry_id = et.entry_id ");
        }
        if (this.categoryOrders != null) {
            sb.append("LEFT JOIN category AS c ON e.entry_id = c.entry_id ");
        }
        return sb.toString();
    }

    public ClauseAndParams toWhereClause() {
        AtomicInteger i = new AtomicInteger(1);
        Map<String, String> clause = new LinkedHashMap<>();
        Map<String, Object> params = new HashMap<>();

        if (this.hasKeyword()) {
            params.put("$" + i, "%" + this.keyword + "%");
            clause.put("$" + i, "AND e.content LIKE $" + i);
            i.incrementAndGet();
        }
        if (this.createdBy != null) {
            params.put("$" + i, this.createdBy);
            clause.put("$" + i, "AND e.created_by = $" + i);
            i.incrementAndGet();
        }
        if (this.lastModifiedBy != null) {
            params.put("$" + i, this.lastModifiedBy);
            clause.put("$" + i, "AND e.last_modified_by = $" + i);
            i.incrementAndGet();
        }
        if (this.tag != null) {
            params.put("$" + i, this.tag.getName());
            clause.put("$" + i, "AND et.tag_name = $" + i);
            i.incrementAndGet();
        } else if (this.isExcludeSeries()) {
            params.put("$" + i, SERIES);
            clause.put("$" + i, "AND e.entry_id NOT IN (SELECT entry_id FROM entry_tag WHERE tag_name = $" + i + ")");
            i.incrementAndGet();
        }
        if (this.categoryOrders != null) {
            this.categoryOrders.getValue().forEach(c -> {
                int categoryOrder = c.getCategoryOrder();
                String categoryStringKey = "$" + i;
                String categoryOrderKey = "$" + i.incrementAndGet();
                params.put(categoryStringKey, c.getCategory().getName());
                clause.put(categoryStringKey, "AND c.category_name = " + categoryStringKey);
                params.put(categoryOrderKey, categoryOrder);
                clause.put(categoryOrderKey, "AND c.category_order = " + categoryOrderKey);
                i.incrementAndGet();
            });
        }
        return new ClauseAndParams(clause, params);
    }

    public static class ClauseAndParams {

        private final Map<String, String> clause;

        private final Map<String, Object> params;

        ClauseAndParams(Map<String, String> clause, Map<String, Object> params) {
            this.clause = clause;
            this.params = params;
        }

        public String clauseForEntryId() {
            return String.join(" ", this.clause.values());
        }

        public Map<String, Object> params() {
            return this.params;
        }
    }

    public static class SearchCriteriaBuilder {

        private CategoryOrders categoryOrders;

        private String createdBy;

        private boolean excludeContent;

        private String keyword;

        private String lastModifiedBy;

        private Tag tag;

        SearchCriteriaBuilder() {
        }

        public SearchCriteria build() {
            return new SearchCriteria(excludeContent, createdBy, lastModifiedBy, tag,
                    categoryOrders, keyword);
        }

        public SearchCriteriaBuilder categoryOrders(CategoryOrders categoryOrders) {
            this.categoryOrders = categoryOrders;
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

    @Override
    public String toString() {
        return "SearchCriteria{" +
                "excludeContent=" + excludeContent +
                ", createdBy='" + createdBy + '\'' +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", tag=" + tag +
                ", categoryOrders=" + categoryOrders +
                ", keyword='" + keyword + '\'' +
                '}';
    }
}

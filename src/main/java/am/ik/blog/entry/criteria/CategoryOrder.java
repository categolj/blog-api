package am.ik.blog.entry.criteria;

import am.ik.blog.entry.Category;

public class CategoryOrder {
	private final Category category;
	private final int categoryOrder;

	@java.beans.ConstructorProperties({ "category", "categoryOrder" })
	public CategoryOrder(Category category, int categoryOrder) {
		this.category = category;
		this.categoryOrder = categoryOrder;
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof CategoryOrder)) {
			return false;
		}
		final CategoryOrder other = (CategoryOrder) o;
		if (!other.canEqual((Object) this)) {
			return false;
		}
		final Object this$category = this.getCategory();
		final Object other$category = other.getCategory();
		if (this$category == null ? other$category != null
				: !this$category.equals(other$category)) {
			return false;
		}
		if (this.getCategoryOrder() != other.getCategoryOrder()) {
			return false;
		}
		return true;
	}

	public Category getCategory() {
		return this.category;
	}

	public int getCategoryOrder() {
		return this.categoryOrder;
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $category = this.getCategory();
		result = result * PRIME + ($category == null ? 43 : $category.hashCode());
		result = result * PRIME + this.getCategoryOrder();
		return result;
	}

	public String toString() {
		return "CategoryOrder(category=" + this.getCategory() + ", categoryOrder="
				+ this.getCategoryOrder() + ")";
	}

	protected boolean canEqual(Object other) {
		return other instanceof CategoryOrder;
	}
}

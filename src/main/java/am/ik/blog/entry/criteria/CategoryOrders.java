package am.ik.blog.entry.criteria;

import java.util.HashSet;
import java.util.Set;

import am.ik.blog.entry.Category;

public class CategoryOrders {
	private final Set<CategoryOrder> value = new HashSet<>();

	public CategoryOrders() {
	}

	public CategoryOrders add(CategoryOrder categoryOrder) {
		value.add(categoryOrder);
		return this;
	}

	public CategoryOrders add(Category category, int categoryOrder) {
		return add(new CategoryOrder(category, categoryOrder));
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof CategoryOrders)) {
			return false;
		}
		final CategoryOrders other = (CategoryOrders) o;
		if (!other.canEqual((Object) this)) {
			return false;
		}
		final Object this$value = this.getValue();
		final Object other$value = other.getValue();
		if (this$value == null ? other$value != null : !this$value.equals(other$value)) {
			return false;
		}
		return true;
	}

	public Set<CategoryOrder> getValue() {
		return this.value;
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $value = this.getValue();
		result = result * PRIME + ($value == null ? 43 : $value.hashCode());
		return result;
	}

	public String toString() {
		return "CategoryOrders(value=" + this.getValue() + ")";
	}

	protected boolean canEqual(Object other) {
		return other instanceof CategoryOrders;
	}
}

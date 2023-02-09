package am.ik.blog.pagination;

import java.util.List;

public record OffsetPage<T>(List<T> content, int size, int number,
							long totalElements) {

	public long totalPages() {
		return this.size == 0 ? 1 : (int) Math.ceil((double) this.totalElements / (double) this.size);
	}

	public boolean hasNext() {
		return this.number + 1 < this.totalPages();
	}

	public boolean hasPrevious() {
		return this.number > 0;
	}
}

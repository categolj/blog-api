package am.ik.blog.pagination;

public record OffsetPageRequest(int pageNumber, int pageSize) {
	public int offset() {
		return this.pageNumber * this.pageSize;
	}
}

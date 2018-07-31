package am.ik.blog.support;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.reactive.function.server.ServerRequest;

public class PageableImpl implements Pageable {
	private final PageRequest pageRequest;

	public PageableImpl(ServerRequest request) {
		int page = request.queryParam("page").map(Integer::valueOf).orElse(0);
		int size = request.queryParam("size").map(Integer::valueOf).orElse(10);
		this.pageRequest = PageRequest.of(page, size);
	}

	@Override
	public Sort getSort() {
		return pageRequest.getSort();
	}

	@Override
	public Pageable next() {
		return pageRequest.next();
	}

	public PageRequest previous() {
		return pageRequest.previous();
	}

	@Override
	public Pageable first() {
		return pageRequest.first();
	}

	@Override
	public boolean equals(Object obj) {
		return pageRequest.equals(obj);
	}

	@Override
	public int hashCode() {
		return pageRequest.hashCode();
	}

	@Override
	public String toString() {
		return pageRequest.toString();
	}

	@Override
	public int getPageSize() {
		return pageRequest.getPageSize();
	}

	@Override
	public int getPageNumber() {
		return pageRequest.getPageNumber();
	}

	@Override
	public long getOffset() {
		return pageRequest.getOffset();
	}

	@Override
	public boolean hasPrevious() {
		return pageRequest.hasPrevious();
	}

	@Override
	public Pageable previousOrFirst() {
		return pageRequest.previousOrFirst();
	}

	public static Pageable unpaged() {
		return Pageable.unpaged();
	}

	@Override
	public boolean isPaged() {
		return pageRequest.isPaged();
	}

	@Override
	public boolean isUnpaged() {
		return pageRequest.isUnpaged();
	}

	@Override
	public Sort getSortOr(Sort sort) {
		return pageRequest.getSortOr(sort);
	}

	@Override
	public Optional<Pageable> toOptional() {
		return pageRequest.toOptional();
	}
}

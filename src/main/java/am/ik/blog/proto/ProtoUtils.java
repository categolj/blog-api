package am.ik.blog.proto;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import am.ik.pagination.CursorPage;
import am.ik.pagination.OffsetPage;
import com.google.protobuf.Timestamp;

final public class ProtoUtils {

	public static Entry toProto(am.ik.blog.entry.Entry entry) {
		return Entry.newBuilder()
			.setEntryId(entry.getEntryId())
			.setFrontMatter(toProto(entry.getFrontMatter()))
			.setContent(entry.getContent())
			.setCreated(toProto(entry.getCreated()))
			.setUpdated(toProto(entry.getUpdated()))
			.build();
	}

	public static FrontMatter toProto(am.ik.blog.entry.FrontMatter frontMatter) {
		return FrontMatter.newBuilder()
			.setTitle(frontMatter.title())
			.addAllCategories(frontMatter.categories().stream().map(ProtoUtils::toProto).toList())
			.addAllTags(frontMatter.tags().stream().map(ProtoUtils::toProto).toList())
			.build();
	}

	public static Tag toProto(am.ik.blog.tag.Tag tag) {
		Tag.Builder builder = Tag.newBuilder().setName(tag.name());
		if (tag.version() != null) {
			builder.setVersion(tag.version());
		}
		return builder.build();
	}

	public static TagAndCount toProto(am.ik.blog.tag.TagAndCount tagAndCount) {
		am.ik.blog.tag.Tag tag = tagAndCount.tag();
		TagAndCount.Builder builder = TagAndCount.newBuilder().setName(tag.name());
		if (tag.version() != null) {
			builder.setVersion(tag.version());
		}
		return builder.build();
	}

	public static TagsResponse toProtoTagsResponse(List<am.ik.blog.tag.TagAndCount> tagAndCounts) {
		return TagsResponse.newBuilder().addAllTags(tagAndCounts.stream().map(ProtoUtils::toProto).toList()).build();
	}

	public static Category toProto(am.ik.blog.category.Category category) {
		return Category.newBuilder().setName(category.name()).build();
	}

	static CategoryList toProtoCategoryList(List<am.ik.blog.category.Category> categoryList) {
		return CategoryList.newBuilder()
			.addAllCategories(categoryList.stream().map(ProtoUtils::toProto).toList())
			.build();
	}

	public static CategoriesResponse toProtoCategoriesResponse(List<List<am.ik.blog.category.Category>> categories) {
		return CategoriesResponse.newBuilder()
			.addAllCategoryGroups(categories.stream().map(ProtoUtils::toProtoCategoryList).toList())
			.build();
	}

	public static Author toProto(am.ik.blog.entry.Author author) {
		Author.Builder builder = Author.newBuilder();
		OffsetDateTime date = author.date();
		if (author.name() != null) {
			builder.setName(author.name());
		}
		if (date != null) {
			builder.setDate(Timestamp.newBuilder().setSeconds(date.toEpochSecond()).setNanos(date.getNano()).build());
		}
		return builder.build();
	}

	public static CursorPageEntryInstant toProto(CursorPage<am.ik.blog.entry.Entry, Instant> page) {
		return CursorPageEntryInstant.newBuilder()
			.addAllContent(page.content().stream().map(ProtoUtils::toProto).toList())
			.setSize(page.size())
			.setHasNext(page.hasNext())
			.setHasPrevious(page.hasPrevious())
			.build();
	}

	public static OffsetPageEntry toProto(OffsetPage<am.ik.blog.entry.Entry> page) {
		return OffsetPageEntry.newBuilder()
			.addAllContent(page.content().stream().map(ProtoUtils::toProto).toList())
			.setSize(page.size())
			.setNumber(page.number())
			.setTotalElements(page.totalElements())
			.build();
	}

}

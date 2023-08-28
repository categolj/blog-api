package am.ik.blog.tag;

public record TagNameAndCount(String name, int count) {
	public Tag tag() {
		return new Tag(name);
	}
}

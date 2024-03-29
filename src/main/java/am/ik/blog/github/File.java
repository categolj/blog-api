package am.ik.blog.github;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public record File(String name, String path, String sha, String url, String gitUrl, String htmlUrl, String downloadUrl,
		String content, String type) {
	public String decode() {
		return new String(Base64.getMimeDecoder().decode(this.content), StandardCharsets.UTF_8);
	}
}

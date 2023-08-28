package am.ik.blog.github;

public record Committer(String login, String id, String avatarUrl, String gravaterId, String url, String htmlUrl,
		String followersUrl, String followingUrl, String gistsUrl, String starredUrl, String subscriptionsUrl,
		String organizationsUrl, String reposUrl, String eventsUrl, String receivedEventsUrl, String type,
		boolean siteAdmin) {
}

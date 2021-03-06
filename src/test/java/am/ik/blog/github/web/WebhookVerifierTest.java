package am.ik.blog.github.web;

import am.ik.blog.github.web.WebhookVerifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class WebhookVerifierTest {

    @Test
    void verify() throws Exception {
        WebhookVerifier verifier = new WebhookVerifier("foofoo");
        String payload = "{\"ref\":\"refs/heads/master\",\"before\":\"7cec87b9fc13d16bec71347707df8e982057da06\",\"after\":\"f5a987f39f94b83499857dc04f48802b0ccc1ee1\",\"created\":false," +
            "\"deleted\":false,\"forced\":false,\"base_ref\":null,\"compare\":\"https://github.com/making/blog.ik.am/compare/7cec87b9fc13...f5a987f39f94\"," +
            "\"commits\":[{\"id\":\"f5a987f39f94b83499857dc04f48802b0ccc1ee1\",\"tree_id\":\"d6fa51123571290f7f4f3ccd023f38c15236f79d\",\"distinct\":true,\"message\":\"Create 00497.md\"," +
            "\"timestamp\":\"2017-12-24T18:59:40+09:00\",\"url\":\"https://github.com/making/blog.ik.am/commit/f5a987f39f94b83499857dc04f48802b0ccc1ee1\",\"author\":{\"name\":\"Toshiaki Maki\"," +
            "\"email\":\"tmaki@pivotal.io\",\"username\":\"making\"},\"committer\":{\"name\":\"GitHub\",\"email\":\"noreply@github.com\",\"username\":\"web-flow\"},\"added\":[\"content/00497.md\"]," +
            "\"removed\":[],\"modified\":[]}],\"head_commit\":{\"id\":\"f5a987f39f94b83499857dc04f48802b0ccc1ee1\",\"tree_id\":\"d6fa51123571290f7f4f3ccd023f38c15236f79d\",\"distinct\":true," +
            "\"message\":\"Create 00497.md\",\"timestamp\":\"2017-12-24T18:59:40+09:00\",\"url\":\"https://github.com/making/blog.ik.am/commit/f5a987f39f94b83499857dc04f48802b0ccc1ee1\"," +
            "\"author\":{\"name\":\"Toshiaki Maki\",\"email\":\"tmaki@pivotal.io\",\"username\":\"making\"},\"committer\":{\"name\":\"GitHub\",\"email\":\"noreply@github.com\"," +
            "\"username\":\"web-flow\"},\"added\":[\"content/00497.md\"],\"removed\":[],\"modified\":[]},\"repository\":{\"id\":48331386,\"name\":\"blog.ik.am\",\"full_name\":\"making/blog.ik.am\"," +
            "\"owner\":{\"name\":\"making\",\"email\":\"tmaki@pivotal.io\",\"login\":\"making\",\"id\":106908,\"avatar_url\":\"https://avatars0.githubusercontent.com/u/106908?v=4\"," +
            "\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/making\",\"html_url\":\"https://github.com/making\",\"followers_url\":\"https://api.github.com/users/making/followers\"," +
            "\"following_url\":\"https://api.github.com/users/making/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/making/gists{/gist_id}\",\"starred_url\":\"https://api" +
            ".github.com/users/making/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/making/subscriptions\",\"organizations_url\":\"https://api.github" +
            ".com/users/making/orgs\",\"repos_url\":\"https://api.github.com/users/making/repos\",\"events_url\":\"https://api.github.com/users/making/events{/privacy}\"," +
            "\"received_events_url\":\"https://api.github.com/users/making/received_events\",\"type\":\"User\",\"site_admin\":false},\"private\":false,\"html_url\":\"https://github.com/making/blog" +
            ".ik.am\",\"description\":null,\"fork\":false,\"url\":\"https://github.com/making/blog.ik.am\",\"forks_url\":\"https://api.github.com/repos/making/blog.ik.am/forks\"," +
            "\"keys_url\":\"https://api.github.com/repos/making/blog.ik.am/keys{/key_id}\",\"collaborators_url\":\"https://api.github.com/repos/making/blog.ik.am/collaborators{/collaborator}\"," +
            "\"teams_url\":\"https://api.github.com/repos/making/blog.ik.am/teams\",\"hooks_url\":\"https://api.github.com/repos/making/blog.ik.am/hooks\",\"issue_events_url\":\"https://api.github" +
            ".com/repos/making/blog.ik.am/issues/events{/number}\",\"events_url\":\"https://api.github.com/repos/making/blog.ik.am/events\",\"assignees_url\":\"https://api.github" +
            ".com/repos/making/blog.ik.am/assignees{/user}\",\"branches_url\":\"https://api.github.com/repos/making/blog.ik.am/branches{/branch}\",\"tags_url\":\"https://api.github" +
            ".com/repos/making/blog.ik.am/tags\",\"blobs_url\":\"https://api.github.com/repos/making/blog.ik.am/git/blobs{/sha}\",\"git_tags_url\":\"https://api.github.com/repos/making/blog.ik" +
            ".am/git/tags{/sha}\",\"git_refs_url\":\"https://api.github.com/repos/making/blog.ik.am/git/refs{/sha}\",\"trees_url\":\"https://api.github.com/repos/making/blog.ik" +
            ".am/git/trees{/sha}\",\"statuses_url\":\"https://api.github.com/repos/making/blog.ik.am/statuses/{sha}\",\"languages_url\":\"https://api.github.com/repos/making/blog.ik.am/languages\"," +
            "\"stargazers_url\":\"https://api.github.com/repos/making/blog.ik.am/stargazers\",\"contributors_url\":\"https://api.github.com/repos/making/blog.ik.am/contributors\"," +
            "\"subscribers_url\":\"https://api.github.com/repos/making/blog.ik.am/subscribers\",\"subscription_url\":\"https://api.github.com/repos/making/blog.ik.am/subscription\"," +
            "\"commits_url\":\"https://api.github.com/repos/making/blog.ik.am/commits{/sha}\",\"git_commits_url\":\"https://api.github.com/repos/making/blog.ik.am/git/commits{/sha}\"," +
            "\"comments_url\":\"https://api.github.com/repos/making/blog.ik.am/comments{/number}\",\"issue_comment_url\":\"https://api.github.com/repos/making/blog.ik.am/issues/comments{/number}\"," +
            "\"contents_url\":\"https://api.github.com/repos/making/blog.ik.am/contents/{+path}\",\"compare_url\":\"https://api.github.com/repos/making/blog.ik.am/compare/{base}...{head}\"," +
            "\"merges_url\":\"https://api.github.com/repos/making/blog.ik.am/merges\",\"archive_url\":\"https://api.github.com/repos/making/blog.ik.am/{archive_format}{/ref}\"," +
            "\"downloads_url\":\"https://api.github.com/repos/making/blog.ik.am/downloads\",\"issues_url\":\"https://api.github.com/repos/making/blog.ik.am/issues{/number}\"," +
            "\"pulls_url\":\"https://api.github.com/repos/making/blog.ik.am/pulls{/number}\",\"milestones_url\":\"https://api.github.com/repos/making/blog.ik.am/milestones{/number}\"," +
            "\"notifications_url\":\"https://api.github.com/repos/making/blog.ik.am/notifications{?since,all,participating}\",\"labels_url\":\"https://api.github.com/repos/making/blog.ik" +
            ".am/labels{/name}\",\"releases_url\":\"https://api.github.com/repos/making/blog.ik.am/releases{/id}\",\"deployments_url\":\"https://api.github.com/repos/making/blog.ik" +
            ".am/deployments\",\"created_at\":1450634109,\"updated_at\":\"2016-10-23T10:37:14Z\",\"pushed_at\":1514109581,\"git_url\":\"git://github.com/making/blog.ik.am.git\"," +
            "\"ssh_url\":\"git@github.com:making/blog.ik.am.git\",\"clone_url\":\"https://github.com/making/blog.ik.am.git\",\"svn_url\":\"https://github.com/making/blog.ik.am\"," +
            "\"homepage\":\"https://blog.ik.am\",\"size\":1492,\"stargazers_count\":0,\"watchers_count\":0,\"language\":null,\"has_issues\":true,\"has_projects\":true,\"has_downloads\":true," +
            "\"has_wiki\":true,\"has_pages\":false,\"forks_count\":4,\"mirror_url\":null,\"archived\":false,\"open_issues_count\":0,\"license\":null,\"forks\":4,\"open_issues\":0,\"watchers\":0," +
            "\"default_branch\":\"master\",\"stargazers\":0,\"master_branch\":\"master\"},\"pusher\":{\"name\":\"making\",\"email\":\"tmaki@pivotal.io\"},\"sender\":{\"login\":\"making\"," +
            "\"id\":106908,\"avatar_url\":\"https://avatars0.githubusercontent.com/u/106908?v=4\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/making\",\"html_url\":\"https://github" +
            ".com/making\",\"followers_url\":\"https://api.github.com/users/making/followers\",\"following_url\":\"https://api.github.com/users/making/following{/other_user}\"," +
            "\"gists_url\":\"https://api.github.com/users/making/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/making/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api" +
            ".github.com/users/making/subscriptions\",\"organizations_url\":\"https://api.github.com/users/making/orgs\",\"repos_url\":\"https://api.github.com/users/making/repos\"," +
            "\"events_url\":\"https://api.github.com/users/making/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/making/received_events\",\"type\":\"User\"," +
            "\"site_admin\":false}}";
        String signature = verifier.signature(payload);
        Assertions.assertThat(signature)
            .isEqualTo("sha1=6ff50ec0e2f69d5831d8a5a88570be819b18515a");
    }
}
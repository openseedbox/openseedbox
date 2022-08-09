package com.openseedbox.code.libs;

public class GitHubOAuth2 extends EnhancedOAuth2 {

	public static final String GITHUB_AUTHORIZATION_URL = "https://github.com/login/oauth/authorize";
	public static final String GITHUB_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
	public static final String GITHUB_USER_INFO_URL = "https://api.github.com/user";
	public static final String GITHUB_USER_EMAILS_URL = "https://api.github.com/user/emails";
	public static final String GITHUB_SCOPE = "read:user user:email";

	public GitHubOAuth2(String clientid, String secret) {
		super(GITHUB_AUTHORIZATION_URL, GITHUB_ACCESS_TOKEN_URL, clientid, secret);
		scope = GITHUB_SCOPE;
	}
}

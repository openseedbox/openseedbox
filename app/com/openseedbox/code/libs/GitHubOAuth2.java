package com.openseedbox.code.libs;

public class GitHubOAuth2 extends EnhancedOAuth2 {

	public static final String GITHUB_AUTHORIZATION_URL = "https://github.com/login/oauth/authorize";
	public static final String GITHUB_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";

	public GitHubOAuth2(String clientid, String secret) {
		super(GITHUB_AUTHORIZATION_URL, GITHUB_ACCESS_TOKEN_URL, clientid, secret);
	}
}

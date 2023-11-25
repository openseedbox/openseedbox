package controllers;

import com.google.gson.Gson;
import com.openseedbox.Config;
import com.openseedbox.code.libs.GitHubOAuth2;
import com.openseedbox.code.libs.OAuth2;
import com.openseedbox.models.ScopedUser;
import play.Logger;
import play.libs.WS;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuthGitHub extends BaseOAuth<GitHubOAuth2> {
	@Override
	protected AuthProvider initProvider() {
		AuthProvider provider = new AuthProvider();

		provider.providerService = new GitHubOAuth2(Config.getGitHubClientId(), Config.getGitHubClientSecret());
		provider.providerName = "GitHub";
		provider.providerId = provider.providerName.toLowerCase();

		return provider;
	}

	@Override
	protected ScopedUser retrieveScopedUser(OAuth2.Response accessTokenResponse) {
		WS.HttpResponse response = WS.url(provider.providerService.GITHUB_USER_INFO_URL)
				.setHeader("Accept", "application/vnd.github+json")
				.setHeader("Authorization", "token " + accessTokenResponse.accessToken)
				.get();
		Logger.debug("retrieveScopedUser(%s): %s", this.getClass().getSimpleName(), response.getString());

		Gson gson = new Gson();
		ScopedUser user = gson.fromJson(response.getJson(), ScopedUser.class);

		if (user.email == null) {
			// email is not public, had to use the dedicated email endpoint
			response = WS.url(provider.providerService.GITHUB_USER_EMAILS_URL)
					.setHeader("Accept", "application/vnd.github+json")
					.setHeader("Authorization", "token " + accessTokenResponse.accessToken)
					.get();
			user.email = response.getJson().getAsJsonArray().get(0).getAsJsonObject()
					.getAsJsonPrimitive("email").getAsString();
		}

		return user;
	}

	/**
	 * This would never happen, but at least it looks good!
	 *
	 * @return a html source for a flash message how to deal with empty email address
	 */
	@Deprecated
	@Override
	protected String authorizedWithoutEmailMessage() {
		setMessageIsRaw();
		return renderToString("tags/auth/authorized-without-email.html", Stream.of(
				new AbstractMap.SimpleEntry<>("providerName", provider.providerName),
				new AbstractMap.SimpleEntry<>("providerSettingsUrl", "https://github.com/settings/profile"),
				new AbstractMap.SimpleEntry<>("providerEmailSettingsImageUrl", "/images/github-settings-public-email.png"))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
		);
	}
}

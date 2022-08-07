package controllers;

import com.google.gson.Gson;
import com.openseedbox.Config;
import com.openseedbox.code.libs.KeyCloakOIDC;
import com.openseedbox.code.libs.OAuth2;
import com.openseedbox.models.ScopedUser;
import play.Logger;
import play.libs.F;
import play.libs.WS;

import java.util.Map;

public class AuthKeyCloak extends BaseOAuth<KeyCloakOIDC> {
	@Override
	protected AuthProvider initProvider() {
		AuthProvider provider = new AuthProvider();

		KeyCloakOIDC service = KeyCloakOIDC.builder()
				.withClientId(Config.getKeyCloakClientId())
				.withClientSecret(Config.getKeyCloakClientSecret())
				.withOpenIDConfigurationURL(Config.getKeyCloakOpenIdConfigurationUrl())
				.build();

		provider.providerService = service;
		provider.providerName = "KeyCloak";
		provider.providerId = provider.providerName.toLowerCase();

		return provider;
	}

	@Override
	protected F.Tuple<String, String> customizeAuthURL(String controllerName, String actionName, Map<String, Object> actionParameters) {
		return customizeAuthURLWithFragmentRedirect(controllerName, actionName, actionParameters);
	}

	@Override
	protected ScopedUser retrieveScopedUser(OAuth2.Response accessTokenResponse) {
		WS.HttpResponse response = WS.url(provider.providerService.userinfoURL)
				.setHeader("Accept", "application/json")
				.setHeader("Authorization", "bearer " + accessTokenResponse.accessToken)
				.get();

		return new Gson().fromJson(response.getJson(), ScopedUser.class);
	}
}

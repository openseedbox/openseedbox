package controllers;

import com.openseedbox.Config;
import com.openseedbox.code.libs.KeyCloakOIDC;
import com.openseedbox.code.libs.oidc.ResponseMode;

public class AuthKeyCloak extends BaseOIDCAuth<KeyCloakOIDC> {
	@Override
	protected AuthProvider initProvider() {
		AuthProvider provider = new AuthProvider();

		KeyCloakOIDC service = new KeyCloakOIDC.Builder()
				.withClientId(Config.getKeyCloakClientId())
				.withClientSecret(Config.getKeyCloakClientSecret())
				.withOpenIDConfigurationURL(Config.getKeyCloakOpenIdConfigurationUrl())
				.withResponseMode(ResponseMode.valueOf(Config.getKeyCloakResponseMode()))
				.build();

		provider.providerService = service;
		provider.providerName = "KeyCloak";
		provider.providerId = provider.providerName.toLowerCase();

		return provider;
	}

	@Override
	protected String customizeAuthURLAction() {
		if (provider.providerService.responseMode.toString().startsWith(ResponseMode.FRAGMENT.toString())) {
			return authUrlActionRedirect;
		} else {
			return super.customizeAuthURLAction();
		}
	}
}

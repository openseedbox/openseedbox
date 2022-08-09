package controllers;

import com.openseedbox.Config;
import com.openseedbox.code.libs.GoogleOIDC;

public class AuthGoogle extends BaseOIDCAuth<GoogleOIDC> {
	@Override
	protected AuthProvider initProvider() {
		AuthProvider provider = new AuthProvider();

		GoogleOIDC service = new GoogleOIDC.Builder()
				.withClientId(Config.getGoogleClientId())
				.withClientSecret(Config.getGoogleClientSecret())
				.withOpenIDConfigurationURL(Config.getGoogleOpenIdConfigurationUrl())
				.build();

		provider.providerService = service;
		provider.providerName = "Google";
		provider.providerId = provider.providerName.toLowerCase();

		return provider;
	}
}

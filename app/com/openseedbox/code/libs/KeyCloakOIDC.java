package com.openseedbox.code.libs;

import com.openseedbox.code.libs.oidc.ResponseMode;

import java.util.Map;

public class KeyCloakOIDC extends OpenIDConnect {
	public static final String DEFAULT_GRANT_TYPE = "authorization_code";

	protected static final String RESPONSE_MODE_NAME = "response_mode";
	protected static final String GRANT_TYPE_NAME = "grant_type";

	public ResponseMode responseMode;
	public String grantType;

	public KeyCloakOIDC(String authorizationURL, String accessTokenURL, String clientid, String secret) {
		super(authorizationURL, accessTokenURL, clientid, secret);
	}

	public KeyCloakOIDC() {
		super();
		this.responseMode = ResponseMode.FRAGMENT;
		this.grantType = DEFAULT_GRANT_TYPE;
	}

	@Override
	public void retrieveVerificationCode(String callbackURL, Map<String, String> parameters) {
		parameters.putIfAbsent(RESPONSE_MODE_NAME, responseMode.toString());
		super.retrieveVerificationCode(callbackURL, parameters);
	}

	@Override
	protected Response retrieveAccessToken(String callbackURL, Map<String, Object> params) {
		params.putIfAbsent(GRANT_TYPE_NAME, grantType);
		return super.retrieveAccessToken(callbackURL, params);
	}

	public static WellKnownBuilder builder() {
		return new WellKnownBuilder();
	}

	public static class WellKnownBuilder extends OpenIDConnect.WellKnownBuilder<KeyCloakOIDC> {
		@Override
		protected KeyCloakOIDC createOIDC() {
			return new KeyCloakOIDC();
		}

		public WellKnownBuilder withResponseMode(ResponseMode responseMode) {
			this.building.responseMode = responseMode;
			return this;
		}

		public WellKnownBuilder withGrantType(String grantType) {
			this.building.grantType = grantType;
			return this;
		}
	}
}

package com.openseedbox.code.libs;

import com.openseedbox.code.libs.oidc.ResponseMode;
import play.mvc.Scope;

import java.util.Map;

public class KeyCloakOIDC extends OpenIDConnect {
	public static final String DEFAULT_GRANT_TYPE = "authorization_code";

	protected static final String RESPONSE_MODE_NAME = "response_mode";
	protected static final String GRANT_TYPE_NAME = "grant_type";
	protected static final String RESPONSE_NAME = "response";

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
	public boolean isResponseToRetrieveVerificationCode() {
		if (Scope.Params.current()._contains(RESPONSE_NAME)) {
			throw new UnsupportedOperationException("JWT response processing is not implemented yet!");
		} else {
			return super.isResponseToRetrieveVerificationCode();
		}
	}

	@Override
	public void retrieveVerificationCode(String callbackURL, Map<String, String> parameters) {
		parameters.putIfAbsent(RESPONSE_MODE_NAME, responseMode.toString());
		super.retrieveVerificationCode(callbackURL, parameters);
	}

	@Override
	protected ResponseWithIdToken retrieveAccessToken(String callbackURL, Map<String, Object> params) {
		params.putIfAbsent(GRANT_TYPE_NAME, grantType);
		return super.retrieveAccessToken(callbackURL, params);
	}

	public static class Builder extends OpenIDConnect.Builder<KeyCloakOIDC, Builder> {
		@Override
		protected KeyCloakOIDC createOIDC() {
			return new KeyCloakOIDC();
		}

		@Override
		protected Builder createBuilder() {
			return this;
		}

		public Builder withResponseMode(ResponseMode responseMode) {
			this.building.responseMode = responseMode;
			return this;
		}

		public Builder withGrantType(String grantType) {
			this.building.grantType = grantType;
			return this;
		}
	}
}

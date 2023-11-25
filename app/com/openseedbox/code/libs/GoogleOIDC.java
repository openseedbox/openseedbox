package com.openseedbox.code.libs;

import java.util.Map;

public class GoogleOIDC extends OpenIDConnect {
	public static final String DEFAULT_GRANT_TYPE = "authorization_code";

	protected static final String GRANT_TYPE_NAME = "grant_type";

	public String grantType;

	public GoogleOIDC() {
		super();
		this.grantType = DEFAULT_GRANT_TYPE;
	}

	@Override
	protected ResponseWithIdToken retrieveAccessToken(String callbackURL, Map<String, Object> params) {
		params.putIfAbsent(GRANT_TYPE_NAME, grantType);
		return super.retrieveAccessToken(callbackURL, params);
	}


	public static class Builder extends OpenIDConnect.Builder<GoogleOIDC, Builder> {
		@Override
		protected GoogleOIDC createOIDC() {
			return new GoogleOIDC();
		}

		@Override
		protected Builder createBuilder() {
			return this;
		}
	}
}

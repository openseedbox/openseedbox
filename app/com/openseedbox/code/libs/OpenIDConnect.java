package com.openseedbox.code.libs;

import com.google.gson.JsonObject;
import play.libs.WS;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class OpenIDConnect extends EnhancedOAuth2 {
	public static final String DEFAULT_SCOPE = "openid email profile";
	public static final String DEFAULT_RESPONSE_TYPE = "code";

	protected static final String NONCE_NAME = "nonce";
	protected static final String RESPONSE_TYPE_NAME = "response_type";

	public String issuer;
	public String userinfoURL;
	public String jwksURL;
	public String nonce;
	public String responseType;

	protected List<String> availableScopes;

	public OpenIDConnect(String authorizationURL, String accessTokenURL, String clientid, String secret) {
		super(authorizationURL, accessTokenURL, clientid, secret);
	}

	public OpenIDConnect() {
		super(null, null, null, null);
		this.scope = DEFAULT_SCOPE;
		this.responseType = DEFAULT_RESPONSE_TYPE;
	}

	@Override
	public void retrieveVerificationCode(String callbackURL, Map<String, String> parameters) {
		nonce = addUnguessableParamValueAndSaveForLater(NONCE_NAME, parameters);
		parameters.putIfAbsent(RESPONSE_TYPE_NAME, responseType);
		super.retrieveVerificationCode(callbackURL, parameters);
	}

	@Override
	protected Response retrieveAccessToken(String callbackURL, Map<String, Object> params) {
		if (params.getOrDefault((String) CLIENT_SECRET_NAME, (String) "not exists") == null) {
			params.remove((String) CLIENT_SECRET_NAME);
		}
		WS.HttpResponse response = WS.url(accessTokenURL).params(params).post();
		return new Response(response);
	}

	public static WellKnownBuilder builder() {
		return new WellKnownBuilder<>();
	}

	public static class WellKnownBuilder<T extends OpenIDConnect> {
		private String openIDConfigurationUrl;
		protected JsonObject openIDConfiguration;

		protected T building = createOIDC();

		protected T createOIDC() {
			return (T) new OpenIDConnect();
		}

		public WellKnownBuilder<T> withOpenIDConfigurationURL(String url) {
			this.openIDConfigurationUrl = url;
			return this;
		}

		public WellKnownBuilder<T> withClientId (String clientid) {
			this.building.clientid = clientid;
			return this;
		}

		public WellKnownBuilder<T> withClientSecret (String secret) {
			this.building.secret = secret;
			return this;
		}

		public WellKnownBuilder<T> withScope(String scope) {
			this.building.scope = scope;
			return this;
		}

		public WellKnownBuilder<T> withResponseType(String responseType) {
			this.building.responseType = responseType;
			return this;
		}

		public T build() {
			WS.HttpResponse configurationResponse = null;
			try {
				configurationResponse = WS.url(openIDConfigurationUrl).getAsync().get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			openIDConfiguration = Objects.requireNonNull(configurationResponse).getJson().getAsJsonObject();
			parseOpenIDConfiguration();
			return this.building;
		}

		protected void parseOpenIDConfiguration() {
			String issuer = openIDConfiguration.get("issuer").getAsString();
			String authorization_endpoint = openIDConfiguration.get("authorization_endpoint").getAsString();
			String token_endpoint = openIDConfiguration.get("token_endpoint").getAsString();
			String userinfo_endpoint = openIDConfiguration.get("userinfo_endpoint").getAsString();
			String jwks_uri = openIDConfiguration.get("jwks_uri").getAsString();
			List<String> scopes_supported = Arrays.asList(openIDConfiguration.getAsJsonArray("scopes_supported")
					.spliterator().toString());

			this.building.issuer = issuer;
			this.building.authorizationURL = authorization_endpoint;
			this.building.accessTokenURL = token_endpoint;
			this.building.userinfoURL = userinfo_endpoint;
			this.building.jwksURL = jwks_uri;
			this.building.availableScopes = scopes_supported;
		}
	}
}

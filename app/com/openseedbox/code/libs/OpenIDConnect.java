package com.openseedbox.code.libs;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.exceptions.JavaExecutionException;
import play.libs.WS;
import play.mvc.Scope;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
	protected ResponseWithIdToken retrieveAccessToken(String callbackURL, Map<String, Object> params) {
		if (params.getOrDefault((String) CLIENT_SECRET_NAME, (String) "not exists") == null) {
			params.remove((String) CLIENT_SECRET_NAME);
			Logger.debug("retrieveAccessToken(%s): removed %s", this.getClass().getSimpleName(), CLIENT_SECRET_NAME);
		}
		WS.HttpResponse response = WS.url(accessTokenURL).params(params).post();
		Logger.debug("retrieveAccessToken(%s): params - %s", this.getClass().getSimpleName(), params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(", ")));
		Logger.debug("retrieveAccessToken(%s): response - %s", this.getClass().getSimpleName(), response.getString());
		return new ResponseWithIdToken(response, this);
	}

	public boolean shouldAbortTheProcessBecauseOfNonce(ResponseWithIdToken response) {
		if (response.idToken == null) {
			return false;
		}
		String nonceFromIdToken;
		try {
			nonceFromIdToken = new JsonParser().parse(response.idToken).getAsJsonObject().get(NONCE_NAME).getAsString();
		} catch (JsonSyntaxException | NullPointerException e) {
			return true;
		}
		if (nonceFromIdToken == null) {
			return true;
		}
		String nonceFromFlash = Scope.Flash.current().get(NONCE_NAME);
		return !nonceFromIdToken.equals(nonceFromFlash);
	}


	public static class ResponseWithIdToken extends Response {
		public final String idToken;

		static final String ID_TOKEN_NAME = "id_token";

		public ResponseWithIdToken(WS.HttpResponse response, OpenIDConnect authProvider) {
			super(new Response(response).accessToken,
					getErrorOrValidateToken(response, authProvider), response);
			if (this.error != null) {
				this.idToken = null;
			} else {
				String token;
				try {
					JsonElement tokenElement = response.getJson()
							.getAsJsonObject()
							.get(ID_TOKEN_NAME);
					if (tokenElement != null) {
						token = new String(Base64.getDecoder().decode(
								JWT.decode(tokenElement.getAsString()).getPayload()
						));
					} else {
						token = null;
					}
				} catch (NullPointerException e) {
					token = null;
				}
				this.idToken = token;
			}
		}

		private static Error getErrorOrValidateToken(WS.HttpResponse response, OpenIDConnect authProvider) {
			String token = response.getJson().getAsJsonObject().get(ID_TOKEN_NAME).getAsString();
			DecodedJWT jwt = JWT.decode(token);
			try {
				JwkProvider jwkProvider = new JwkProviderBuilder(new URI(authProvider.jwksURL).normalize().toURL())
						.cached(10, 24, TimeUnit.HOURS)
						.rateLimited(10, 1, TimeUnit.MINUTES)
						.build();
				Jwk jwk = jwkProvider.get(jwt.getKeyId());
				Algorithm algorithm = null;
				if ("RS256".equals(jwt.getAlgorithm())) {
					algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
				}
				JWTVerifier verifier = JWT.require(algorithm)
						.withAudience(authProvider.clientid)
						.withIssuer(jwt.getIssuer())
						.build();
				verifier.verify(token);
			} catch (JwkException | URISyntaxException | MalformedURLException e) {
				Error.Type errorType = e instanceof JwkException ? Error.Type.OAUTH : Error.Type.COMMUNICATION;
				return new Error(errorType, e.getClass().getSimpleName(), e.getMessage());
			}
			return null;
		}
	}


	public abstract static class Builder<T extends OpenIDConnect, B extends Builder<T,B>> {
		private String openIDConfigurationUrl;
		protected JsonObject openIDConfiguration;

		protected T building = createOIDC();
		protected B builder = createBuilder();

		protected abstract T createOIDC();
		protected abstract B createBuilder();

		public B withOpenIDConfigurationURL(String url) {
			this.openIDConfigurationUrl = url;
			return builder;
		}

		public B withClientId (String clientid) {
			this.building.clientid = clientid;
			return builder;
		}

		public B withClientSecret (String secret) {
			this.building.secret = secret;
			return builder;
		}

		public B withScope(String scope) {
			this.building.scope = scope;
			return builder;
		}

		public B withResponseType(String responseType) {
			this.building.responseType = responseType;
			return builder;
		}

		public T build() {
			WS.HttpResponse configurationResponse = null;
			try {
				String cacheKey = "openid-configuration-" + openIDConfigurationUrl;
				String cachedResponseString = Cache.get(cacheKey, String.class);
				if (StringUtils.isEmpty(cachedResponseString)) {
					configurationResponse = WS.url(openIDConfigurationUrl).getAsync().get();
					openIDConfiguration = Objects.requireNonNull(configurationResponse, "No response from " + openIDConfigurationUrl).getJson().getAsJsonObject();
					Cache.set(cacheKey, configurationResponse.getString(), "1d");
				} else {
					openIDConfiguration = new JsonParser().parse(cachedResponseString).getAsJsonObject();
				}
			} catch (InterruptedException | ExecutionException e) {
				throw new JavaExecutionException(e);
			}
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

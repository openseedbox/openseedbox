package com.openseedbox.code.libs;

import org.apache.commons.lang.RandomStringUtils;
import play.mvc.Scope;

import java.util.Map;

public class EnhancedOAuth2 extends OAuth2 {
	public static final String STATE_NAME = "state";
	protected static final String SCOPE_NAME = "scope";

	public String scope;

	public EnhancedOAuth2(String authorizationURL, String accessTokenURL, String clientid, String secret) {
		super(authorizationURL, accessTokenURL, clientid, secret);
	}

	@Override
	public void retrieveVerificationCode(String callbackURL, Map<String, String> parameters) {
		addUnguessableParamValueAndSaveForLater(STATE_NAME, parameters);
		parameters.putIfAbsent(SCOPE_NAME, scope);
		super.retrieveVerificationCode(callbackURL, parameters);
	}

	public boolean shouldAbortTheProcess() {
		String stateFromParams = Scope.Params.current().get(STATE_NAME);
		String stateFromFlash = Scope.Flash.current().get(STATE_NAME);
		return !stateFromParams.equals(stateFromFlash);
	}

	protected final String addUnguessableParamValueAndSaveForLater (String paramName, Map<String, String> parameters) {
		String unguessableParamValue = parameters.getOrDefault(paramName, RandomStringUtils.random(128, true, true));
		parameters.putIfAbsent(paramName, unguessableParamValue);
		if (!Scope.Flash.current().contains(paramName) && !Scope.Session.current().contains(paramName)) {
			Scope.Flash.current().put(paramName, unguessableParamValue);
		}
		return unguessableParamValue;
	}
}

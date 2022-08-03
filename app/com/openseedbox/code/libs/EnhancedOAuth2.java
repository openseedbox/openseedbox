package com.openseedbox.code.libs;

import org.apache.commons.lang.RandomStringUtils;
import play.mvc.Scope;

import java.util.Map;

public class EnhancedOAuth2 extends OAuth2 {
	public static final String STATE_NAME = "state";

	public EnhancedOAuth2(String authorizationURL, String accessTokenURL, String clientid, String secret) {
		super(authorizationURL, accessTokenURL, clientid, secret);
	}

	@Override
	public void retrieveVerificationCode(String callbackURL, Map<String, String> parameters) {
		String unguessableParamValue = parameters.getOrDefault(STATE_NAME, RandomStringUtils.random(128, true, true));
		parameters.putIfAbsent(STATE_NAME, unguessableParamValue);
		if (!Scope.Flash.current().contains(STATE_NAME) && !Scope.Session.current().contains(STATE_NAME)) {
			Scope.Flash.current().put(STATE_NAME, unguessableParamValue);
		}
		super.retrieveVerificationCode(callbackURL, parameters);
	}

	public boolean shouldAbortTheProcess() {
		String stateFromParams = Scope.Params.current().get(STATE_NAME);
		String stateFromFlash = Scope.Flash.current().get(STATE_NAME);
		return !stateFromParams.equals(stateFromFlash);
	}

}

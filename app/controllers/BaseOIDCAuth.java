package controllers;

import com.google.gson.Gson;
import com.openseedbox.code.libs.OAuth2;
import com.openseedbox.code.libs.OpenIDConnect;
import com.openseedbox.models.JwtBasedScopedUser;
import com.openseedbox.models.ScopedUser;

public abstract class BaseOIDCAuth<T extends OpenIDConnect> extends BaseOAuth<T> {

	@Override
	protected final ScopedUser retrieveScopedUser(T.Response accessTokenResponse) {
		return retrieveScopedUser((T.ResponseWithIdToken) accessTokenResponse);
	}

	protected ScopedUser retrieveScopedUser(T.ResponseWithIdToken accessTokenResponse) {
		return new Gson().fromJson(accessTokenResponse.idToken, JwtBasedScopedUser.class);
	};
}

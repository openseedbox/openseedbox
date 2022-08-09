package controllers;

import com.openseedbox.code.libs.EnhancedOAuth2;
import com.openseedbox.code.libs.OAuth2;
import com.openseedbox.models.ScopedUser;
import com.openseedbox.models.User;
import org.apache.commons.codec.digest.DigestUtils;
import play.Logger;
import play.cache.Cache;
import play.libs.F;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public abstract class BaseOAuth<T extends EnhancedOAuth2> extends Base {

	protected AuthProvider provider = initProvider();

	public final void authenticate(String state, String code, String scope) {
		T providerService = provider.providerService;
		if (!providerService.isCodeResponse()) {
			providerService.retrieveVerificationCode(authURL());
		}
		if (providerService.shouldAbortTheProcess()) {
			logoutWithMessage(String.format("Invalid %s parameter! Got: %s, expected: %s",
					providerService.STATE_NAME, state, flash.get(providerService.STATE_NAME)),
					Level.SEVERE
			);
		}
		AuthResult result = dieWithErrorOrRetrieveUserInfo(providerService.retrieveAccessToken(authURL()));
		if (result.user != null) {
			ScopedUser user = result.user;
			if (user.email != null) {
				User u = User.findByEmailAddress(user.email);

				if (u == null) {
					//create new user
					u = new User();
					u.setEmailAddress(user.email);
					// also set displayname, as it's required (fixes /admin/edituser)
					u.setDisplayName(user.name != null ? user.name : user.email);
					u.setAvatarUrl(user.avatar_url != null ? user.avatar_url :
							user.picture != null ? user.picture :
							String.format("https://www.gravatar.com/avatar/%s",DigestUtils.md5Hex(user.email))
					);
					u.setOpenId(user.sub != null ? user.sub : user.id);
					u.setLastAccess(new Date());

					//if this is the very first user, set them as admin
					boolean isFirstUser = User.count() == 0;
					u.setAdmin(isFirstUser);

					u.save();

					// reload user and signin automatically
					u = User.findByEmailAddress(user.email);
					session.put("currentUserId", u.getId());
				} else {
					//login user
					u.setLastAccess(new Date());
					u.save();
					session.put("currentUserId", u.getId());
				}
				redirect("Client.index");
			} else {
				Logger.info("No email, no error! What next?!");
				logoutWithMessage(authorizedWithoutEmailMessage(), Level.WARNING);
			}
		} else {
			Logger.warn("Got error from %s! Error: %s", provider.providerName, result.error);
			logoutWithMessage(String.format("Error response from %s: %s", provider.providerName, result.error), Level.SEVERE);
		}
	};

	protected abstract AuthProvider initProvider();
	private AuthResult dieWithErrorOrRetrieveUserInfo(OAuth2.Response accessTokenResponse) {
		if (accessTokenResponse.error != null) {
			return AuthResult.error(accessTokenResponse.error);
		}
		return new AuthResult(retrieveScopedUser(accessTokenResponse), null);
	}
	protected abstract ScopedUser retrieveScopedUser(OAuth2.Response accessTokenResponse);

	public final String authURL() {
		String controllerName = this.getClass().getSimpleName();
		String actionName = "authenticate";
		Map<String, Object> actionParameters = new HashMap<>();

		F.Tuple<String, String> customizedAuthURL = customizeAuthURL(controllerName, actionName, actionParameters);

		return play.mvc.Router.getFullUrl(customizedAuthURL._1 + "." + customizedAuthURL._2, actionParameters);
	}

	protected F.Tuple<String, String> customizeAuthURL(String controllerName, String actionName, Map<String,
			Object> actionParameters) {
		return new F.Tuple(controllerName, actionName);
	};

	protected String authorizedWithoutEmailMessage() {
		return "Please grant the \"email\" OAuth scope to the application and try again!";
	}

	protected final F.Tuple<String, String> customizeAuthURLWithFragmentRedirect(
			String controllerName, String actionName, Map<String, Object> actionParameters) {
		actionParameters.put("redirectTo", controllerName + "." + actionName);
		return new F.Tuple(Auth.class.getSimpleName(), "fragmentRedirect");
	};

	protected final void logoutWithMessage(String message, Level logLevel) {
		Cache.delete(getCurrentUserCacheKey());
		session.clear();
		if (logLevel.intValue() <= Level.INFO.intValue()) {
			setGeneralMessage(message);
		} else if (logLevel.intValue() <= Level.WARNING.intValue()) {
			setGeneralWarningMessage(message);
		} else if (logLevel.intValue() <= Level.SEVERE.intValue()) {
			setGeneralErrorMessage(message);
		}
		redirect("Auth.login");
	}

	public class AuthProvider {
		public String providerName;
		public String providerId;
		public T providerService;
	}

	public static class AuthResult {
		public final ScopedUser user;
		public final OAuth2.Error error;

		public AuthResult(ScopedUser user, OAuth2.Error error) {
			this.user = user; this.error = error;
		}

		public static AuthResult error(OAuth2.Error error) {
			return new AuthResult(null, error);
		}
	}
}

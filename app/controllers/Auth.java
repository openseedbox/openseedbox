package controllers;

import java.util.Date;
import java.util.logging.Level;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.JsonObject;
import com.openseedbox.Config;
import com.openseedbox.models.User;

import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Router;

public class Auth extends Base {

	private static final String GOOGLE_TOKEN_ENDPOINT = "https://accounts.google.com/o/oauth2/tokeninfo";

	public static void login() {
		renderArgs.put("clientId", Config.getGoogleClientId());
		if (!StringUtils.isEmpty(Config.getKeyCloakClientId())) {
			renderArgs.put("keyCloak", true);
		}
		render();
	}

	public static void logout() {
		logoutWithMessage("Logged out. See you later!", Level.INFO);
	}

	private static void logoutWithMessage(String message, Level logLevel) {
		Cache.delete(getCurrentUserCacheKey());
		session.clear();
		if (logLevel.intValue() <= Level.INFO.intValue()) {
			setGeneralMessage(message);
		} else if (logLevel.intValue() <= Level.WARNING.intValue()) {
			setGeneralWarningMessage(message);
		} else if (logLevel.intValue() <= Level.SEVERE.intValue()) {
			setGeneralErrorMessage(message);
		}
		login();
	}

	public static void fragmentRedirect(String redirectTo) {
		if (redirectTo == null) {
			redirectTo = "Auth.echo"; // "Auth.authenticate"
		}
		redirectTo = Router.reverse(redirectTo).url;
		flash.keep();
		render(redirectTo);
	}

	public static void echo() {
		render();
	}

	public static void authenticate(String id_token) throws Exception {
		HttpResponse googleResponse = WS.url(GOOGLE_TOKEN_ENDPOINT).setParameter("id_token", id_token).getAsync().get();

		JsonObject body = googleResponse.getJson().getAsJsonObject();

		if (body.has("email")) {
			String emailAddress = body.get("email").getAsString();

			User u = User.findByEmailAddress(emailAddress);

			if (u == null) {
				//create new user
				u = new User();
				u.setEmailAddress(emailAddress);
				// also set displayname, as it's required (fixes /admin/edituser)
				u.setDisplayName(emailAddress);
				u.setAvatarUrl(String.format("https://www.gravatar.com/avatar/%s",
						DigestUtils.md5Hex(u.getEmailAddress())));
				u.setLastAccess(new Date());

				//if this is the very first user, set them as admin
				boolean isFirstUser = User.count() == 0;
				u.setAdmin(isFirstUser);

				u.save();

				// reload user and signin automatically
				u = User.findByEmailAddress(emailAddress);
				session.put("currentUserId", u.getId());
			} else {
				//login user
				u.setLastAccess(new Date());
				u.save();
				session.put("currentUserId", u.getId());
			}
			redirect("Client.index");
		} else if (body.has("error")) {
			String errorAsString = body.get("error").getAsString();
			Logger.warn("Got error from Google! Error: %s, token: %s", errorAsString, id_token);

			logoutWithMessage(String.format("Something wrong in the Google response: %s", errorAsString), Level.WARNING);
		} else {
			Logger.info("No email, no error! What next?!");
			logoutWithMessage("Please grant the \"email\" OAuth scope to the application and try again!", Level.WARNING);
		}
	}
}

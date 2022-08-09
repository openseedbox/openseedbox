package controllers;

import com.openseedbox.code.libs.EnhancedOAuth2;
import com.openseedbox.code.libs.OAuth2;
import com.openseedbox.models.JwtBasedScopedUser;
import com.openseedbox.models.ScopedUser;
import com.openseedbox.models.User;
import com.openseedbox.mvc.TemplateNameResolver;
import org.apache.commons.codec.digest.DigestUtils;
import play.Logger;
import play.cache.Cache;
import play.libs.WS;
import play.mvc.Router;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class BaseOAuth<T extends EnhancedOAuth2> extends Base {

	protected AuthProvider provider = initProviderOrLogout();

	public final void authenticate() {
		T providerService = provider.providerService;
		Logger.debug("authenticate(%s): params - %s", this.getClass().getSimpleName(), params.allSimple().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(", ")));
		if (params.all().size() <= 3) { // action, body + provider (from conf/routes: Auth{provider}.{action})
			tryProviderUrlOrLogout(providerService.authorizationURL);
			providerService.retrieveVerificationCode(authURL());
		}
		if (!providerService.isResponseToRetrieveVerificationCode()) {
			logoutWithMessage(String.format("Unkown response parameters from %s: %s", provider.providerName,
					params.allSimple().keySet().stream().filter(s ->
							!s.equalsIgnoreCase("provider") &&
							!s.equalsIgnoreCase("action") &&
							!s.equalsIgnoreCase("body")
					).collect(Collectors.joining(", "))), Level.WARNING);
		}
		if (providerService.isStateParameterValid()) {
			logoutWithMessage(String.format("Invalid %s parameter! Got: %s, expected: %s",
							T.STATE_NAME, params.get(T.STATE_NAME), flash.get(T.STATE_NAME)),
					Level.SEVERE
			);
		}
		tryProviderUrlOrLogout(providerService.accessTokenURL);
		T.Response accessTokenResponse = providerService.retrieveAccessToken(authURL());
		verifyAccessTokenResponse(accessTokenResponse);
		AuthResult result = dieWithErrorOrRetrieveUserInfo(accessTokenResponse);
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

	public final void redirect() {
		if (params.all().size() < 3) {
			Logger.debug("redirect params: %d", params.all().size());
			redirect("Auth.login");
		}
		renderArgs.put("redirectTo", Router.reverse(this.getClass().getSimpleName() + ".authenticate").url);
		flash.keep();
		renderTemplate(new TemplateNameResolver().resolveTemplateName(Auth.class.getSimpleName() + "/" + "fragmentRedirect" + ".html"));
	}

	private AuthProvider initProviderOrLogout() {
		try {
			return initProvider();
		} catch (Throwable e) {
			logoutWithMessage(String.format("Got exception while contacting %s: %s", "auth provider", e.getMessage()), Level.SEVERE);
		}
		// never ever ...
		return null;
	}
	protected abstract AuthProvider initProvider();
	private AuthResult dieWithErrorOrRetrieveUserInfo(T.Response accessTokenResponse) {
		if (accessTokenResponse.error != null) {
			return AuthResult.error(accessTokenResponse.error);
		}
		return new AuthResult(retrieveScopedUser(accessTokenResponse), null);
	}
	protected abstract ScopedUser retrieveScopedUser(T.Response accessTokenResponse);

	public final String authURL() {
		String controllerName = this.getClass().getSimpleName();
		String actionName = customizeAuthURLAction();

		return play.mvc.Router.getFullUrl(controllerName + "." + actionName);
	}

	protected String customizeAuthURLAction() {
		return authUrlActionAuthenticate;
	};
	protected final String authUrlActionAuthenticate = "authenticate";
	protected final String authUrlActionRedirect = "redirect";

	protected String authorizedWithoutEmailMessage() {
		return "Please grant the \"email\" OAuth scope to the application and try again!";
	}

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

	private void tryProviderUrlOrLogout(String url) {
		try {
			WS.url(url).optionsAsync().get(1, TimeUnit.MINUTES);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			logoutWithMessage(String.format("Got exception while contacting %s: %s", provider.providerName, e.getMessage()), Level.SEVERE);
		}
	}

	protected void verifyAccessTokenResponse(T.Response response) {}


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

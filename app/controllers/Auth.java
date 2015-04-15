package controllers;

import com.google.gson.JsonObject;
import com.openseedbox.Config;
import java.util.Date;
import com.openseedbox.models.User;
import net.sf.oval.internal.Log;
import org.apache.commons.codec.digest.DigestUtils;
import play.cache.Cache;
import play.libs.WS;
import play.libs.WS.HttpResponse;

public class Auth extends Base {

        private static final String GOOGLE_TOKEN_ENDPOINT = "https://accounts.google.com/o/oauth2/tokeninfo";

	public static void login() {
                renderArgs.put("clientId", Config.getGoogleClientId());
		renderTemplate("auth/login.html");
	}

	public static void logout() {
		Cache.delete(getCurrentUserCacheKey());
		session.clear();
		login();
	}

	public static void authenticate(String id_token) throws Exception {
                HttpResponse googleResponse = WS.url(GOOGLE_TOKEN_ENDPOINT).setParameter("id_token", id_token).getAsync().get();

                JsonObject body = googleResponse.getJson().getAsJsonObject();

                String emailAddress = body.get("email").getAsString();

                User u = User.findByEmailAddress(emailAddress);

                if (u == null) {
                    //create new user
                    u = new User();
                    u.setEmailAddress(emailAddress);
                    u.setAvatarUrl(String.format("https://www.gravatar.com/avatar/%s",
                                    DigestUtils.md5Hex(u.getEmailAddress())));
                    u.setLastAccess(new Date());
                    u.setAdmin(false);
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
                redirect("/client");
	}
}

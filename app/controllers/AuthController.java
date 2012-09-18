package controllers;

import java.util.Date;
import models.Account;
import models.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.libs.OpenID;
import play.libs.OpenID.UserInfo;

public class AuthController extends BaseController {

	public static void login() {
		renderTemplate("auth/login.html");
	}
	
	public static void logout() {
		Cache.delete(getCurrentUserCacheKey());
		session.clear();
		login();
	}

	public static void authenticate(String openid_url) {
		if (OpenID.isAuthenticationResponse()) {
			UserInfo vu = OpenID.getVerifiedID();
			if (vu == null) {
				setGeneralErrorMessage("Oops. Authentication has failed");
				login();
			}
			//check that user is in database. If not, create.
			User u = User.all().filter("openId", vu.id).get();
			String emailAddress = vu.extensions.get("email");
			
			if (u == null) {
				//check that the email isnt already in the database. if it is, the user is probably being re-authenticated by the provider and sometimes the openID changes.
				if (!StringUtils.isEmpty(emailAddress)) {
					User temp = User.all().filter("emailAddress", emailAddress).get();
					if (temp != null) {
						temp.openId = vu.id;
						temp.save();
						u = temp;
					}
				}			
			}
			
			if (u == null) {
				//email is not already in the database, definitely a new user
				u = new User();
				u.openId = vu.id;
				u.emailAddress = emailAddress;
				if (vu.extensions.containsKey("firstName")) {
					u.displayName = String.format("%s %s", vu.extensions.get("firstName"), vu.extensions.get("lastName"));
				} else {
					u.displayName = vu.extensions.get("fullname");
				}
				if (!StringUtils.isEmpty(u.emailAddress)) {
					u.emailAddress = u.emailAddress.toLowerCase();
					u.avatarUrl = String.format("http://www.gravatar.com/avatar/%s",
							DigestUtils.md5Hex(u.emailAddress));
				}
				u.lastAccess = new Date();
				u.isAdmin = false;
				u.insert();			
				Account a = new Account();
				a.setPrimaryUser(u);
				a.insert();
				u.primaryAccount = a;
				u.update();
			}
			session.put("currentUserId", u.id);
			redirect("/client");
		} else {
			OpenID req = OpenID.id(openid_url);
			req.required("email", "http://axschema.org/contact/email");
			req.required("firstName", "http://axschema.org/namePerson/first");
			req.required("lastName", "http://axschema.org/namePerson/last");

			// Simple Registration (SREG)
			req.required("email");
			req.optional("fullname");
			
			if (!req.verify()) {
				setGeneralErrorMessage("Cannot verify your OpenID");
				login();
			}
		}
	}
}

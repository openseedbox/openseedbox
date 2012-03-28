package controllers;

import code.GenericResult;
import play.mvc.Before;
import play.mvc.Controller;
import play.templates.Template;
import play.templates.TemplateLoader;
import controllers.securesocial.SecureSocial;
import models.User;
import play.Logger;
import play.mvc.Catch;
import securesocial.provider.*;

public class BaseController extends Controller {
	
	protected static long currentUserId;
	
	@Before
	protected static void before() {
		SocialUser su = SecureSocial.getCurrentUser();
		if (su != null) {
			//check user exists in database
			User inDb = User.all().filter("oauthId", su.id.id).get();
			//if not, add
			if (inDb == null) {
				Logger.debug("OAuth user %s not in database, adding.", su.email);
				User u = new User();
				u.oauthId = su.id.id;
				u.emailAddress = su.email;
				u.maxDiskspaceGB = 1;
				u.isAdmin = false;
				u.insert();
				currentUserId = u.id;
			} else {
				currentUserId = inDb.id;
			}
			renderArgs.put("currentUser", getCurrentUser());
		} else {
			Logger.debug("User was null, logging out");
			redirect("/auth/logout");
		}	
	}
	
	protected static User getCurrentUser() {
		return User.findById(currentUserId);
	}
	
	protected static void result(Object o) {
		throw new GenericResult(o);
	}

	protected static void resultTemplate(String name) {
		Template t = TemplateLoader.load(name);
		throw new GenericResult(t.render());
	}

	protected static void resultError(String message) {
		throw new GenericResult(message, true);
	}

	@Catch(Exception.class)
	protected static void onException(Exception ex) {
		if (params.get("ext") != null && params.get("ext").equals("json")) {
			resultError(ex.getMessage());
		}
	}
}
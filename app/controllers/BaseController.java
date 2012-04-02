package controllers;

import code.GenericResult;
import controllers.securesocial.SecureSocial;
import models.User;
import org.h2.util.StringUtils;
import play.mvc.Before;
import play.mvc.Catch;
import play.mvc.Controller;
import play.templates.Template;
import play.templates.TemplateLoader;
import securesocial.provider.SocialUser;

public class BaseController extends Controller {
	
	@Before
	protected static void before() {
		User u = getCurrentUser();
		renderArgs.put("activeUser", getActualUser());
		renderArgs.put("currentUser", u);
		if (!request.url.contains("requireEmail")) {
			if (u != null && StringUtils.isNullOrEmpty(u.emailAddress)) {
				redirect("/main/requireEmail");
			}	
		}
	}
	
	protected static User getCurrentUser() {
		SocialUser su = SecureSocial.getCurrentUser();
		if (su != null) {
			return User.fromSocialUser(su);
		}
		return null;
	}
	
	protected static User getActualUser() {
		long actualUserId = 0;
		if (session.contains("actualUserId")) {
			String s = session.get("actualUserId");
			actualUserId = Long.parseLong(s);
		}
		if (actualUserId > 0) {
			return User.getByKey(actualUserId);
		}
		return getCurrentUser();
	}
	
	protected static String getServerPath() {
		return String.format("http://%s", request.host);
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
package controllers;

import code.GenericResult;
import models.User;
import org.h2.util.StringUtils;
import play.mvc.Before;
import play.mvc.Catch;
import play.mvc.Controller;
import play.templates.Template;
import play.templates.TemplateLoader;

public class BaseController extends Controller {
	
	@Before
	protected static void before() {
		User u = getCurrentUser();
		renderArgs.put("currentUser", u);
		if (!request.url.contains("requireEmail")) {
			if (u != null && StringUtils.isNullOrEmpty(u.emailAddress)) {
				redirect("/main/requireEmail");
			}	
		}
	}
	
	protected static User getCurrentUser() {
		long currentUserId = 0;
		if (session.contains("currentUserId")) {
			String s = session.get("currentUserId");
			currentUserId = Long.parseLong(s);
		}
		if (currentUserId > 0) {
			return User.getByKey(currentUserId);
		}
		return null;
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
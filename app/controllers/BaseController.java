package controllers;

import code.GenericResult;
import java.util.HashMap;
import java.util.Map;
import models.User;
import org.h2.util.StringUtils;
import play.data.validation.Validation;
import play.exceptions.UnexpectedException;
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
	
	private static User _user;
	protected static User getCurrentUser() {
		if (_user != null) {
			return _user;
		}
		long currentUserId = 0;
		if (session.contains("currentUserId")) {
			String s = session.get("currentUserId");
			currentUserId = Long.parseLong(s);
		}
		if (currentUserId > 0) {
			_user = User.getByKey(currentUserId);
		}
		return _user;
	}
	
	protected static void addGeneralError(Exception ex) {
		Validation.addError("general", ex.getMessage());
	}
	
	protected static String getServerPath() {
		return String.format("http://%s", request.host);	
	}
	
	protected static String renderToString(String template) {
		return renderToString(template, new HashMap<String, Object>());
	}
	
	protected static String renderToString(String template, Map<String, Object> args) {
		Template t = TemplateLoader.load(template);
		return t.render(args);		
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
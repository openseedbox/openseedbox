package controllers;

import code.MessageException;
import code.jobs.PlanSwitcherJob;
import models.PlanSwitch;
import models.User;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import play.libs.Mail;

public class MainController extends BaseController {

	public static void index() throws MessageException {
		ClientController.index();
	}
	
	public static void testEmail() {
		try {
			SimpleEmail se = new SimpleEmail();
			se.setFrom("noreply@myseedbox.com");
			se.addTo("erin.dru@gmail.com");
			se.setSubject("Test email from myseedbox");
			se.setMsg("Testy Testy test");
			Mail.send(se);
		} catch (EmailException ex) {
			resultError(ex.toString());
		}
		result("Yay!");
	}
	
	public static void triggerError() throws Exception {
		throw new Exception("Test error!");
	}
	
	public static void ping() {
		result("pong");
	}

}

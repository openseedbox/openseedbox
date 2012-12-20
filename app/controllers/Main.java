package controllers;

import com.openseedbox.code.MessageException;
import models.Node;
import notifiers.Mails;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import play.libs.Mail;

public class Main extends Base {

	public static void index() {
		Client.index(null);
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
	
	public static void testNodeDown() {
		Mails.nodeDown(Node.getBestForNewTorrent(), null);
	}
	
	public static void testNodeBackUp() {
		Mails.nodeBackUp(Node.getBestForNewTorrent());
	}	
	
	public static void triggerError() throws Exception {
		throw new Exception("Test error!");
	}
	
	public static void ping() {
		result("pong");
	}

}

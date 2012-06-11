package notifiers;

import code.Util;
import models.Invitation;
import org.apache.commons.lang.StringUtils;
import play.mvc.Http.Request;
import play.mvc.Mailer;

public class Mails extends Mailer {

	public static void inviteUser(Invitation invitation) {
		setContentType("text/html");
		setSubject(invitation.getInvitingUser().displayName + " wants to share their seedbox with you!");
		addRecipient(invitation.emailAddress);
		setFrom("noreply@openseedbox.com");
		send(invitation);
	}
	
	public static void sendError(Exception exception, Request request) {
		setContentType("text/html");
		setSubject("An OpenSeedbox error occured!");
		String address = play.Play.configuration.getProperty("errors.mailto");
		if (!StringUtils.isEmpty(address)) {
			addRecipient(address);
			setFrom("errors@openseedbox.com");
			String stackTrace = Util.getStackTrace(exception);
			send(exception, stackTrace, request);
		}
	}
	
}

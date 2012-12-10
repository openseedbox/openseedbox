package notifiers;

import models.Invitation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import play.mvc.Http.Request;
import play.mvc.Mailer;

public class Mails extends Mailer {

	public static void inviteUser(Invitation invitation) {
		setContentType("text/html");
		setSubject(invitation.getInvitingUser().getDisplayName() + " wants to share their seedbox with you!");
		addRecipient(invitation.getEmailAddress());
		setFrom("noreply@openseedbox.com");
		send(invitation);
	}
	
	public static void sendError(Throwable exception, Request request) {
		setContentType("text/html");
		setSubject("An OpenSeedbox error occured!");
		String address = play.Play.configuration.getProperty("errors.mailto");
		if (!StringUtils.isEmpty(address)) {
			addRecipient(address);
			setFrom("errors@openseedbox.com");
			String stackTrace = ExceptionUtils.getStackTrace(exception);
			send(exception, stackTrace, request);
		}
	}
	
}

package notifiers;

import models.Invitation;
import play.mvc.Mailer;

public class Mails extends Mailer {

	public static void inviteUser(Invitation invitation) {
		setContentType("text/html");
		setSubject(invitation.getInvitingUser().displayName + " wants to share their seedbox with you!");
		addRecipient(invitation.emailAddress);
		setFrom("noreply@openseedbox.com");
		send(invitation);
	}
	
}

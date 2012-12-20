package notifiers;

import com.openseedbox.Config;
import java.net.ConnectException;
import models.Invitation;
import models.Node;
import org.apache.commons.lang.exception.ExceptionUtils;
import play.mvc.Http.Request;
import play.mvc.Mailer;

public class Mails extends Mailer {

	public static void inviteUser(Invitation invitation) {		
		setSubject(invitation.getInvitingUser().getDisplayName() + " wants to share their seedbox with you!");
		addRecipient(invitation.getEmailAddress());
		setFrom("noreply@openseedbox.com");
		send(invitation);
	}
	
	public static void sendError(Throwable exception, Request request) {		
		setSubject("An OpenSeedbox error occured!");		
		addRecipient(Config.getErrorEmailAddress());
		setFrom(Config.getErrorFromEmailAddress());
		String stackTrace = ExceptionUtils.getStackTrace(exception);
		send(exception, stackTrace, request);		
	}
	
	public static void nodeDown(Node node, Throwable exactError) {
		setSubject("Node '" + node.getName() + "' is down!");
		addRecipient(Config.getErrorEmailAddress());
		setFrom(Config.getErrorFromEmailAddress());
		String stackTrace = "Node wont respond to pings.";
		if (exactError != null) {
			stackTrace = ExceptionUtils.getStackTrace(exactError);
		}		
		String status = "down";
		send(node, stackTrace, status);		
	}
	
	public static void nodeBackUp(Node node) {
		setSubject("Node '" + node.getName() + "' back up!");
		addRecipient(Config.getErrorEmailAddress());
		setFrom(Config.getErrorFromEmailAddress());
		String status = "back up";				
		send("Mails/nodeDown", node, status);
	}
	
}

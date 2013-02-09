package com.openseedbox.notifiers;

import com.openseedbox.Config;
import com.openseedbox.models.Node;
import org.apache.commons.lang.exception.ExceptionUtils;
import play.mvc.Http.Request;
import play.mvc.Mailer;

public class Mails extends Mailer {
	
	public static void sendError(Throwable exception, Request request) {		
		setContentType("text/html");
		setSubject("An OpenSeedbox error occured!");	
		addRecipient(Config.getErrorEmailAddress());
		setFrom(Config.getErrorFromEmailAddress());
		String stackTrace = ExceptionUtils.getStackTrace(exception);
		send("mails/sendError", exception, stackTrace, request);		
	}
	
	public static void nodeDown(Node node, Throwable exactError) {
		setContentType("text/html");
		setSubject("Node '" + node.getName() + "' is down!");
		addRecipient(Config.getErrorEmailAddress());
		setFrom(Config.getErrorFromEmailAddress());
		String stackTrace = "Node wont respond to pings.";
		if (exactError != null) {
			stackTrace = ExceptionUtils.getStackTrace(exactError);
		}		
		String status = "down";		
		send("mails/nodeDown", node, stackTrace, status);		
	}
	
	public static void nodeBackUp(Node node) {
		setContentType("text/html");
		setSubject("Node '" + node.getName() + "' back up!");
		addRecipient(Config.getErrorEmailAddress());
		setFrom(Config.getErrorFromEmailAddress());
		String status = "back up";				
		send("mails/nodeDown", node, status);
	}
	
}

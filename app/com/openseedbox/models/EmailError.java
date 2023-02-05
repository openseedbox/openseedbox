package com.openseedbox.models;

import com.openseedbox.notifiers.Mails;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import play.Logger;
import play.templates.JavaExtensions;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class EmailError extends ModelBase {

	@Column(name = "error_key", unique = true)
	private String key;
	
	private String sentToEmailAddress;
	
	private Date lastSent;

  @NotNull
	private int sendCount;
	
	
	public static void nodeDown(Node node, Throwable error) {
		String downKey = getNodeDownErrorKey(node);
		String upKey = getNodeBackUpErrorKey(node);
		if (hasntBeenAnEmailSince(downKey, 10)) {
			Logger.info("Sending nodeDown");
			Mails.nodeDown(node, error);
			updateLastSentForKey(downKey);			
			resetLastSentForKey(upKey); //incase it went down again and comes back up in less than 10 minutes from when the last 'back up' email was sent
		}		
	}
	
	public static void nodeBackUp(Node node) {		
		String downKey = getNodeDownErrorKey(node);
		String upKey = getNodeBackUpErrorKey(node);		
		if (hasntBeenAnEmailSince(upKey, 10)) {
			Logger.info("Sending nodeBackUp");
			Mails.nodeBackUp(node);			
			updateLastSentForKey(upKey);
			resetLastSentForKey(downKey); //incase it goes down again in less than 10 minutes after the 'back up' email was sent
		}		
	}
	
	public static boolean hasntBeenAnEmailSince(String key, int minutes) {
		Date now = new Date();
		long newMillis = now.getTime() - ((minutes * 60) * 1000);
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(newMillis);	
		EmailError e = getForKey(key);
		if (e.getSendCount() >= 3) { //if more than 3 have been sent, stop spamming the inbox
			return false;
		}
		e.setSendCount(e.getSendCount() + 1);
		if (e.getLastSent() == null) { return true; }
		Date ls = e.getLastSent();
		return (ls.before(c.getTime()));
	}
	
	public static EmailError getForKey(String key) {
		EmailError e =  EmailError.<EmailError>all()
				.where()
				.eq("key", key)
				.findUnique();
		if (e == null) {
			e = new EmailError();
			e.setKey(key);
			e.setLastSent(null);
			e.save();					
		}
		return e;
	}
	
	private static void updateLastSentForKey(String key) {
		setLastSentForKey(key, new Date());
	}
	
	private static void resetLastSentForKey(String key) {
		setLastSentForKey(key, null);
	}	
	
	private static void setLastSentForKey(String key, Date date) {
		EmailError e = getForKey(key);
		if (e != null) {
			e.setLastSent(date);
			e.setSendCount(0);
			e.save();
		}		
	}	
	
	private static String getNodeDownErrorKey(Node node) {
		return "node." + JavaExtensions.slugify(node.getName()) + ".down";
	}
	
	private static String getNodeBackUpErrorKey(Node node) {
		return "node." + JavaExtensions.slugify(node.getName()) + ".back-up";
	}
	
	/* Getters and Setters */

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSentToEmailAddress() {
		return sentToEmailAddress;
	}

	public void setSentToEmailAddress(String sentToEmailAddress) {
		this.sentToEmailAddress = sentToEmailAddress;
	}

	public Date getLastSent() {
		return lastSent;
	}

	public void setLastSent(Date lastSent) {
		this.lastSent = lastSent;
	}

	public int getSendCount() {
		return sendCount;
	}

	public void setSendCount(int sendCount) {
		this.sendCount = sendCount;
	}		
}

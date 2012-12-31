package com.openseedbox.models;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import com.openseedbox.notifiers.Mails;
import play.Logger;
import play.templates.JavaExtensions;
import siena.Column;
import siena.Table;
import siena.Unique;

@Table("email_error")
public class EmailError extends ModelBase {
	
	@Unique("error_key_unique") @Column("error_key") private String key;
	
	@Column("sent_to_email_address") private String sentToEmailAddress;
	
	@Temporal(TemporalType.TIMESTAMP) private Date lastSent;
	
	
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
		if (e.getLastSent() == null) { return true; }
		Date ls = e.getLastSent();
		return (ls.before(c.getTime()));
	}
	
	public static EmailError getForKey(String key) {
		EmailError e =  EmailError.all().filter("key", key).get();
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
	
}

package com.openseedbox.models;

import com.openseedbox.code.Util;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import play.db.jpa.Model;

@Table(name="invitation")
public class Invitation extends Model {
	
	@Column(name="email_address")
	protected String emailAddress;
	
	@Column(name="user_id")
	protected User invitingUser;
	
	@Column(name="invitation_date")
	@Temporal(TemporalType.TIMESTAMP)
	protected Date invitationDate;	
	
	@Column(name="is_accepted")
	protected boolean isAccepted;
	
	public String getInvitationDateLocal() {
		return null;//Util.formatDateTime(Util.getLocalDate(invitationDate, getInvitingUser()));
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}
	
	public User getInvitingUser() {
		return invitingUser;
	}
	
	public boolean isAccepted() {
		return isAccepted;
	}
}

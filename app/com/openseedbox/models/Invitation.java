package com.openseedbox.models;

import com.openseedbox.code.Util;
import java.util.Date;
import javax.persistence.*;

@Entity
public class Invitation extends ModelBase {
	
	protected String emailAddress;

	@ManyToOne
	@Column(name="user_id")
	protected User invitingUser;
	
	@Temporal(TemporalType.TIMESTAMP)
	protected Date invitationDate;	
	
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

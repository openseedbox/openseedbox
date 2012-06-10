package models;

import code.Util;
import java.util.Date;
import org.joda.time.DateTime;
import play.modules.siena.EnhancedModel;
import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Table;

@Table("invitation")
public class Invitation extends EnhancedModel {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Column("email_address")
	public String emailAddress;
	
	@Column("user_id")
	public User invitingUser;
	
	@Column("invitation_date") @siena.DateTime
	public Date invitationDate;	
	
	public String getInvitationDateLocal() {
		return Util.formatDateTime(Util.getLocalDate(invitationDate, getInvitingUser()));
	}
	
	public User getInvitingUser() {
		return User.getByKey(invitingUser.id);
	}
	
	public boolean isAccepted() {
		return User.all().filter("emailAddress", this.emailAddress).count() > 0;
	}
}

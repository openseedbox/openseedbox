package models;

import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import play.modules.siena.EnhancedModel;
import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Table;

@Table("invited_user")
public class InvitedUser extends EnhancedModel {
	
	@Id(Generator.AUTO_INCREMENT)
	public long id;
	
	@siena.DateTime
	@Column("invitation_date")
	public Date invitationDate;
	
	@Column("inviting_user")
	public User invitingUser;
	
	@Column("actual_user")
	public User actualUser;
	
	@Column("email_address")
	public String emailAddress;
	
	@Column("accepted")
	public Boolean accepted;
	
	public User getInvitingUser() {
		User u = User.getByKey(this.invitingUser.id);
		return u;
	}
	
	public User getActualUser() {
		if (this.actualUser != null) {
			return User.getByKey(this.actualUser.id);
		}
		return null;
	}
	
	public String getPendingFor() {
		DateTime now = DateTime.now();
		DateTime created = new DateTime(this.invitationDate);
		Days days = Days.daysBetween(created, now);
		Hours hours = Hours.hoursBetween(created, now);
		Minutes minutes = Minutes.minutesBetween(created, now);
		if (days.getDays() > 0) {
			return String.format("%d days", days.getDays());
		} else if (hours.getHours() > 0) {
			return String.format("%d hours", hours.getHours());
		} else {
			return String.format("%d minutes", minutes.getMinutes());
		}
	}
	
	public String getStatus() {
		if (getActualUser() == null) {
			return "Email Sent";
		}
		return (this.accepted) ? "Accepted" : "Rejected";
	}
	
	public Boolean isActivated() {
		User u = getActualUser();
		if (u != null) {
			return u.isActivated;
		}
		return false;
	}
	
}

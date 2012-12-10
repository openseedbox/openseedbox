package models;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import play.db.jpa.Model;

@Entity
@Table(name="user_message")
public class UserMessage extends Model {
	
	public static enum State {
		MESSAGE, ERROR
	}
	
	public static enum Type {
		GENERAL, SWITCHPLAN, LIMITSEXCEEDED
	}
	
	private State state;
	
	private Type type;
	
	private String heading;
	
	private String message;
	
	@Column(name="user_id")
	private User user;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="create_date_utc")
	private Date createDateUtc;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="dismiss_date_utc")
	private Date dismissDateUtc;
	
	/* Getters and Setters */

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getHeading() {
		return heading;
	}

	public void setHeading(String heading) {
		this.heading = heading;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getCreateDateUtc() {
		return createDateUtc;
	}

	public void setCreateDateUtc(Date createDateUtc) {
		this.createDateUtc = createDateUtc;
	}

	public Date getDismissDateUtc() {
		return dismissDateUtc;
	}

	public void setDismissDateUtc(Date dismissDateUtc) {
		this.dismissDateUtc = dismissDateUtc;
	}
	
}

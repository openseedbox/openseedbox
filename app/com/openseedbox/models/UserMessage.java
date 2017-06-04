package com.openseedbox.models;

import java.util.Date;
import java.util.List;
import siena.Column;
import siena.Table;
import siena.Text;

@Table("user_message")
public class UserMessage extends ModelBase {
	
	public static enum State {
		MESSAGE, ERROR
	}
	
	private State state;			
	private String heading;	
	@Text private String message;
	@Column("user_id") private User user;		
	@Column("create_date") private Date createDate;	
	private boolean retrieved;
	
	public UserMessage() {
		retrieved = false;
		createDate = new Date();
		state = State.ERROR;
	}
	
	/**
	 * Retrieve all unretrieved messages for the specified user.
	 * Note: retrieving an unretrieved message marks it as retrieved!
	 * @param u The user to retrieve unretrieved messages for
	 * @return A list of unretrieved messages
	 */
	public static List<UserMessage> retrieveForUser(User u) {
		List<UserMessage> all = UserMessage.all().filter("user", u).filter("retrieved", false).fetch();
		for (UserMessage um : all) {
			um.setRetrieved(true);
		}
		UserMessage.batch().update(all);
		return all;
	}
	
	/* Getters and Setters */

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
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

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public boolean isRetrieved() {
		return retrieved;
	}

	public void setRetrieved(boolean retrieved) {
		this.retrieved = retrieved;
	}

}

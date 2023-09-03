package com.openseedbox.models;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
public class UserMessage extends ModelBase {
	
	public static enum State {
		MESSAGE, ERROR
	}

  @Enumerated(EnumType.STRING)
	@NotNull
	private State state;
	@NotNull
	private String heading;	
	@NotNull
  @Lob
	private String message;
	@NotNull
	@ManyToOne
	private User user;
	@NotNull
	private Date createDate;
	@NotNull
	private boolean retrieved;
	
	public UserMessage(User user, String heading, String message) {
		retrieved = false;
		createDate = new Date();
		state = State.ERROR;

		this.user = user;
		this.heading = heading;
		this.message = message;
	}
	
	/**
	 * Retrieve all unretrieved messages for the specified user.
	 * Note: retrieving an unretrieved message marks it as retrieved!
	 * @param u The user to retrieve unretrieved messages for
	 * @return A list of unretrieved messages
	 */
	public static List<UserMessage> retrieveForUser(User u) {
		List<UserMessage> all = UserMessage.<UserMessage>all()
				.where()
				.eq("user", u)
				.eq("retrieved", false)
				.findList();
		for (UserMessage um : all) {
			um.setRetrieved(true);
		}
		save(all);
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

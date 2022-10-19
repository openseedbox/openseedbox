package com.openseedbox.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;
import java.util.List;

/**
 * When a torrent is added, it gets put in a queue and processed by a job.
 * Same for when its deleted. This class represents a torrent event.
 * Note: The fields in this class are not on the Torrent object itself in case
 * there were errors, we do not want the torrent to exist in the database if it was
 * not successfully added
 * @author Erin Drummond
 */
@Entity
public class TorrentEvent extends EventBase {

	private String torrentHash;

	//@Column(name = "user_id") ??
	private User user;

	@Enumerated(EnumType.STRING)
	private TorrentEventType eventType;

	private boolean userNotified;
	
	public TorrentEvent(TorrentEventType type, User user) {		
		this.eventType = type;
		this.user = user;
		this.save();
	}
	
	public static List<TorrentEvent> getIncompleteForUser(User u, TorrentEventType eventType) {
		return TorrentEvent.<TorrentEvent>all().where().eq("user", u)
				  .isNull("completionDate").eq("eventType", eventType)
				  .orderBy("eventType, startDate").findList();
	}
	
	public static List<TorrentEvent> getOlderThanMinutes(int minutes) {
		Date nowMinusMinutes = new Date(System.currentTimeMillis() - (minutes * 1000 * 60));
		return TorrentEvent.<TorrentEvent>all().where()
				  .isNull("completionDate")
				  .lt("startDate", nowMinusMinutes).findList();
	}

	public static int deleteOlderThan(Date date) {
		return TorrentEvent.<TorrentEvent>all().where().lt("startDate", date).delete();
	}

	public enum TorrentEventType {
		ADDING, REMOVING
	}
	
	/* Getters and Setters */
	public String getTorrentHash() {
		return torrentHash;
	}

	public void setTorrentHash(String torrentHash) {
		this.torrentHash = torrentHash;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public TorrentEventType getEventType() {
		return eventType;
	}

	public void setEventType(TorrentEventType eventType) {
		this.eventType = eventType;
	}

	public boolean isUserNotified() {
		return userNotified;
	}

	public void setUserNotified(boolean userNotified) {
		this.userNotified = userNotified;
	}
	
}

package com.openseedbox.models;

import java.util.Date;
import java.util.List;
import siena.Column;
import siena.Table;

/**
 * When a torrent is added, it gets put in a queue and processed by a job.
 * Same for when its deleted. This class represents a torrent event.
 * Note: The fields in this class are not on the Torrent object itself in case
 * there were errors, we do not want the torrent to exist in the database if it was
 * not successfully added
 * @author Erin Drummond
 */
@Table("torrent_event")
public class TorrentEvent extends EventBase {

	@Column("torrent_hash") private String torrentHash;
	@Column("user_id") private User user;
	@Column("event_type") private TorrentEventType eventType;
	@Column("user_notified") private boolean userNotified;
	
	public TorrentEvent(TorrentEventType type, User user) {		
		this.eventType = type;
		this.user = user;
		this.insert();
	}
	
	public static List<TorrentEvent> getIncompleteForUser(User u, TorrentEventType eventType) {
		return TorrentEvent.all().filter("user", u)
				  .filter("completionDate", null).filter("eventType", eventType)
				  .order("eventType").order("startDate").fetch();
	}
	
	public static TorrentEvent getIncompleteForUser(User u, String torrentHash) {
		//There should be only one _incomplete_ TorrentEvent per user/torrentHash!
		return null;/*
		return TorrentEvent.all().filter("user", u)
				  .filter("completionDate", null).filter("torrentHash", torrentHash).get();*/
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

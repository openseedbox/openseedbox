package com.openseedbox.backend;

/**
 * Represents some statistics for the current session.
 * The current session is calculated based on all the torrents the user has
 * running in the backend
 * 
 * @author Erin Drummond
 */
public interface ISessionStatistics {
	
	/**
	 * Gets the total amount of bytes that the user has uploaded
	 * across all torrents.
	 * @return  The number of bytes uploaded
	 */
	public long getTotalUploadedBytes();
	
	/**
	 * Gets the total amount of bytes that the user has downloaded
	 * across all their torrents
	 * @return The number of bytes downloaded
	 */
	public long getTotalDownloadedBytes();
	
	/**
	 * Gets the total upload speed of all the users torrents (in bytes/sec)
	 * @return The upload speed in bytes/sec
	 */
	public long getTotalUploadSpeedBytes();
	
	/**
	 * Gets the total download speed of all the users torrents (in bytes/sec)
	 * @return  The download speed in bytes/sec
	 */
	public long getTotalDownloadSpeedBytes();
	
	/**
	 * Gets the total number of torrents the user has in the backend
	 * @return The number of torrents
	 */
	public int getTorrentCount();
}

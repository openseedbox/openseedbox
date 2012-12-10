package com.openseedbox.backend;

/**
 * Represents a Torrent tracker
 * @author Erin Drummond
 */
public interface ITracker {
	
	/**
	 * @return The tracker host name/url
	 */
	public String getHost();
	
	/**
	 * @return The number of leechers downloading this torrent
	 */
	public int getLeecherCount();
	
	/**
	 * @return The number of seeders seeding this torrent
	 */
	public int getSeederCount();
	
	/**
	 * @return The number of people who have downloaded this torrent
	 */
	public int getDownloadCount();
	
}

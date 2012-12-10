package com.openseedbox.backend;

import java.util.List;
import java.util.Set;


/**
 * Represents a torrent
 * @author Erin Drummond
 */
public interface ITorrent {
	
	/**
	 * Gets the name of the torrent
	 * @return The torrent name
	 */
	public String getName();
	
	/**
	 * This is mainly used for the UI to look different when a torrent isnt running
	 * A torrent is considered running if its not paused or queued (ie, its downloading or seeding)
	 * @return True if the torrent is running, false otherwise
	 */
	public boolean isRunning();
	
	/**
	 * Gets the percent complete of the metadata (eg, if the torrent was being downloaded from a magnet)
	 * Should return 100 if metdata has finished downloading (ie, the backend has the whole torrent)
	 * @return The percent complete of the metadata, between 0 and 100
	 */
	public double getMetadataPercentComplete();
	
	/**
	 * Gets the percentage completed of this torrent
	 * @return The percent complete, between 0 and 100
	 */
	public double getPercentComplete();
	
	/**
	 * Gets the download speed of this torrent, in bytes/sec
	 * @return The download speed, in bytes/sec
	 */
	public long getDownloadSpeedBytes();
	
	/**
	 * Gets the upload speed of this torrent, in bytes/sec
	 * @return The upload speed, in bytes/sec
	 */
	public long getUploadSpeedBytes();
	
	/**
	 * Gets the info_hash of this torrent
	 * @return The info_hash
	 */
	public String getTorrentHash();
	
	/**
	 * Returns true if the torrent is in an error state on the backend (eg,
	 * run out of disk space etc)
	 * @return True if in error state, false if otherwise
	 */
	public boolean hasErrorOccured();
	
	/**
	 * Returns an error message if the torrent is in error state, or null
	 * if the torrent is fine
	 * @return An error message, or null if there is no error
	 */
	public String getErrorMessage();
	
	/**
	 * Gets the total file size of the torrent, in bytes
	 * @return The total size of the torrent in bytes
	 */
	public long getTotalSizeBytes();
	
	/**
	 * Gets the amount of data that has been currently downloaded, in bytes
	 * @return The amount downloaded in bytes
	 */
	public long getDownloadedBytes();
	
	/**
	 * Gets the amount of data that has been uploaded, in bytes
	 * @return The amount uploaded, in bytes
	 */
	public long getUploadedBytes();
	
	/**
	 * Gets the current status of the torrent
	 * @return a @TorrentState object representing the status
	 */
	public TorrentState getStatus();
	
	/**
	 * Gets a list of the files in a torrent
	 * @return The list of files
	 */
	public List<IFile> getFiles();
	
	/**
	 * Gets a list of the peers in a torrent
	 * @return The list of peers
	 */
	public List<IPeer> getPeers();
	
	/**
	 * Gets a list of the trackers in a torrent
	 * @return The list of trackers
	 */
	public List<ITracker> getTrackers();
	
	public static enum TorrentState {
		METADATA_DOWNLOADING, DOWNLOADING, PAUSED, SEEDING, ERROR
	}
}

package com.openseedbox.backend;

/**
 * Represents a peer in a torrent swarm
 * @author Erin Drummond
 */
public interface IPeer {
	
	/**
	 * Gets the name of the torrent client the peer is using
	 * @return The name of the client
	 */
	public String getClientName();
	
	/**
	 * @return True if we are downloading from this peer 
	 */
	public boolean isDownloadingFrom();
	
	/**
	 * @return True if we are uploading to this peer 
	 */
	public boolean isUploadingTo();
	
	/**
	 * @return True if our connection to this peer is encrypted
	 */
	public boolean isEncryptionEnabled();
	
	/**
	 * @return The rate we are downloading from this peer at (in bytes/sec)
	 */
	public long getDownloadRateBytes();
	
	/**
	 * @return The rate we are uploading to this peer at (in bytes/sec)
	 */
	public long getUploadRateBytes();	
}

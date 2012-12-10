package com.openseedbox.backend;

/**
 * Represents a file in a torrent
 * @author Erin Drummond
 */
public interface IFile {
	
	/**
	 * The name of the file, eg MyVideo.mkv
	 * @return The file name
	 */
	public String getName();
	
	/**
	 * The full path of the file in the torrent directory structure,
	 * eg /videos/mkv/MyVideo.mkv
	 * @return The file full path
	 */
	public String getFullPath();
	
	/**
	 * Users can select/deselect files from torrents. If the file
	 * is wanted, the user has selected it, otherwise they have
	 * deselected it.
	 * @return Whether or not the file is wanted
	 */
	public boolean isWanted();
	
	/**
	 * Get the download progress of this file, in bytes
	 * @return The bytes downloaded
	 */
	public long getBytesCompleted();
	
	/**
	 * Get the total size of this file, in bytes
	 * @return The total size of the file
	 */
	public long getFileSizeBytes();
	
	/**
	 * Users can set the download priority of a file. Higher numbers will be downloaded
	 * first. This method should return -1, 0 or 1 where:
	 *  -1 = Low Priority
	 *   0 = Normal Priority
	 *   1 = High Priority
	 * @return The priority of this file
	 */
	public int getPriority();
	
}

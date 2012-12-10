package com.openseedbox.backend;

import java.util.ArrayList;
import java.util.List;
import play.data.validation.Required;

/**
 * Represents a standard backend configuration that a specific backend
 * implementation should be able to adhere to
 * @author Erin Drummond
 */
public class BackendConfig {
	
	@Required
	private String completeFolder;
	
	@Required
	private String incompleteFolder;
	
	@Required
	private String torrentFolder;
	
	@Required
	private String baseFolder;
	
	private double ratioLimit;
	private long uploadSpeedLimitKb;
	private long downloadSpeedLimitKb;
	
	private List<String> blocklistUrls;
	
	public static BackendConfig getDefaultConfig() {
		BackendConfig b = new BackendConfig();
		b.setCompleteFolder("/torrents/complete");
		b.setIncompleteFolder("/torrents/incomplete");
		b.setTorrentFolder("/torrents/metadata");
		b.setBaseFolder("/openseedbox");
		b.setRatioLimit(2.0);
		b.setDownloadSpeedLimitKb(-1);
		b.setUploadSpeedLimitKb(-1);
		List<String> bl = new ArrayList<String>();
		bl.add("http://list.iblocklist.com/?list=bt_level1&fileformat=p2p&archiveformat=gz");
		b.setBlocklistUrls(bl);
		return b;		
	}
	
	/* Getters and Setters */

	/**
	 * @return The folder that web-related files should be stored in (eg, site config, ssl certs)
	 */
	public String getBaseFolder() {
		return baseFolder;
	}

	public void setBaseFolder(String httpFolder) {
		this.baseFolder = httpFolder;
	}
	
	/**
	 * @return The folder that completed torrents should go in
	 */
	public String getCompleteFolder() {
		return completeFolder;
	}

	public void setCompleteFolder(String completeFolder) {
		this.completeFolder = completeFolder;
	}

	/**
	 * @return The folder that incomplete torrents should go in
	 * (they should be moved to the 'complete' folder on completion)
	 */
	public String getIncompleteFolder() {
		return incompleteFolder;
	}

	public void setIncompleteFolder(String incompleteFolder) {
		this.incompleteFolder = incompleteFolder;
	}

	/**
	 * @return The folder that torrent files should be downloaded to in the case
	 * of a url or magnet being requested
	 */
	public String getTorrentFolder() {
		return torrentFolder;
	}

	public void setTorrentFolder(String torrentFolder) {
		this.torrentFolder = torrentFolder;
	}

	/**
	 * @return The default ratio limit that a torrent should stop seeding at
	 */
	public double getRatioLimit() {
		return ratioLimit;
	}

	public void setRatioLimit(double ratioLimit) {
		this.ratioLimit = ratioLimit;
	}

	/**
	 * @return The maximum total upload speed (in kb/s)
	 */
	public long getUploadSpeedLimitKb() {
		return uploadSpeedLimitKb;
	}

	public void setUploadSpeedLimitKb(long uploadSpeedLimitKb) {
		this.uploadSpeedLimitKb = uploadSpeedLimitKb;
	}

	/**
	 * @return The maximum total download speed (in kb/s)
	 */
	public long getDownloadSpeedLimitKb() {
		return downloadSpeedLimitKb;
	}

	public void setDownloadSpeedLimitKb(long downloadSpeedLimitKb) {
		this.downloadSpeedLimitKb = downloadSpeedLimitKb;
	}

	/**
	 * @return A list of blocklist urls. The backend should block peers from the
	 * files returned by these urls, if supported
	 */
	public List<String> getBlocklistUrls() {
		return blocklistUrls;
	}

	public void setBlocklistUrls(List<String> blocklistUrls) {
		this.blocklistUrls = blocklistUrls;
	}
	
	
}

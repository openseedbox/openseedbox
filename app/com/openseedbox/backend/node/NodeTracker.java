package com.openseedbox.backend.node;

import com.openseedbox.backend.ITracker;

public class NodeTracker implements ITracker {
	
	private String host;
	private String announceUrl;
	private int leecherCount;
	private int seederCount;
	private int downloadCount;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getAnnounceUrl() {
		return announceUrl;
	}

	public void setAnnounceUrl(String announceUrl) {
		this.announceUrl = announceUrl;
	}

	public int getLeecherCount() {
		return leecherCount;
	}

	public void setLeecherCount(int leecherCount) {
		this.leecherCount = leecherCount;
	}

	public int getSeederCount() {
		return seederCount;
	}

	public void setSeederCount(int seederCount) {
		this.seederCount = seederCount;
	}

	public int getDownloadCount() {
		return downloadCount;
	}

	public void setDownloadCount(int downloadCount) {
		this.downloadCount = downloadCount;
	}
	
}

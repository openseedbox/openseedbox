package com.openseedbox.backend.node;

import com.openseedbox.backend.IPeer;

public class NodePeer implements IPeer {
	
	private String clientName;
	private boolean downloadingFrom;
	private boolean uploadingTo;
	private boolean encryptionEnabled;
	private long downloadRateBytes;
	private long uploadRateBytes;
	private String ipAddress;

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public boolean isDownloadingFrom() {
		return downloadingFrom;
	}

	public void setDownloadingFrom(boolean downloadingFrom) {
		this.downloadingFrom = downloadingFrom;
	}

	public boolean isUploadingTo() {
		return uploadingTo;
	}

	public void setUploadingTo(boolean uploadingTo) {
		this.uploadingTo = uploadingTo;
	}

	public boolean isEncryptionEnabled() {
		return encryptionEnabled;
	}

	public void setEncryptionEnabled(boolean encryptionEnabled) {
		this.encryptionEnabled = encryptionEnabled;
	}

	public long getDownloadRateBytes() {
		return downloadRateBytes;
	}

	public void setDownloadRateBytes(long downloadRateBytes) {
		this.downloadRateBytes = downloadRateBytes;
	}

	public long getUploadRateBytes() {
		return uploadRateBytes;
	}

	public void setUploadRateBytes(long uploadRateBytes) {
		this.uploadRateBytes = uploadRateBytes;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
}

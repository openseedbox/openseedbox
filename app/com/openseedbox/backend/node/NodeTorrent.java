package com.openseedbox.backend.node;

import com.openseedbox.backend.IFile;
import com.openseedbox.backend.IPeer;
import com.openseedbox.backend.ITorrent;
import com.openseedbox.backend.ITracker;
import com.openseedbox.backend.TorrentState;
import java.util.List;
import models.Node;
import org.apache.commons.lang.StringUtils;

public class NodeTorrent implements ITorrent {
	
	private String name;
	private boolean running;
	private double metadataPercentComplete;
	private boolean metadataDownloading;
	private double percentComplete;
	private long downloadSpeedBytes;
	private long uploadSpeedBytes;
	private String torrentHash;
	private String errorMessage;
	private long totalSizeBytes;
	private long downloadedBytes;
	private long uploadedBytes;
	private TorrentState state;
	private Node node;
	
	public NodeTorrent(Node n) {
		node = n;
	}

	public String getName() {
		return name;
	}

	public boolean isRunning() {
		return running;
	}

	public double getMetadataPercentComplete() {
		return metadataPercentComplete;
	}

	public boolean isMetadataDownloading() {
		return metadataPercentComplete != 1.0;
	}

	public double getPercentComplete() {
		return percentComplete;
	}

	public long getDownloadSpeedBytes() {
		return downloadSpeedBytes;
	}

	public long getUploadSpeedBytes() {
		return uploadSpeedBytes;
	}

	public String getTorrentHash() {
		return torrentHash;
	}

	public boolean hasErrorOccured() {
		return !StringUtils.isEmpty(getErrorMessage());
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public long getTotalSizeBytes() {
		return totalSizeBytes;
	}

	public long getDownloadedBytes() {
		return downloadedBytes;
	}

	public long getUploadedBytes() {
		return uploadedBytes;
	}

	public TorrentState getStatus() {
		return state;
	}

	private transient List<IFile> _files;
	public List<IFile> getFiles() {
		if (_files == null) {
			_files = node.getNodeBackend().getTorrentFiles(this.getTorrentHash());
		}
		return _files;
	}

	private transient List<IPeer> _peers;
	public List<IPeer> getPeers() {
		if (_peers == null) {
			_peers = node.getNodeBackend().getTorrentPeers(this.getTorrentHash());
		}
		return _peers;
	}

	private transient List<ITracker> _trackers;
	public List<ITracker> getTrackers() {
		if (_trackers == null) {
			_trackers = node.getNodeBackend().getTorrentTrackers(this.getTorrentHash());
		}
		return _trackers;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public void setMetadataPercentComplete(double metadataPercentComplete) {
		this.metadataPercentComplete = metadataPercentComplete;
	}

	public void setPercentComplete(double percentComplete) {
		this.percentComplete = percentComplete;
	}

	public void setDownloadSpeedBytes(long downloadSpeedBytes) {
		this.downloadSpeedBytes = downloadSpeedBytes;
	}

	public void setUploadSpeedBytes(long uploadSpeedBytes) {
		this.uploadSpeedBytes = uploadSpeedBytes;
	}

	public void setTorrentHash(String torrentHash) {
		this.torrentHash = torrentHash;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setTotalSizeBytes(long totalSizeBytes) {
		this.totalSizeBytes = totalSizeBytes;
	}

	public void setDownloadedBytes(long downloadedBytes) {
		this.downloadedBytes = downloadedBytes;
	}

	public void setUploadedBytes(long uploadedBytes) {
		this.uploadedBytes = uploadedBytes;
	}

	public void setStatus(TorrentState state) {
		this.state = state;
	}

	public boolean isSeeding() {
		return getStatus() == TorrentState.SEEDING;
	}

	public boolean isDownloading() {
		return getStatus() == TorrentState.DOWNLOADING;
	}

	public boolean isPaused() {
		return getStatus() == TorrentState.PAUSED;
	}
	
}

package com.openseedbox.models;

import com.openseedbox.backend.IFile;
import com.openseedbox.backend.IPeer;
import com.openseedbox.backend.ITorrent;
import com.openseedbox.backend.ITracker;
import com.openseedbox.backend.TorrentState;
import com.openseedbox.code.Util;
import com.openseedbox.gson.SerializedAccessorName;
import com.openseedbox.gson.UseAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;


@UseAccessor
@Entity
public class Torrent extends ModelBase implements ITorrent {
	
	private String torrentHash;
	private String name;	
	private double metadataPercentComplete;
	private double percentComplete;
	private long downloadSpeedBytes;
	private long uploadSpeedBytes;
	private long downloadedBytes;
	private long uploadedBytes;
	private long totalSizeBytes;
	private String zipDownloadLink;
	private String error;
	@Enumerated(EnumType.STRING)
	private TorrentState state;
	private Date createDate;
	@ManyToOne
	private Node node;
	
	public static Torrent getByHash(String hash) {
		return Torrent.<Torrent>all()
				.where()
				.eq("torrentHash", hash)
				.findUnique();
	}
	
	public static List<Torrent> getByHash(List<String> hashes) {
		if (hashes.size() > 0) {
			return Torrent.<Torrent>all()
					.where()
					.in("torrentHash", hashes)
					.findList();
		}
		return new ArrayList<Torrent>();
	}
	
	public Torrent() {
		if (this.createDate == null) {
			this.createDate = new Date();
		}
	}

	/**
	 * WTF?!
	 * @param t
	 */
	public void merge(ITorrent t) {
		if (t == null) {
			throw new IllegalArgumentException("You cant merge a null torrent!");
		}
		if (!t.getTorrentHash().equals(this.getTorrentHash())) {
			throw new IllegalArgumentException("You must merge a torrent with a matching hash!");
		}
		this.setName(t.getName());
		this.setMetadataPercentComplete(t.getMetadataPercentComplete());
		this.setPercentComplete(t.getPercentComplete());
		this.setDownloadSpeedBytes(t.getDownloadSpeedBytes());
		this.setUploadSpeedBytes(t.getUploadSpeedBytes());
		this.setDownloadedBytes(t.getDownloadedBytes());
		this.setUploadedBytes(t.getUploadedBytes());
		this.setTotalSizeBytes(t.getTotalSizeBytes());		
		this.setErrorMessage(t.getErrorMessage());
		this.setStatus(t.getStatus());	
		this.setZipDownloadLink(t.getZipDownloadLink());
	}
	
	@SerializedAccessorName("ratio")
	public double getRatio() {
		if (uploadedBytes == 0) {
			return 0;
		}
		return ((double) uploadedBytes) / ((double) downloadedBytes);
	}
	
	/* Getters and Setters */
	
	private transient List<IPeer> _peers;
	public List<IPeer> getPeers() {
		if (_peers == null) {
			_peers = this.getNode().getNodeBackend().getTorrentPeers(this.getTorrentHash());
		}
		return _peers;
	}
	
	@SerializedAccessorName("is-metadata-downloading")
	public boolean isMetadataDownloading() {
		return metadataPercentComplete != 1;
	}
 	
	@SerializedAccessorName("is-running")
	public boolean isRunning() {
		if (state == TorrentState.ERROR) {
			return (downloadSpeedBytes > 0);
		}
		return state != TorrentState.PAUSED;
	}
	
	private transient List<ITracker> _trackers;
	public List<ITracker> getTrackers() {
		if (_trackers == null) {
			_trackers = this.getNode().getNodeBackend().getTorrentTrackers(this.getTorrentHash());
		}
		return _trackers;
	}
	
	
	private transient List<IFile> _files;
	public List<IFile> getFiles() {
		if (_files == null) {
			_files = this.getNode().getNodeBackend().getTorrentFiles(this.getTorrentHash());
		}
		return _files;
	}

	@SerializedAccessorName("name")
	public String getName() {
		return Util.URLDecode(name);
	}

	@SerializedAccessorName("metadata-percent-complete")
	public double getMetadataPercentComplete() {
		return metadataPercentComplete;
	}

	@SerializedAccessorName("percent-complete")
	public double getPercentComplete() {
		return percentComplete;
	}

	@SerializedAccessorName("download-speed-bytes")
	public long getDownloadSpeedBytes() {
		return downloadSpeedBytes;
	}
	
	@SerializedAccessorName("upload-speed-bytes")
	public long getUploadSpeedBytes() {
		return uploadSpeedBytes;
	}
	
	@SerializedAccessorName("hash")	
	public String getTorrentHash() {
		return torrentHash;
	}

	@SerializedAccessorName("has-error-occured")	
	public boolean hasErrorOccured() {
		return !StringUtils.isEmpty(error);
	}

	@SerializedAccessorName("error-message")	
	public String getErrorMessage() {
		return error;
	}

	@SerializedAccessorName("total-size-bytes")	
	public long getTotalSizeBytes() {
		return totalSizeBytes;
	}

	@SerializedAccessorName("downloaded-bytes")	
	public long getDownloadedBytes() {
		return downloadedBytes;
	}

	@SerializedAccessorName("uploaded-bytes")	
	public long getUploadedBytes() {
		return uploadedBytes;
	}

	@SerializedAccessorName("status")	
	public TorrentState getStatus() {
		return state;
	}

	public void setHashString(String hashString) {
		this.torrentHash = hashString;
	}

	public void setName(String name) {
		this.name = name;
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

	public void setDownloadedBytes(long downloadedBytes) {
		this.downloadedBytes = downloadedBytes;
	}

	public void setUploadedBytes(long uploadedBytes) {
		this.uploadedBytes = uploadedBytes;
	}

	public void setTotalSizeBytes(long totalSizeBytes) {
		this.totalSizeBytes = totalSizeBytes;
	}

	public void setErrorMessage(String error) {
		this.error = error;
	}

	public void setStatus(TorrentState state) {
		this.state = state;
	}

	public Node getNode() {
		return Node.findById(node.id);
	}

	public void setNode(Node node) {
		this.node = node;
	}

	@SerializedAccessorName("is-seeding")
	public boolean isSeeding() {
		return getStatus() == TorrentState.SEEDING;
	}

	@SerializedAccessorName("is-downloading")
	public boolean isDownloading() {
		return getStatus() == TorrentState.DOWNLOADING || getDownloadSpeedBytes() > 0;
	}

	@SerializedAccessorName("is-paused")
	public boolean isPaused() {
		return getStatus() == TorrentState.PAUSED;
	}

	@SerializedAccessorName("is-complete")
	public boolean isComplete() {
		return getPercentComplete() == 1.0;
	}

	@SerializedAccessorName("zip-download-link")
	public String getZipDownloadLink() {
		return this.zipDownloadLink;
	}

	public void setZipDownloadLink(String zipDownloadLink) {
		this.zipDownloadLink = zipDownloadLink;
	}

}
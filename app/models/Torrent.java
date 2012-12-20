package models;

import com.openseedbox.backend.IFile;
import com.openseedbox.backend.IPeer;
import com.openseedbox.backend.ITorrent;
import com.openseedbox.backend.ITracker;
import com.openseedbox.backend.TorrentState;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import siena.Column;
import siena.Table;

@Table("torrent")
public class Torrent extends ModelBase implements ITorrent {
	
	@Column("torrent_hash") private String torrentHash;	
	private String name;	
	@Column("metadata_percent_complete") private double metadataPercentComplete;
	@Column("percent_complete") private double percentComplete;
	@Column("download_speed_bytes") private long downloadSpeedBytes;
	@Column("upload_speed_bytes") private long uploadSpeedBytes;
	@Column("downloaded_bytes") private long downloadedBytes;
	@Column("uploaded_bytes") private long uploadedBytes;	
	@Column("total_size_bytes") private long totalSizeBytes;
	@Column("zip_download_link") private String zipDownloadLink;
	private String error;
	private TorrentState state;
	@Column("create_date")
	private Date createDate;
	@Column("node_id")
	private Node node;
	
	public static Torrent getByHash(String hash) {
		return Torrent.all().filter("torrentHash", hash).get();
	}
	
	public static List<Torrent> getByHash(List<String> hashes) {
		return Torrent.all().filter("torrentHash IN", hashes).fetch();
	}
	
	public Torrent() {
		if (this.createDate == null) {
			this.createDate = new Date();
		}
	}
	
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
	
	private transient List<IPeer> _peers;
	public List<IPeer> getPeers() {
		if (_peers == null) {
			_peers = this.getNode().getNodeBackend().getTorrentPeers(this.getTorrentHash());
		}
		return _peers;
	}
	
	public boolean isMetadataDownloading() {
		return metadataPercentComplete != 1;
	}
 	
	public boolean isRunning() {
		return state != TorrentState.ERROR && state != TorrentState.PAUSED;
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

	public String getName() {
		return name;
	}

	public double getMetadataPercentComplete() {
		return metadataPercentComplete;
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
		return !StringUtils.isEmpty(error);
	}

	public String getErrorMessage() {
		return error;
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
		return Node.getByKey(node.id);
	}

	public void setNode(Node node) {
		this.node = node;
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

	public boolean isComplete() {
		return getPercentComplete() == 1.0;
	}

	public String getZipDownloadLink() {
		return this.zipDownloadLink;
	}

	public void setZipDownloadLink(String zipDownloadLink) {
		this.zipDownloadLink = zipDownloadLink;
	}

}
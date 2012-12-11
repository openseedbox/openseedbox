package models;

import com.openseedbox.backend.IFile;
import com.openseedbox.backend.IPeer;
import com.openseedbox.backend.ITorrent;
import com.openseedbox.backend.ITracker;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import siena.Column;
import siena.Table;

@Table("torrent")
public class Torrent extends ModelBase implements ITorrent {
	
	@Column("hash_string") private String hashString;	
	private String name;	
	@Column("metadata_percent_complete") private double metadataPercentComplete;
	@Column("percent_complete") private double percentComplete;
	@Column("download_speed_bytes") private long downloadSpeedBytes;
	@Column("upload_speed_bytes") private long uploadSpeedBytes;
	@Column("downloaded_bytes") private long downloadedBytes;
	@Column("uploaded_bytes") private long uploadedBytes;	
	@Column("total_size_bytes") private long totalSizeBytes;
	private String error;
	private TorrentState state;
	@Column("create_date")
	protected Date createDate;
	
	public List<IPeer> getPeers() {
		//WS call to get peers
		return null;
	}
	
	public boolean isMetadataDownloading() {
		return metadataPercentComplete != 1;
	}
 	
	public boolean isRunning() {
		return state != TorrentState.ERROR && state != TorrentState.PAUSED;
	}
	
	public List<ITracker> getTrackers() {
		//WS call to get trackers
		return null;
	}
	
	public List<IFile> getFiles() {
		//WS call to get files
		return null;
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
		return hashString;
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

}
package models;

import com.openseedbox.code.Util;
import com.openseedbox.backend.IFile;
import com.openseedbox.backend.IPeer;
import com.openseedbox.backend.ITorrent;
import com.openseedbox.backend.ITracker;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import play.data.validation.Max;
import play.db.jpa.Model;

@Entity
@Table(name="torrent")
public class Torrent extends Model {
	
	@Max(40)
	protected String hashString;
	
	@Column(name="name")
	protected String name;
	
	@Column(name="user_id")
	protected User user;
	
	@Embedded
	protected List<String> groups = new ArrayList<String>();
	
	@Column(name="create_date_utc")
	@Temporal(TemporalType.TIMESTAMP)
	protected Date createDateUTC = new Date();
	
	private transient ITorrent _torrent;
	
	public String getStatusString() {
		return _torrent.getStatus().toString();
		/*
		switch(_torrent.status) {
			case 0:
				return "Paused";
			case 1:
				return "Waiting to check";
			case 2:
				return "Checking";
			case 3:
				return "Waiting to Download";
			case 4:
				return "Downloading";
			case 5:
				return "Waiting to Seed";
			case 6:
				return "Seeding";
		}
		return "Unknown status: " + _torrent.status;*/
	}
	
	/**
	 * Gets the ids of all the files in the torrent.
	 * This is mainly used for applying something (eg, wanted/priority etc) to
	 * all the files in the torrent
	 * @return The list of ids
	 */
	public List<String> getFileIds() {
		List<String> ret = new ArrayList<String>();
		for (int x = 0; x < _torrent.getFiles().size(); x++) {
			ret.add(String.valueOf(x));
		}
		return ret;
	}
	
	public List<String> getGroups() {
		return this.groups;
	}
	
	public String getHashString() {
		return this.hashString;
	}
	
	public String getPercentDone() {
		return String.format("%.2f", ((double) _torrent.getPercentComplete() * 100));
	}
	
	public int getSeederCount() {
		int total = 0;
		for (ITracker tr : _torrent.getTrackers()) {
			total += tr.getSeederCount();
		}
		return total;
	}
	
	public int getLeecherCount() {
		int total = 0;
		for (ITracker tr : _torrent.getTrackers()) {
			total += tr.getLeecherCount();
		}
		return total;	
	}
	
	public int getPeersDownloadingFromCount() {
		int total = 0;
		for (IPeer peer : _torrent.getPeers()) {
			if (peer.isDownloadingFrom()) {
				total++;
			}
		}
		return total;
	}
	
	public int getPeersUploadingToCount() {
		int total = 0;
		for (IPeer peer : _torrent.getPeers()) {
			if (peer.isUploadingTo()) {
				total++;
			}
		}
		return total;		
	}
	
	public int getPeerCount() {
		return _torrent.getPeers().size();
	}
	
	public List<IPeer> getPeers() {
		return _torrent.getPeers();
	}
	
	public String getTotalSize() {
		return Util.getBestRate(_torrent.getTotalSizeBytes());
	}
	
	public String getDownloadAmount() {
		return Util.getBestRate(_torrent.getDownloadedBytes());
	}
	
	public String getUploadAmount() {
		return Util.getBestRate(_torrent.getUploadedBytes());
	}
	
	public String getDownloadRate() {
		return Util.getBestRate(_torrent.getDownloadSpeedBytes());
	}
	
	public String getUploadRate() {
		return Util.getBestRate(_torrent.getUploadSpeedBytes());
	}
	
	public boolean isMetadataDownloading() {
		return _torrent.getMetadataPercentComplete() != 1;
	}
 	
	public boolean isRunning() {
		return (_torrent.isRunning());
	}
	
	public String getMetadataPercentComplete() {
		return String.format("%.2f", (double) (_torrent.getMetadataPercentComplete() * 100));
	}
	
	public void setBackendTorrent(ITorrent t) {
		this._torrent = t;
	}
	
	public ITorrent getBackendTorrent() {
		return this._torrent;
	}
	
	public List<ITracker> getTrackers() {
		return this._torrent.getTrackers();
	}
	
	public List<IFile> getFiles() {
		return this._torrent.getFiles();
	}

}

package models;

import code.Util;
import code.transmission.TransmissionTorrent;
import code.transmission.TransmissionTorrent.TransmissionFile;
import code.transmission.TransmissionTorrent.TransmissionPeer;
import code.transmission.TransmissionTorrent.TransmissionTrackerStats;
import code.transmission.TransmissionTorrent.TreeNode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import play.modules.siena.EnhancedModel;
import siena.*;
import siena.embed.Embedded;
import siena.embed.EmbeddedMap;

@Table("torrent")
public class Torrent extends EnhancedModel {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Max(40)
	@Unique("hash_user_unique")
	public String hashString;
	
	@Column("name")
	public String name;
	
	@Column("user_id")
	@Unique("hash_user_unique")
	public User user;
	
	@Embedded
	public List<TorrentGroup> groups = new ArrayList<TorrentGroup>();
	
	@Column("create_date_utc")
	@DateTime
	public Date createDateUTC = new Date();
	
	private transient TransmissionTorrent _torrent;
	
	public String getStatusString() {
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
		return "Unknown status: " + _torrent.status;
	}
	
	public String getSubStatusString() {
		if (_torrent.metadataPercentComplete < 1) {
			return String.format("Retrieving torrent from magnet link");
		}
		return "";
	}
	
	/**
	 * Gets the ids of all the files in the torrent.
	 * This is mainly used for applying something (eg, wanted/priority etc) to
	 * all the files in the torrent
	 * @return The list of ids
	 */
	public List<String> getFileIds() {
		List<String> ret = new ArrayList<String>();
		for (int x = 0; x < _torrent.files.size(); x++) {
			ret.add(String.valueOf(x));
		}
		return ret;
	}
	
	public int getStatus() {
		return _torrent.status;
	}
	
	public String getPercentDone() {
		return String.format("%.2f", ((double) _torrent.percentDone * 100));
	}
	
	public int getSeederCount() {
		int total = 0;
		for (TransmissionTrackerStats tracker : _torrent.trackerStats) {
			total += tracker.seederCount;
		}
		return total;
	}
	
	public int getLeecherCount() {
		int total = 0;
		for (TransmissionTrackerStats tracker : _torrent.trackerStats) {
			total += tracker.leecherCount;
		}
		return total;		
	}
	
	public int getPeersDownloadingFromCount() {
		int total = 0;
		for (TransmissionPeer peer : _torrent.peers) {
			if (peer.isDownloadingFrom) {
				total++;
			}
		}
		return total;
	}
	
	public int getPeersUploadingToCount() {
		int total = 0;
		for (TransmissionPeer peer : _torrent.peers) {
			if (peer.isUploadingTo) {
				total++;
			}
		}
		return total;		
	}
	
	public int getPeerCount() {
		return _torrent.peers.size();
	}
	
	public List<TransmissionPeer> getPeers() {
		return _torrent.peers;
	}
	
	public String getTotalSize() {
		return Util.getBestRate(_torrent.totalSize);
	}
	
	public String getDownloadAmount() {
		return Util.getBestRate(_torrent.downloadedEver);
	}
	
	public String getUploadAmount() {
		return Util.getBestRate(_torrent.uploadedEver);
	}
	
	public String getDownloadRate() {
		return Util.getBestRate(_torrent.rateDownload);
	}
	
	public String getUploadRate() {
		return Util.getBestRate(_torrent.rateUpload);
	}
	
	public boolean isMetadataDownloading() {
		return _torrent.metadataPercentComplete != 1;
	}
 	
	public Boolean isRunning() {
		return (_torrent.status != 0 && _torrent.status != 16);
	}
	
	public String getMetadataPercentComplete() {
		return String.format("%.2f", (double) (_torrent.metadataPercentComplete * 100));
	}
	
	public void setTransmissionTorrent(TransmissionTorrent t) {
		this._torrent = t;
	}
	
	public TransmissionTorrent getTransmissionTorrent() {
		return this._torrent;
	}
	
	public List<TransmissionTrackerStats> getTrackers() {
		return this._torrent.trackerStats;
	}
	
	public List<TransmissionFile> getFiles() {
		return this._torrent.files;
	}
	
	public List<TreeNode> getFilesAsTree() {
		return this._torrent.getFilesAsTree();
	}

	@EmbeddedMap
	public static class TorrentGroup {
		@Column("name")
		public String name;
		
		public TorrentGroup(String name) {
			this.name = name;
		}
		
		public TorrentGroup() {}
		
		public String getHtmlName() {
			return name.replace(" ", "-");
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TorrentGroup) {
				return (((TorrentGroup) obj).name.equals(this.name));
			}
			return super.equals(obj);
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}	

}

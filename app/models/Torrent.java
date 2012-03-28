package models;

import code.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import play.modules.siena.EnhancedModel;
import siena.*;
import siena.embed.Embedded;
import siena.embed.EmbeddedMap;

@Table("torrent")
public class Torrent extends EnhancedModel {

	@Id(Generator.NONE)
	@Max(40)
	public String hashString;
	
	@Column("name")
	public String name;
	
	@Column("percent_done")
	public double percentDone;
	
	@Column("rate_download")
	public int rateDownload;
	
	@Column("rate_upload")
	public int rateUpload;
	
	@Column("errorString")
	public String errorString;
	
	@Column("total_size")
	public long totalSize;
	
	@Column("downloaded_ever")
	public long downloadedEver;
	
	@Column("uploaded_ever")
	public long uploadedEver;
	
	@Column("status")
	public int status;
	
	@Column("metadata_percent_complete")
	public double metadataPercentComplete;
	
	@Column("download_dir")
	public String downloadDir;
	
	public User user;
	
	@Embedded
	public List<TorrentFile> files = new ArrayList<>();
	
	@Embedded
	public List<TorrentGroup> groups = new ArrayList<>();
	
	@Column("create_date_utc")
	public Date createDateUTC = new Date();
	
	public String getStatusString() {
		switch(status) {
			case 1:
				return "Waiting to check";
			case 2:
				return "Checking";
			case 4:
				return "Downloading";
			case 8:
				return "Seeding";
			case 16:
				return "Finished";
		}
		return "Paused";
	}
	
	public String getSubStatusString() {
		if (metadataPercentComplete < 1) {
			return String.format("Retrieving torrent from magnet link");
		}
		return "";
	}
	
	public String getTotalSizeMb() {
		return Util.getRateGb(totalSize);
	}
	
	public String getTotalSizeGb() {
		return Util.getRateGb(totalSize);
	}
	
	public String getDownloadedMb() {
		return Util.getRateMb(downloadedEver);
	}
	
	public String getUploadedMb() {
		return Util.getRateMb(uploadedEver);
	}
	
	public String getRateDownloadKb() {
		return Util.getRateKb(rateDownload);
	}
	
	public String getRateUploadKb() {
		return Util.getRateKb(rateUpload);
	}
 	
	public Boolean isRunning() {
		return (status != 0 && status != 16);
	}
	
	public List<String> getFileIds() {
		List<String> ret = new ArrayList<>();
		for(TorrentFile tf : files) {
			ret.add(String.valueOf(tf.transmissionId));
		}
		return ret;
	}
	
	public String getNiceMetadataPercentComplete() {
		return String.format("%.2f", (double) (metadataPercentComplete * 100));
	}
	
	public TorrentInfo getTorrentInfo() {
		TorrentInfo ti = new TorrentInfo();
		ti.files = this.files;
		return ti;
	}
	
	public static Torrent fromJson(String json) {
		Gson g = new GsonBuilder().create();
		Torrent t = g.fromJson(json, Torrent.class);
		Torrent inDb = Torrent.getByKey(t.hashString);
		t.groups = inDb.groups;
		TorrentInfo ti = TorrentInfo.fromJson(json);
		t.files = ti.files;
		return t;
	}
	
	public static Torrent getByKey(String id) {
		Torrent t = Torrent.getByKey(Torrent.class, id);
		return t;
	}
	
	public void merge(Torrent t) {
		this.hashString = t.hashString;
		this.name = t.name;
		this.percentDone = t.percentDone;
		this.rateDownload = t.rateDownload;
		this.rateUpload = t.rateUpload;
		this.errorString = t.errorString;
		this.totalSize = t.totalSize;
		this.downloadedEver = t.downloadedEver;
		this.uploadedEver = t.uploadedEver;
		this.status = t.status;
		this.metadataPercentComplete = t.metadataPercentComplete;
		this.downloadDir = t.downloadDir;
		this.files = t.files;
		this.groups = t.groups;
		this.createDateUTC = t.createDateUTC;	
		if (t.user != null) {
			this.user = t.user;
		}
	}

	@Override
	public String toString() {
		return name;
	}
	
	@EmbeddedMap
	public static class TorrentFile {
		@Column("name")
		public String name;
		@Column("length")
		public long length;
		@Column("bytes_completed")
		public long bytesCompleted;
		@Column("priority")
		public int priority;
		@Column("wanted")
		public boolean wanted;
		@SerializedName("id")
		public int transmissionId;
		
		public Boolean isFinishedDownloading() {
			return (bytesCompleted == length);
		}
		
		public String getPercentComplete() {
			double percent = ((double) bytesCompleted / length) * 100;
			return String.format("%.2f", percent);
		}
	}
	
	@EmbeddedMap
	public static class TorrentGroup {
		@Column("name")
		public String name;
		
		public TorrentGroup(String name) {
			this.name = name;
		}
		
		public TorrentGroup() {}

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

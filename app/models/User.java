package models;

import code.MessageException;
import code.Util;
import java.util.ArrayList;
import java.util.List;
import models.Torrent.TorrentGroup;
import play.data.validation.Email;
import play.modules.siena.EnhancedModel;
import siena.*;

@Table("user")
public class User extends EnhancedModel {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Email
	@Column("email_address")
	public String emailAddress;
	@Column("password")
	public String password;
	@Column("oauth_id")
	public String oauthId;
	@Column("is_admin")
	public Boolean isAdmin;
	
	@Column("max_diskspace_gb")
	public int maxDiskspaceGB;
	
	private Node node;
	
	public List<Torrent> getTorrents() {
		return Torrent.all().filter("user", this).fetch();
	}
	
	public Node getNode() {
		return Model.all(Node.class).getByKey(node.id);
	}
	
	
	public List<Torrent> getTorrentsWithStatus(int status) {
		List<Torrent> ret = new ArrayList<>();
		for (Torrent t : this.getTorrents()) {
			if (t.status == status) {
				ret.add(t);
			}
		}
		return ret;
	}
	
	public List<Torrent> getTorrentsWithGroup(String group) {
		List<Torrent> ret = new ArrayList<>();
		for (Torrent t : this.getTorrents()) {
			if (t.groups.contains(new Torrent.TorrentGroup(group))) {
				ret.add(t);
			}
		}
		return ret;
	}	
	
	public List<TorrentGroup> getTorrentGroups() throws MessageException {
		List<TorrentGroup> ret = new ArrayList<>();
		ret.add(new TorrentGroup("All"));
		ret.add(new TorrentGroup("Downloading"));
		ret.add(new TorrentGroup("Seeding"));
		ret.add(new TorrentGroup("Paused"));		
		List<Torrent> torrents = this.getTorrents();
		for (Torrent t : torrents) {
			for (TorrentGroup group : t.groups) {
				if (!ret.contains(group)) {
					ret.add(group);
				}
			}
		}
		return ret;
	}
	
	public UserStats getUserStats() throws MessageException {
		List<Torrent> t = this.getTorrents();
		long totalSize = 0;
		long totalRateUpload = 0;
		long totalRateDownload = 0;
		for(Torrent to : t) {
			totalSize += to.totalSize;
			totalRateUpload += to.rateUpload;
			totalRateDownload += to.rateDownload;
		}
		UserStats us = new UserStats();
		us.maxSpaceGb = String.format("%.2f", (double)this.maxDiskspaceGB);
		us.usedSpaceGb = Util.getRateGb(totalSize);
		us.rateDownloadKb = Util.getRateKb(totalRateDownload);
		us.rateUploadKb = Util.getRateKb(totalRateUpload);		
		return us;
	}	

	@Override
	public String toString() {
		return emailAddress;
	}
	
	public class UserStats {
		public String maxSpaceGb;
		public String usedSpaceGb;
		public String rateDownloadKb;
		public String rateUploadKb;
	}
	
}

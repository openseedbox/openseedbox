package models;

import code.MessageException;
import code.Transmission;
import code.Util;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.Torrent.TorrentGroup;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Email;
import play.modules.siena.EnhancedModel;
import siena.*;
import siena.embed.Embedded;

@Table("user")
public class User extends EnhancedModel {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Email @Unique("email_address")
	@Column("email_address")
	public String emailAddress;
	
	@Column("open_id")
	public String openId;
	
	@Column("is_admin")
	public Boolean isAdmin;
	
	@Column("avatar_url")
	public String avatarUrl;
	
	@Column("display_name")
	public String displayName;
	
	@Column("last_access")
	@DateTime
	public Date lastAccess;
	
	@Column("primary_account_id")
	public Account primaryAccount;
	
	@Embedded @Column("allowed_account_ids")
	public List<Integer> allowedAccounts; 
	
	public List<Torrent> getTorrents() {
		return Torrent.all().filter("user", this).fetch();
	}
	
	public Node getNode() {
		Account a = getPrimaryAccount();
		if (a != null) {
			return a.getNode();
		}
		return null;
	}
	
	public Transmission getTransmission() {
		Account a = getPrimaryAccount();
		if (a != null) {
			return a.getTransmission();
		}
		return null;		
	}
	
	public Account getPrimaryAccount() {
		if (this.primaryAccount != null) {
			return Account.getByKey(this.primaryAccount.id);
		}
		return null;
	}
	
	public Boolean hasPlan() {
		Account a = getPrimaryAccount();
		if (a != null) {
			return (a.plan != null);
		}
		return false;
	}
	
	public Plan getPlan() {
		Account a = getPrimaryAccount();
		if (a != null) {
			return a.getPlan();
		}
		return null;
	}
	
	public void setPlan(Plan p) throws MessageException {
		Account a = getPrimaryAccount();
		if (a != null) {
			a.plan = p;
			a.save();
		} else {
			throw new MessageException("User " + this.emailAddress + " isnt associated with an account!");
		}
	}
	
	public List<Torrent> getTorrentsWithStatus(int status) {
		List<Torrent> ret = new ArrayList<Torrent>();
		for (Torrent t : this.getTorrents()) {
			if (t.status == status) {
				ret.add(t);
			}
		}
		return ret;
	}
	
	public List<Torrent> getTorrentsWithGroup(String group) {
		List<Torrent> ret = new ArrayList<Torrent>();
		for (Torrent t : this.getTorrents()) {
			if (t.groups.contains(new Torrent.TorrentGroup(group))) {
				ret.add(t);
			}
		}
		return ret;
	}	
	
	public List<TorrentGroup> getTorrentGroups() throws MessageException {
		List<TorrentGroup> ret = new ArrayList<TorrentGroup>();
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
	
	public String getDisplayName() {
		return StringUtils.isEmpty(this.displayName) ? this.emailAddress : this.displayName;
	}
	
	/*
	
	public List<InvitedUser> getInvitedUsers() {
		return InvitedUser.all().filter("invitingUser", this).fetch();
	}
	
	public List<InvitedUser> getPendingInvites() {
		return InvitedUser.all().filter("emailAddress", this.emailAddress)
				.filter("accepted", false).fetch();	
	}
	
	public List<InvitedUser> getSharedAccounts() {
		return InvitedUser.all().filter("actualUser", this)
				.filter("accepted", true).fetch();
	}*/
	
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

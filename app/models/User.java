package models;

import code.MessageException;
import code.Util;
import code.transmission.Transmission;
import code.transmission.TransmissionTorrent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import models.Torrent.TorrentGroup;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Email;
import play.modules.siena.EnhancedModel;
import siena.*;

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
	
	@Column("time_zone")
	public String timeZone;
	
	public Node getNode() {
		Account a = getPrimaryAccount();
		if (a != null) {
			return a.getNode();
		}
		return null;
	}	
	
	public List<Invitation> getInvitations() {
		return Invitation.all().filter("invitingUser", this).order("invitationDate").fetch();
	}
	
	public List<Account> getAvailableAccounts() {
		List<Account> ret = new ArrayList<Account>();
		
		//add the users account since it needs to be in the list of available accounts so the user can switch back to it
		ret.add(this.getPrimaryAccount());
		
		//available accounts are all the invitations where the emailAddress is the same as the current user
		List<Invitation> invitations = Invitation.all().filter("emailAddress", this.emailAddress).fetch();
		for (Invitation i : invitations) {
			User u = i.getInvitingUser();
			ret.add(u.getPrimaryAccount());
		}
		return ret;
	}
	
	public Transmission getTransmission() {
		Account a = getPrimaryAccount();
		if (a != null) {
			return a.getTransmission();
		}
		return null;		
	}
	
	private transient Account _primaryAccount;
	public Account getPrimaryAccount() {
		if (this.primaryAccount != null) {
			if (_primaryAccount == null) {
				_primaryAccount = Account.getByKey(this.primaryAccount.id);
			}
			return _primaryAccount;
		}
		return null;
	}
	
	public boolean hasPlan() {
		Account a = getPrimaryAccount();
		if (a != null) {
			return (a.plan != null);
		}
		return false;
	}
	
	public boolean hasPlanAndNode() {
		return (this.getNode() != null && hasPlan());
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
	
	public Torrent addTorrent(File f) throws MessageException {
		TransmissionTorrent tt = this.getTransmission().addTorrent(f);
		return newTorrent(tt);		
	}
	
	public Torrent addTorrent(String urlOrMagnet) throws MessageException {
		TransmissionTorrent tt = this.getTransmission().addTorrent(urlOrMagnet);
		return newTorrent(tt);
	}
	
	private Torrent newTorrent(TransmissionTorrent tt) {
		Torrent t = new Torrent();
		t.hashString = tt.hashString;
		t.name = tt.name;
		t.user = this;
		t.insert();
		t.setTransmissionTorrent(tt);
		return t;		
	}
	
	public Torrent getTorrent(String hashString) throws MessageException {
		return getTorrent(hashString, false);		
	}
	
	public Torrent getTorrent(String hashString, boolean hitTransmission) throws MessageException {
		Torrent t = Torrent.all().filter("hashString", hashString)
				.filter("user", this).get();		
		if (hitTransmission) {
			TransmissionTorrent to = this.getTransmission().getTorrent(hashString);
			t.setTransmissionTorrent(to);
			calculateUserStats(Arrays.asList(new Torrent[] { t }));
		}
		return t;
	}	
	
	public List<Torrent> getTorrents() throws MessageException {
		List<TransmissionTorrent> trans = this.getTransmission().getAllTorrents();
		List<String> hashes = getHashStringList(trans);
		List<Torrent> ret;
		//need this check or siena fails to do an IN clause with no values
		if (hashes.size() > 0) {
			ret = Torrent.all()
					.filter("hashString IN", hashes)
					.filter("user", this)
					.fetch(); //so you dont have to do a db query per torrent
			for (Torrent t : ret) {
				t.setTransmissionTorrent(getMatchingTransmissionTorrent(trans, t.hashString));
			}
			return ret;
		} else {
			ret = new ArrayList<Torrent>();
		}
		calculateUserStats(ret);
		return ret;
	}
	
	private List<String> getHashStringList(List<TransmissionTorrent> t) {
		List<String> ret = new ArrayList<String>();
		for (TransmissionTorrent to : t) {
			ret.add(to.hashString);
		}
		return ret;
	}
	
	private TransmissionTorrent getMatchingTransmissionTorrent(List<TransmissionTorrent> t, String hash) {
		for (TransmissionTorrent tr : t) {
			if (tr.hashString.equals(hash)) {
				return tr;
			}
		}
		return null;
	}
	
	public List<Torrent> getTorrents(String group) throws MessageException {
		List<Torrent> ret = new ArrayList<Torrent>();
		List<Torrent> all = getTorrents();
		for (Torrent t : all) {
			int status = t.getStatus();		
			if (group.equals("All")) {
				ret.add(t);
			} else if (group.equals("Downloading")) {
				if (status == 4 || status == 3) {
					ret.add(t);
				}
			} else if (group.equals("Seeding")) {
				if (status == 5 || status == 6) {
					ret.add(t);
				}
			} else if (group.equals("Paused")) {
				if (status == 0) {
					ret.add(t);
				}
			} else if (t.groups.contains(new TorrentGroup(group))) {
				//Logger.info("Torrent %s contains group %s", t.name, group);
				ret.add(t);
			}
		}
		calculateUserStats(all); 
		return ret;
	}	
	
	public List<TorrentGroup> getTorrentGroups() {
		List<TorrentGroup> ret = new ArrayList<TorrentGroup>();
		ret.add(new TorrentGroup("All"));
		ret.add(new TorrentGroup("Downloading"));
		ret.add(new TorrentGroup("Seeding"));
		ret.add(new TorrentGroup("Paused"));		
		//dont use getTorrents() because we just need the groups and want to avoid hitting transmission-daemon
		List<Torrent> torrents = Torrent.all().filter("user", this).fetch();
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
	
	private void calculateUserStats(List<Torrent> torrents) throws MessageException {
		long totalSize = 0;
		long totalRateUpload = 0;
		long totalRateDownload = 0;
		for(Torrent to : torrents) {
			totalSize += to.getTransmissionTorrent().totalSize;
			totalRateUpload += to.getTransmissionTorrent().rateUpload;
			totalRateDownload += to.getTransmissionTorrent().rateDownload;
		}
		UserStats us = new UserStats();
		us.maxSpaceGb = String.format("%.2f", (double) getPlan().maxDiskspaceGb);
		us.usedSpaceGb = Util.getRateGb(totalSize);
		us.rateDownloadKb = Util.getRateKb(totalRateDownload);
		us.rateUploadKb = Util.getRateKb(totalRateUpload);		
		_userStats = us;	
	}
	
	private transient UserStats _userStats;
	public UserStats getUserStats() {
		if (!hasPlanAndNode()) { return null; }
		if (_userStats == null) {
			try {
				calculateUserStats(getTorrents());
			} catch (MessageException ex) {
				//do nothing
			}
		}
		return _userStats;
	}	
	
	public class UserStats {
		public String maxSpaceGb;
		public String usedSpaceGb;
		public String rateDownloadKb;
		public String rateUploadKb;
	}
	
}

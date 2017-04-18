package com.openseedbox.models;

import com.openseedbox.code.Util;
import com.openseedbox.gson.SerializedAccessorName;
import com.openseedbox.gson.UseAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Email;
import play.data.validation.Required;
import siena.Column;
import siena.Table;
import siena.Unique;
import siena.embed.Embedded;

@Table("user")
public class User extends ModelBase {
	
	@Email @Required @Column("email_address")
	@Unique("email_address_unique") private String emailAddress;
	@Column("open_id") private String openId;	
	@Column("is_admin") private boolean admin;	
	@Column("avatar_url") private String avatarUrl;	
	@Required @Column("display_name") private String displayName;	
	@Column("last_access") private Date lastAccess;				
	@Column("api_key") private String apiKey;
	@Column("plan_id") private Plan plan;
	@Embedded private List<String> groups;
	@Column("node_id") private Node dedicatedNode;
	
	public static final transient String TORRENT_GROUP_UNGROUPED = "Ungrouped";
	
	public static User findByApiKey(String apiKey) {		
		return User.all().filter("apiKey", apiKey).get();
	}
	
	public static User findByOpenId(String openId) {
		return User.all().filter("openId", openId).get();
	}
	
	public static User findByEmailAddress(String emailAddress) {
		return User.all().filter("emailAddress", emailAddress).get();
	}	
	
	public boolean hasExceededLimits() {
		Plan p = getPlan();
		if (p == null || p.getMaxDiskspaceGb() == -1) {
			return false;
		}
		return (getUsedSpaceBytes() > p.getMaxDiskspaceBytes());			
	}
	
	public List<UserTorrent> getRunningTorrents() {
		return UserTorrent.all().filter("user", this).filter("paused", false).fetch();
	}
	
	public void generateApiKey() {
		String salt = Play.configuration.getProperty("application.secret", "salt value");
		String key = this.emailAddress + salt;
		this.apiKey = DigestUtils.md5Hex(key);
		this.save();
	}
	
	public long getUsedSpaceBytes() {	
		List<UserTorrent> ut = getTorrents();
		long sum = 0;
		//TODO: make less retardedly inefficient
		for (UserTorrent u : ut) {
			sum += u.getTorrent().getTotalSizeBytes();
		}
		return sum;
	}
	
	public String getUsedSpace() {
		return Util.getBestRate(getUsedSpaceBytes());
	}
	
	public List<UserTorrent> getTorrents() {
		return getTorrentsInGroup(null);
	}
	
	public List<UserTorrent> getTorrentsInGroup(String group) {
		List<UserTorrent> ut;
		if (group == null) {
			ut = UserTorrent.getByUser(this);
		} else if (group.equals(User.TORRENT_GROUP_UNGROUPED)) {
			ut = UserTorrent.getByUserAndGroup(this, null);
			ut.addAll(UserTorrent.getByUserAndGroup(this, User.TORRENT_GROUP_UNGROUPED));
		} else {
			ut = UserTorrent.getByUserAndGroup(this, group);
		}
		if (ut.isEmpty()) {
			return ut;
		}
		
		//Batch-load Torrents from the database to save a database call per torrent
		List<String> hashes = new ArrayList<String>();
		for (UserTorrent u : ut) {
			hashes.add(u.getTorrentHash());
		}
		List<Torrent> all = Torrent.getByHash(hashes);
		for (UserTorrent u : ut) {
			for (Torrent t : all) {
				if (u.getTorrentHash().equals(t.getTorrentHash())) {
					u.setTorrent(t);
				}
			}
		}
		return ut;
	}
	
	public boolean hasPlan() {
		return plan != null;
	}
	
	public List<Invoice> getUnpaidInvoices() {		
		return Invoice.getUnpaidForUser(this);
	}
	
	public List<Invoice> getPaidInvoices() {		
		return Invoice.getPaidForUser(this);
	}
	
	public void addTorrentGroup(String groupName) {
		List<String> tgroups = getGroups();
		if (!tgroups.contains(groupName)) {
			tgroups.add(groupName);
			setGroups(tgroups);
			save();
		}		
	}
	
	public void removeTorrentGroup(String groupName) {
		List<String> tgroups = getGroups();
		tgroups.remove(groupName);
		setGroups(tgroups);
		save();		
	}
	
	public UserStats getUserStats() {
		int available = getPlan().getMaxDiskspaceGb();
		long used = 0l;
		long down = 0l;
		long up = 0l;
		List<UserTorrent> torrents = getTorrents();
		for (UserTorrent ut : torrents) {
			used += ut.getTorrent().getTotalSizeBytes();
			down += ut.getTorrent().getDownloadSpeedBytes();
			up += ut.getTorrent().getUploadSpeedBytes();
		}
		return new UserStats(available, used, down, up);
	}

	/* Getters and Setters */
	public String getDisplayName() {
		return StringUtils.isEmpty(this.displayName) ? this.emailAddress : this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean isAdmin) {
		this.admin = isAdmin;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public Date getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(Date lastAccess) {
		this.lastAccess = lastAccess;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public Plan getPlan() {
		if (plan != null) {
			plan.get();
		}
		return plan;
	}

	public void setPlan(Plan plan) {
		this.plan = plan;
	}

	public List<String> getGroups() {		
		if (groups != null) {
			if (!groups.contains("Ungrouped")) {
				Collections.reverse(groups);
				groups.add("Ungrouped");
				Collections.reverse(groups);
			}
			return groups;			
		}
		return new ArrayList<String>(Arrays.asList("Ungrouped"));
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}
	
	public boolean hasDedicatedNode() {
		return dedicatedNode != null;
	}

	public Node getDedicatedNode() {
		if (dedicatedNode != null) {
			dedicatedNode.get();
		}
		return dedicatedNode;
	}

	public void setDedicatedNode(Node dedicatedNode) {
		this.dedicatedNode = dedicatedNode;
	}
	
	/* End Getters and Setters */
	
	@UseAccessor
	public class UserStats {
		
		private int availableSpaceGb;
		private long usedSpaceBytes;
		private long totalDownloadSpeedBytes;
		private long totalUploadSpeedBytes;
		
		public UserStats(int availableSpaceGb, long usedSpaceBytes, long totalDownloadSpeedBytes, long totalUploadSpeedBytes) {
			this.availableSpaceGb = availableSpaceGb;
			this.usedSpaceBytes = usedSpaceBytes;
			this.totalDownloadSpeedBytes = totalDownloadSpeedBytes;
			this.totalUploadSpeedBytes = totalUploadSpeedBytes;
		}
		
		@SerializedAccessorName("available-space-gb")
		public String getAvailableSpaceGb() {
			if (availableSpaceGb != -1) {
				return "" + availableSpaceGb + " GB";
			}
			return "Unlimited GB";
		}
		
		@SerializedAccessorName("used-space-gb")
		public String getUsedSpaceGb() {
			return Util.getBestRate(usedSpaceBytes);
		}
		
		@SerializedAccessorName("percent-used")
		public String getPercentUsed() {
			if (availableSpaceGb != -1 && usedSpaceBytes > 0) {
				long bytes = getPlan().getMaxDiskspaceBytes();
				String asPercent = Util.formatPercentage(((double) usedSpaceBytes / (double) bytes) * 100);
				return asPercent + "%";
			}
			return "";
		}
		
		@SerializedAccessorName("total-download-rate")
		public String getTotalDownloadRate() {
			return Util.getBestRate(totalDownloadSpeedBytes);
		}
		
		@SerializedAccessorName("total-upload-rate")
		public String getTotalUploadRate() {
			return Util.getBestRate(totalUploadSpeedBytes);
		}		
	}
	
}

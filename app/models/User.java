package models;

import com.openseedbox.code.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Email;
import play.data.validation.Max;
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
	@Column("is_admin") private boolean isAdmin;	
	@Column("avatar_url") private String avatarUrl;	
	@Required @Column("display_name") private String displayName;	
	@Column("last_access") private Date lastAccess;				
	@Max(32) @Column("api_key") private String apiKey;
	@Column("plan_id") private Plan plan;
	@Embedded private List<String> groups;
	
	public void generateApiKey() {
		String salt = Play.configuration.getProperty("application.secret", "salt value");
		String key = this.emailAddress + salt;
		this.apiKey = DigestUtils.md5Hex(key);
		this.save();
	}
	
	public long getUsedSpaceBytes() {		
		//TODO: this properly
		long sum = 0;
		return sum;
	}
	
	public String getUsedSpace() {
		return Util.getBestRate(getUsedSpaceBytes());
	}
	
	public List<UserTorrent> getTorrents() {
		return getTorrentsInGroup("All");
	}
	
	public List<UserTorrent> getTorrentsInGroup(String group) {
		List<UserTorrent> ut;
		if (group.equals("All")) {
			ut = UserTorrent.getByUser(this);
		} else {
			ut = UserTorrent.getByUserAndGroup(this, group);
		}
		if (ut.isEmpty()) {
			return ut;
		}
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
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
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
			if (!groups.contains("All")) {
				Collections.reverse(groups);
				groups.add("All");
				Collections.reverse(groups);
			}
			return groups;			
		}
		return Arrays.asList(new String[] { "All" });
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}
	
	/*
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
	}*/
	/*
	private transient UserStats _userStats;
	public UserStats getUserStats() {
		return null;/*
		if (!hasPlanAndNode()) { return null; }
		if (_userStats == null) {
			try {
				calculateUserStats(getTorrents());
			} catch (MessageException ex) {
				//do nothing
			}
		}
		return _userStats;*//*
	}	
	
	public List<UserMessage> getUnreadMessages() {
		return null;/*
		return UserMessage.all().filter("user", this).filter("dismissDateUtc", null).fetch();*//*
	}
	
	public List<UserMessage> getAllMessages() {
		return null;/*
		return UserMessage.all().filter("user", this).fetch();*//*
	}
	
	public UserMessage addUserMessage(String heading, String message) {
		return addUserMessage(heading, message, UserMessage.State.MESSAGE, UserMessage.Type.GENERAL);
	}
	
	public UserMessage addUserMessage(String heading, String message, UserMessage.Type type) {
		return addUserMessage(heading, message, UserMessage.State.MESSAGE, type);
	}	
	
	public UserMessage addUserErrorMessage(String heading, String message) {
		return addUserMessage(heading, message, UserMessage.State.ERROR, UserMessage.Type.SWITCHPLAN);
	}	
	
	public UserMessage addUserMessage(String heading, String message, UserMessage.State state, UserMessage.Type type) {
		UserMessage um = new UserMessage();
		um.setHeading(heading);
		um.setMessage(message);
		um.setState(state);
		um.setType(type);
		um.setCreateDateUtc(new Date());
		um.setUser(this);
		return um.save();
	}
	
	public void dismissUserMessagesOfType(UserMessage.Type type) {
		List<UserMessage> um = getUserMessagesOfType(type);
		for (UserMessage u : um) {
			u.setDismissDateUtc(new Date());
			u.save();
		}
	}
	
	public List<UserMessage> getUserMessagesOfType(UserMessage.Type type) {
		return UserMessage.find("type = ? AND dismissDateUtc IS NULL", type).fetch();
	}
	
	public List<Invoice> getUnpaidInvoices() {
		return null;/*
		return Invoice.all()
				.filter("account", this.getPrimaryAccount())
				.filter("paymentDateUtc", null).fetch();*//*
	}
	
	public List<Invoice> getPaidInvoices() {
		return null;/*
		return Invoice.all()
				.filter("account", this.getPrimaryAccount())
				.filter("paymentDateUtc !=", null)
				.order("-paymentDateUtc").fetch();*/	/*
	}
	
	public boolean hasExceededLimits() throws MessageException {
		UserStats us = this.getUserStats();
		if (us != null) {
			if (Double.parseDouble(us.maxSpaceGb) > -1) {
				if (Double.parseDouble(us.usedSpaceGb) > Double.parseDouble(us.maxSpaceGb)) {
					return true;
				}		
			}
			Plan p = this.getPlan();
			if (p != null) {
				if (p.getMaxActiveTorrents() > -1) {
					if (getRunningTorrents().size() > p.getMaxActiveTorrents()) {
						return true;
					}
				}
			}
			return false;
		} else {
			//if the userstats are null, then likely the seedbox is unreachable
			throw new MessageException("Your seedbox appears to be unreachable! Please contact support.");
		}
	}
	
	public void notifyLimitsExceeded() {
		//check if user has already been notified
		if (getUserMessagesOfType(UserMessage.Type.LIMITSEXCEEDED).isEmpty()) {
			addUserMessage("Limits Exceeded", "You have exceeded your plan limits! All torrents will be paused until you remove some.", UserMessage.State.ERROR, UserMessage.Type.LIMITSEXCEEDED);
		}
	}
	
	public void removeLimitsExceeded() {
		dismissUserMessagesOfType(UserMessage.Type.LIMITSEXCEEDED);
	}
	
	public class UserStats {
		public String maxSpaceGb;
		public String usedSpaceGb;
		public String rateDownloadKb;
		public String rateUploadKb;
	}*/
	
	public static User findByApiKey(String apiKey) {
		return User.all().filter("apiKey", apiKey).get();
	}
	
	public static User findByOpenId(String openId) {
		return User.all().filter("openId", openId).get();
	}
	
	public static User findByEmailAddress(String emailAddress) {
		return User.all().filter("emailAddress", emailAddress).get();
	}
	
}

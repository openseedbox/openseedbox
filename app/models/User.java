package models;

import com.openseedbox.code.MessageException;
import com.openseedbox.backend.BackendManager;
import com.openseedbox.backend.ITorrent;
import com.openseedbox.backend.transmission.TransmissionTorrent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Email;
import play.data.validation.Max;
import play.db.jpa.Model;

@Entity
@Table(name="user")
public class User extends Model {
	
	@Email
	@Column(name="email_address")
	private String emailAddress;
	
	@Column(name="open_id")
	private String openId;
	
	@Column(name="is_admin")
	private boolean admin;
	
	@Column(name="avatar_url")
	private String avatarUrl;
	
	@Column(name="display_name")
	private String displayName;
	
	@Column(name="last_access")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastAccess;
	
	@OneToOne
	@JoinColumn(name="primary_account_id")
	private Account primaryAccount;
	
	@Column(name="time_zone")
	private String timeZone;
	
	@Column(name="api_key")
	@Max(32)
	private String apiKey;

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
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

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void generateApiKey() {
		String salt = Play.configuration.getProperty("application.secret", "salt value");
		String key = this.emailAddress + salt;
		this.apiKey = DigestUtils.md5Hex(key);
		//this.save();
	}
	
	public boolean isAdmin() {
		return admin;
	}
	
	public void setAdmin(boolean isAdmin) {
		this.admin = isAdmin;
	}
	
	public List<Invitation> getInvitations() {
		return null;
		//return Invitation.all().filter("invitingUser", this).order("invitationDate").fetch();
	}
	
	public List<Account> getAvailableAccounts() {
		List<Account> ret = new ArrayList<Account>();
		
		//add the users account since it needs to be in the list of available accounts so the user can switch back to it
		ret.add(this.getPrimaryAccount());
		
		//available accounts are all the invitations where the emailAddress is the same as the current user
		List<Invitation> invitations = null;//Invitation.all().filter("emailAddress", this.emailAddress).fetch();
		for (Invitation i : invitations) {
			User u = i.getInvitingUser();
			ret.add(u.getPrimaryAccount());
		}
		return ret;
	}

	public Account getPrimaryAccount() {
		return this.primaryAccount;
	}
	
	public void setPrimaryAccount(Account primaryAccount) {
		this.primaryAccount = primaryAccount;
	}
	
	public String getEmailAddress() {
		return this.emailAddress;
	}
	
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	public boolean hasPlan() {
		return getPrimaryAccount().getPlan() != null;
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
			//a.save();
		} else {
			throw new MessageException("User " + this.emailAddress + " isnt associated with an account!");
		}
	}
	
	public ITorrent addTorrent(File f) throws MessageException {
		return BackendManager.getForAccount(primaryAccount).addTorrent(f);	
	}
	
	public ITorrent addTorrent(String urlOrMagnet) throws MessageException {
		return BackendManager.getForAccount(primaryAccount).addTorrent(urlOrMagnet);
	}
	
	private Torrent newTorrent(TransmissionTorrent tt) {
		return null;
		/*
		Torrent t = new Torrent();
		t.hashString = tt.hashString;
		//for some reason, the names are URLEncoded. Strip it
		String name = tt.name;
		try {
			name = URLDecoder.decode(name, "UTF8");
		} catch (Exception ex) {
			//fuck off java and your checked UnsupportedEncodingException's
		}
		t.name = name;
		t.user = this;
		t.insert();
		t.setTransmissionTorrent(tt);
		return t;	*/	
	}
	
	public Torrent getTorrent(String hashString) {
		return Torrent.find("hashString = ? AND account = ?", hashString, this.primaryAccount).first();
	}	
	
	public List<Torrent> getTorrents() {
		return Torrent.find("account = ?", this.primaryAccount).fetch();
	}
	
	public List<Torrent> getRunningTorrents() {
		List<Torrent> ret = new ArrayList<Torrent>();
		for (Torrent t : getTorrents()) {
			if (t.isRunning()) {
				ret.add(t);
			}
		}
		return ret;
	}
	
	public List<Torrent> getTorrents(String group) throws MessageException {
		if (group.equals("All")) {
			return getTorrents();
		}
		return Torrent.find("account = ? AND groups IN ?", this.primaryAccount, group).fetch();
	}	
	
	public List<String> getTorrentGroups() {
		List<Torrent> ts = Torrent.find("select groups from Torrent t WHERE account=?",
				  this.primaryAccount).fetch();
		List<String> ret = new ArrayList<String>();
		for (Torrent t : ts) {
			for (String g : t.getGroups()) {
				if (!ret.contains(g)) {
					ret.add(g);
				}
			}
		}
		return ret;
	}
	
	public String getDisplayName() {
		return StringUtils.isEmpty(this.displayName) ? this.emailAddress : this.displayName;
	}
	
	public void setDisplayName(String name) {
		this.displayName = name;
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
		return _userStats;*/
	}	
	
	public List<UserMessage> getUnreadMessages() {
		return null;/*
		return UserMessage.all().filter("user", this).filter("dismissDateUtc", null).fetch();*/
	}
	
	public List<UserMessage> getAllMessages() {
		return null;/*
		return UserMessage.all().filter("user", this).fetch();*/
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
				.filter("paymentDateUtc", null).fetch();*/
	}
	
	public List<Invoice> getPaidInvoices() {
		return null;/*
		return Invoice.all()
				.filter("account", this.getPrimaryAccount())
				.filter("paymentDateUtc !=", null)
				.order("-paymentDateUtc").fetch();*/	
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
	}
	
	public static User getByApiKey(String apiKey) {
		return User.find("apiKey = ?", apiKey).first();
	}
	
	public static User getByOpenId(String openId) {
		return User.find("openId = ?", openId).first();
	}
	
	public static User getByEmailAddress(String emailAddress) {
		return User.find("emailAddress = ?", emailAddress).first();
	}
	
}

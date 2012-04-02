package models;

import code.MessageException;
import code.Util;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.Torrent.TorrentGroup;
import play.data.validation.Email;
import play.modules.siena.EnhancedModel;
import securesocial.provider.SocialUser;
import securesocial.provider.UserId;
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
	@Column("is_activated")
	public Boolean isActivated;
	@Column("auth_provider_type")
	public String authProviderType;
	@Column("avatar_url")
	public String avatarUrl;
	@Column("display_name")
	public String displayName;
	@Column("last_access")
	@DateTime
	public Date lastAccess;
	@Column("activation_uuid")
	public String activationUuid;
	
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
	
	public static User fromSocialUser(SocialUser su) {
		return User.all().filter("authProviderType", su.id.provider.name())
				.filter("oauthId", su.id.id).get();
	}
	
	public static User fromUserId(UserId id) {
		SocialUser su = new SocialUser();
		su.id = id;
		return User.fromSocialUser(su);
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

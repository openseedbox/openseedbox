package models;

import com.openseedbox.backend.ITorrent;
import java.util.List;
import siena.Column;
import siena.Table;

@Table("torrent_group")
public class UserTorrent extends ModelBase {

	@Column("group_name") private String groupName;
	@Column("user_id") private User user;
	@Column("torrent_hash") private String torrentHash;
	
	private transient Torrent torrent;
	
	public static int getAverageTorrentsPerUser() {		
		return -1;
	}

	public static List<UserTorrent> getByUser(User u) {
		return UserTorrent.all().filter("user", u).fetch();
	}
	
	public static UserTorrent getByUser(User u, String hash) {
		return UserTorrent.all().filter("user", u)
				  .filter("torrentHash", hash).get();
	}

	public static List<UserTorrent> getByUser(User u, List<String> hashes) {
		return UserTorrent.all().filter("user", u)
				  .filter("torrentHash IN", hashes).fetch();
	}
	
	public static int getUsersWithTorrentCount(String hash) {
		return UserTorrent.all().filter("torrentHash", hash).count();
	}			  
	
	public String getGroupName() {		
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getTorrentHash() {
		return torrentHash;
	}

	public void setTorrentHash(String hashString) {
		this.torrentHash = hashString;
	}
	
	public void setTorrent(Torrent t) {
		this.torrent = t;
	}
	
	public Torrent getTorrent() {
		if (this.torrent == null) {
			this.torrent = Torrent.getByHash(this.getTorrentHash());
		}
		return torrent;
	}
	
	public String getNiceStatus() {
		switch (this.getTorrent().getStatus()) {
			case DOWNLOADING:
				return "Downloading";
			case SEEDING:
				return "Seeding";
			case PAUSED:
				return "Paused";
			case METADATA_DOWNLOADING:
				return "Metadata Downloading";
			case ERROR:
				return "Error";
		}
		return null;
	}
	
	
}

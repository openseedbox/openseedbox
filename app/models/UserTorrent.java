package models;

import java.sql.Connection;
import siena.Column;
import siena.Table;
import siena.jdbc.JdbcPersistenceManager;

@Table("torrent_group")
public class UserTorrent extends ModelBase {

	@Column("group_name")
	private String groupName;
	@Column("user_id") private User user;
	private String hashString;
	
	public static int getAverageTorrentsPerUser() {
		
		return -1;
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

	public String getHashString() {
		return hashString;
	}

	public void setHashString(String hashString) {
		this.hashString = hashString;
	}
	
	
	
}

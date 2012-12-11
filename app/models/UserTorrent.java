package models;

import siena.Column;
import siena.Table;

@Table("torrent_group")
public class UserTorrent extends ModelBase {

	@Column("group_name")
	private String groupName;
	@Column("user_id") private User user;
	private String hashString;

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

package com.openseedbox.models;

import com.openseedbox.backend.IFile;
import com.openseedbox.code.Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import siena.Column;
import siena.Table;

@Table("torrent_group")
public class UserTorrent extends ModelBase {

	@Column("group_name")
	private String groupName;
	@Column("user_id")
	private User user;
	@Column("torrent_hash")
	private String torrentHash;
	private transient Torrent torrent;
	private boolean adding;
	private boolean deleting;

	public static int getAverageTorrentsPerUser() {
		return -1;
	}

	public static List<UserTorrent> getByUser(User u) {
		return UserTorrent.all().filter("user", u).fetch();
	}

	public static List<UserTorrent> getByUserAndGroup(User u, String group) {
		return UserTorrent.all()
				  .filter("user", u).filter("groupName", group).fetch();
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

	public static void blankOutGroup(User u, String group) {
		List<UserTorrent> list = UserTorrent.getByUserAndGroup(u, group);
		for (UserTorrent ut : list) {
			ut.setGroupName(null);
		}
		UserTorrent.batch().update(list);
	}

	public String getNiceStatus() {
		if (this.isAdding()) {
			return "Adding";
		} else if (this.isDeleting()) {
			return "Deleting";
		}
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
				if (this.getTorrent().getDownloadSpeedBytes() > 0) {
					return "Downloading";
				}
				return "Error";
		}
		return null;
	}

	public String getNiceSubStatus() {
		String ret = "";
		Torrent t = getTorrent();
		if (t.hasErrorOccured() && (t.getDownloadSpeedBytes() == 0)) {
			return "";
		}
		if (t.isSeeding()) {
			ret += String.format("at %s/s", Util.getBestRate(t.getUploadSpeedBytes()));
		}
		if (t.isMetadataDownloading()) {
			ret += t.isPaused() ? "(Metadata " : "(";
			ret += Util.formatPercentage(t.getMetadataPercentComplete() * 100) + "%";
			ret += ")";
		} else if (t.getPercentComplete() < 1 && !t.isPaused()) {
			ret += Util.formatPercentage((t.getPercentComplete() * 100)) + "%";
		}
		if (t.isComplete()) {
			if (!t.isPaused()) {
				ret += String.format(" (%s %s)", Util.getBestRate(t.getUploadedBytes()),
						  "seeded, ratio: " + Util.getSignificantFigures(t.getRatio(), 2));
			}
		} else {
			if (!t.isMetadataDownloading()) {
				ret += String.format(" (%s downloaded)", Util.getBestRate(t.getDownloadedBytes()));
			}
		}
		return ret;
	}

	public String getNiceTotalSize() {
		Torrent t = getTorrent();
		if (t.getTotalSizeBytes() > 0) {
			return Util.getBestRate(t.getTotalSizeBytes());
		}
		return "Unknown";
	}

	public List<TreeNode> getFilesAsTree() {
		List<TreeNode> mapTree = new ArrayList<TreeNode>();
		for (IFile f : getTorrent().getFiles()) {
			String[] paths = f.getFullPath().split("/");
			//Logger.info("paths: %s", paths.length);
			List<TreeNode> parent = mapTree;
			for (int x = 0; x < paths.length; x++) {
				String path = paths[x];
				TreeNode n = getTreeNode(parent, path);
				TreeNode newTn;
				if (n == null) {
					newTn = new TreeNode();
					newTn.name = path;
					//only set the file on the final node so earlier nodes
					//dont keep getting overwritten when multiple files match
					if (paths.length - 1 == x) {
						newTn.file = f;
					}
					newTn.level = x;
					newTn.fullPath = getFullPath(paths, x);
					parent.add(newTn);
					//Logger.info("added:%s", newTn.name);
				} else {
					newTn = n;
				}
				Collections.sort(newTn.children);
				parent = newTn.children;
			}
		}
		return mapTree;
	}

	private TreeNode getTreeNode(List<TreeNode> t, String name) {
		for (TreeNode tn : t) {
			if (tn.name.equals(name)) {
				return tn;
			}
		}
		return null;
	}

	private String getFullPath(String[] path, int level) {
		return StringUtils.join(path, "/", 0, level + 1);
	}

	/* Getters and Setters */
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

	public boolean isAdding() {
		return adding;
	}

	public void setAdding(boolean adding) {
		this.adding = adding;
	}

	public boolean isDeleting() {
		return deleting;
	}

	public void setDeleting(boolean deleting) {
		this.deleting = deleting;
	}	
	/* End Getters and Setters */
	
	public class TreeNode implements Comparable {

		public String name = "";
		public IFile file = null;
		public List<TreeNode> children = new ArrayList<TreeNode>();
		public int level = 0;
		public String fullPath = "";

		@Override
		public String toString() {
			return String.format("TreeNode, name: %s, children:%s", name, children.size());
		}

		@Override
		public int compareTo(Object t) {
			if (t instanceof TreeNode) {
				TreeNode tn = (TreeNode) t;
				return this.name.compareTo(tn.name);
			}
			return -1;
		}

		public boolean isAnyChildWanted() {
			boolean wanted = false;
			for (TreeNode tn : this.children) {
				if (tn.file != null && tn.file.isWanted()) {
					wanted = true;
					break;
				}
				wanted = tn.isAnyChildWanted();
				if (wanted) {
					break;
				}
			}
			return wanted;
		}

		public boolean isAnyChildIncomplete() {
			boolean complete = false;
			for (TreeNode tn : this.children) {
				if (tn.file != null && !tn.file.isCompleted()) {
					complete = true;
					break;
				}
				complete = tn.isAnyChildIncomplete();
				if (complete) {
					break;
				}
			}
			return complete;
		}

		public long getTotalSize() {
			long total = 0l;
			if (this.file != null) {
				total += this.file.getFileSizeBytes();
			} else {
				for (TreeNode child : this.children) {
					total += child.getTotalSize();
				}
			}
			return total;
		}
	}
}

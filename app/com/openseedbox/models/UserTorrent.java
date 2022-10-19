package com.openseedbox.models;

import java.util.*;

import org.apache.commons.lang.StringUtils;

import com.openseedbox.backend.*;
import com.openseedbox.code.Util;
import com.openseedbox.gson.SerializedAccessorName;
import com.openseedbox.gson.UseAccessor;

import javax.persistence.*;

@Entity
@Table(name = "torrent_group")
@UseAccessor
public class UserTorrent extends ModelBase {

	private String groupName;
	@ManyToOne (optional = false)
	private User user;
	@ManyToOne (targetEntity = Torrent.class, optional = false)
	private String torrentHash;
	private transient Torrent torrent;
	private boolean paused;
	private boolean running;

	public static int getAverageTorrentsPerUser() {
		return -1;
	}

	public static List<UserTorrent> getByUser(User u) {
		return UserTorrent.<UserTorrent>all()
				.where()
				.eq("user", u)
				.findList();
	}

	public static List<UserTorrent> getByUserAndGroup(User u, String group) {
		return UserTorrent.<UserTorrent>all()
				.where()
				.eq("user", u)
				.eq("groupName", group)
				.findList();
	}

	public static UserTorrent getByUser(User u, String hash) {
		return UserTorrent.<UserTorrent>all()
				.where()
				.eq("user", u)
				.eq("torrentHash", hash)
				.findUnique();
	}

	public static List<UserTorrent> getByUser(User u, List<String> hashes) {
		return UserTorrent.<UserTorrent>all()
				.where()
				.eq("user", u)
				.eq("torrentHash IN", hashes)
				.findList();
	}

	public static List<UserTorrent> getByHash(String hash) {
		return UserTorrent.<UserTorrent>all()
				.where()
				.eq("torrentHash", hash)
				.findList();
	}

	public static int getUsersWithTorrentCount(String hash) {
		return UserTorrent.<UserTorrent>all().where().eq("torrentHash", hash).findRowCount();
	}

	public static void blankOutGroup(User u, String group) {
		List<UserTorrent> list = UserTorrent.getByUserAndGroup(u, group);
		for (UserTorrent ut : list) {
			ut.setGroupName(null);
		}
		save(list);
	}

	public static boolean isTorrentStoppedByAllUsers(String hash) {
		int count = UserTorrent.<UserTorrent>all().where().eq("torrentHash", hash).findRowCount();
		int paused = UserTorrent.<UserTorrent>all().where().eq("torrentHash", hash).eq("paused", true).findRowCount();
		return (count == paused);
	}

	public static boolean isTorrentStartedByAUser(String hash) {
		return UserTorrent.all().where().eq("torrentHash", hash).eq("paused", false).findRowCount() >= 1;
	}

	@SerializedAccessorName("nice-status")
	public String getNiceStatus() {
		TorrentState ts = this.getTorrent().getStatus();
		if (this.isPaused()) {
			return "Paused";
		} else if (running && ts == TorrentState.PAUSED) {
			return "Running";
		}
		switch (ts) {
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

	@SerializedAccessorName("nice-sub-status")
	public String getNiceSubStatus() {
		if (this.isPaused()) {
			return "";
		}
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
		String ts = getStats().getTotalSize();
		return (!ts.equals("N/A")) ? ts : "Unknown";
	}

	@SerializedAccessorName("nice-stats")
	public NiceStats getStats() {
		return new NiceStats(this.getTorrent());
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
	@SerializedAccessorName("group")
	public String getGroupName() {
		if (groupName == null) {
			return User.TORRENT_GROUP_UNGROUPED;
		}
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

	@SerializedAccessorName("hash")
	public String getTorrentHash() {
		return torrentHash;
	}

	public void setTorrentHash(String hashString) {
		this.torrentHash = hashString;
	}

	public void setTorrent(Torrent t) {
		this.torrent = t;
	}

	@SerializedAccessorName("torrent-data")
	public Torrent getTorrent() {
		if (this.torrent == null) {
			this.torrent = Torrent.getByHash(this.getTorrentHash());
		}
		return torrent;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public boolean isRunning() {
		if (isPaused()) {
			return false;
		}
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	/* End Getters and Setters */
	@UseAccessor
	public class TreeNode extends AbstractFile implements Comparable {

		private String name = "";
		private IFile file = null;
		private List<TreeNode> children = new ArrayList<TreeNode>();
		private int level = 0;
		private String fullPath = "";

		@Override
		public String toString() {
			return String.format("TreeNode, name: %s, children:%s", name, children.size());
		}
		
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

		@SerializedAccessorName("total-size-bytes")
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

		public String getDownloadLink() {
			if (this.file != null) {
				return this.file.getDownloadLink();
			}
			return null;
		}

		/* Getters and Setters */
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public IFile getFile() {
			return file;
		}

		public void setFile(IFile file) {
			this.file = file;
		}

		@SerializedAccessorName("children")
		public List<TreeNode> getChildren() {
			return children;
		}

		public void setChildren(List<TreeNode> children) {
			this.children = children;
		}

		public int getLevel() {
			return level;
		}

		public void setLevel(int level) {
			this.level = level;
		}

		public String getFullPath() {
			return fullPath;
		}

		public void setFullPath(String fullPath) {
			this.fullPath = fullPath;
		}

		public String getId() {
			if (file != null) {
				return file.getId();
			}
			return "";
		}

		public boolean isWanted() {
			if (file != null) {
				return file.isWanted();
			}
			return isAnyChildWanted();
		}

		public long getBytesCompleted() {
			if (file != null) {
				return file.getBytesCompleted();
			}
			long ret = -1;
			for (TreeNode c : this.children) {
				ret += c.getBytesCompleted();
			}
			return ret;
		}

		public long getFileSizeBytes() {
			if (file != null) {
				return file.getFileSizeBytes();
			}
			return getTotalSize();
		}

		public int getPriority() {
			if (file != null) {
				return file.getPriority();
			}
			return -1;
		}

		@Override
		public double getPercentComplete() {
			if (isCompleted()) {
				return 1d;
			}
			return super.getPercentComplete();
		}

		@Override
		public boolean isCompleted() {
			return !isAnyChildIncomplete();
		}
		/* End Getters and Setters */
	}

	@UseAccessor
	public class NiceStats {

		private ITorrent t;

		public NiceStats(ITorrent t) {
			this.t = t;
		}

		@SerializedAccessorName("total-size")
		public String getTotalSize() {
			if (!t.isMetadataDownloading()) {
				return Util.getBestRate(t.getTotalSizeBytes());
			}
			return "N/A";
		}

		@SerializedAccessorName("download-speed")
		public String getDownloadSpeed() {
			if (!t.isMetadataDownloading()) {
				return Util.getBestRate(t.getDownloadSpeedBytes());
			}
			return "N/A";
		}

		@SerializedAccessorName("upload-speed")
		public String getUploadSpeed() {
			if (!t.isMetadataDownloading()) {
				return Util.getBestRate(t.getUploadSpeedBytes());
			}
			return "N/A";
		}

		@SerializedAccessorName("amount-downloaded")
		public String getAmountDownloaded() {
			if (!t.isMetadataDownloading()) {
				return Util.getBestRate(t.getDownloadedBytes());
			}
			return "N/A";
		}

		@SerializedAccessorName("amount-uploaded")
		public String getAmountUploaded() {
			if (!t.isMetadataDownloading()) {
				return Util.getBestRate(t.getUploadedBytes());
			}
			return "N/A";
		}

		@SerializedAccessorName("ratio")
		public String getRatio() {
			if (!t.isMetadataDownloading()) {
				return Util.formatPercentage(t.getRatio());
			}
			return "N/A";
		}

		@SerializedAccessorName("percent-complete")
		public String getPercentComplete() {
			return Util.formatPercentage(t.getPercentComplete() * 100) + "%";
		}

		@SerializedAccessorName("metadata-percent-complete")
		public String getMetadataPercentComplete() {
			return Util.formatPercentage(t.getMetadataPercentComplete() * 100) + "%";
		}
	}
}

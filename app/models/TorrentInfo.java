package models;

import code.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.util.*;
import models.Torrent.TorrentFile;
import org.apache.commons.lang.StringUtils;

public class TorrentInfo {

	public List<TorrentFile> files = new ArrayList<TorrentFile>();
	public List<TorrentPeer> peers = new ArrayList<TorrentPeer>();
	public List<TorrentTracker> trackers = new ArrayList<TorrentTracker>();
	
	public PeersFrom peersFrom;

	public static TorrentInfo fromJson(String json) {
		return null;
		/*
		TorrentInfo ti = new TorrentInfo();
		Map<String, Object> mo = (JSONObject) JSONValue.parse(json);
		//do trackers
		List<Object> trackers = (List<Object>) mo.get("trackerStats");
		List<Long> priorities = (List<Long>) mo.get("priorities");
		List<Long> wanted = (List<Long>) mo.get("wanted");
		List<Object> files = (List<Object>) mo.get("files");
		List<Object> peers = (List<Object>) mo.get("peers");
		JSONObject peersFrom = (JSONObject) mo.get("peersFrom");
		Gson g = new GsonBuilder().create();
		for (Object o : trackers) {
			JSONObject jo = (JSONObject) o;
			TorrentTracker tt = g.fromJson(jo.toJSONString(), TorrentTracker.class);
			ti.trackers.add(tt);
		}
		for (int x = 0; x < files.size(); x++) {
			JSONObject o = (JSONObject) files.get(x);
			TorrentFile tf = g.fromJson(o.toJSONString(), TorrentFile.class);
			tf.priority = priorities.get(x).intValue();
			tf.wanted = (wanted.get(x) > 0);
			tf.transmissionId = x;
			ti.files.add(tf);
		}
		for (Object o : peers) {
			TorrentPeer p = g.fromJson(((JSONObject) o).toJSONString(), TorrentPeer.class);
			ti.peers.add(p);
		}
		ti.peersFrom = g.fromJson(peersFrom.toJSONString(), PeersFrom.class);
		return ti;*/
	}

	public List<TreeNode> filesAsTree() {
		List<TreeNode> mapTree = new ArrayList<TreeNode>();
		for (TorrentFile f : files) {
			String[] paths = f.name.split("/");
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

	public class TreeNode implements Comparable {

		public String name = "";
		public TorrentFile file = null;
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
		
		public Boolean anyChildWanted() {
			Boolean wanted = false;
			for (TreeNode tn : this.children) {
				if (tn.file != null && tn.file.wanted) {
					wanted = true; break;
				}
				wanted = tn.anyChildWanted();
				if (wanted) { break; }
			}
			return wanted;
		}
		
		public long getTotalSize() {
			long total = 0l;
			if (this.file != null) {
				total += this.file.length;
			} else {
				for (TreeNode child : this.children) {
					total += child.getTotalSize();
				}
			}
			return total;
		}
		
		public String getNiceTotalSize() {
			long ts = getTotalSize();
			if (ts > 1073741824) {
				return Util.getRateGb(ts) + "GB";
			} else if (ts > 1048576) {
				return Util.getRateMb(ts) + "MB";
			} else {
				return Util.getRateKb(ts) + "KB";
			}		
		}
	}

	public class TorrentPeer {

		public String address;
		public String clientName;
		public boolean clientIsChoked;
		public boolean clientIsInterested;
		public String flagStr;
		public boolean isDownloadingFrom;
		public boolean isEncrypted;
		public boolean isIncoming;
		public boolean isUploadingTo;
		public boolean isUTP;
		public boolean peerIsChoked;
		public boolean peerIsInterested;
		public int port;
		public double progress;
		public long rateToClient;
		public long rateToPeer;
	}

	public class TorrentTracker {

		public String announce;
		public int downloadCount;
		public boolean hasAnnounced;
		public boolean hasScraped;
		public String host;
		@SerializedName("id")
		public int trackerId;
		public boolean isBackup;
		public int lastAnnouncePeerCount;
		public String lastAnnounceResult;
		public int lastAnnounceStartTime;
		public boolean lastAnnounceSucceeded;
		public long lastAnnounceTime;
		public boolean lastAnnounceTimedOut;
		public String lastScrapeResult;
		public int lastScrapeStartTime;
		public boolean lastScrapeSucceeded;
		public int lastScrapeTime;
		public boolean lastScrapeTimedOut;
		public int leecherCount;
		public int nextAnnounceTime;
		public int nextScrapeTime;
		public String scrape;
		public int scrapeState;
		public int seederCount;
		public int tier;

		public String getLastAnnounceTime() {
			if (lastAnnounceTime <= 0) {
				return "N/A";
			}
			Date time = new Date(Long.parseLong("" + (this.lastAnnounceTime * 1000)));
			return Util.formatDateTime(time);
		}

		public String getLastScrapeTime() {
			if (lastScrapeTime <= 0) {
				return "N/A";
			}
			return Util.formatDateTime(new Date(Long.parseLong("" + (this.lastScrapeTime * 1000))));
		}
	}

	public class PeersFrom {

		public int fromCache;
		public int fromDht;
		public int fromIncoming;
		public int fromLpd;
		public int fromLtep;
		public int fromPex;
		public int fromTracker;
	}
}

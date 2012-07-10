package code.transmission;

import code.Util;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import models.Node;
import models.User;
import org.apache.commons.lang.StringUtils;

public class TransmissionTorrent {

	public int id;
	public String name;
	public double percentDone;
	public long rateDownload;
	public long rateUpload;
	public String errorString;
	public String hashString;
	public long totalSize;
	public long downloadedEver;
	public long uploadedEver;
	public int status;
	public double metadataPercentComplete;
	public String downloadDir;
	public List<TransmissionFile> files;
	public List<Integer> wanted;
	public List<TransmissionPeer> peers;
	public TransmissionPeerFrom peersFrom;
	public List<Integer> priorities;
	public List<TransmissionTrackerStats> trackerStats;
	
	private void fixFiles() {
		//set the id and wanted fields on each file
		//since the rpc response doesnt have them
		//but its way easier to program when they are present
		List<TransmissionFile> newList = new ArrayList<TransmissionFile>();
		for (int x = 0; x < files.size(); x++) {
			TransmissionFile f = files.get(x);
			f.id = x;
			f.wanted = (wanted.get(x) == 1); 
			f.priority = priorities.get(x);
			newList.add(f);
		}
		this.files = newList;
	}
	
	public Boolean isComplete() {
		return (this.percentDone == 1.0);
	}
	
	public List<TreeNode> getFilesAsTree() {
		fixFiles();
		List<TreeNode> mapTree = new ArrayList<TreeNode>();
		for (TransmissionFile f : files) {
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
		public TransmissionFile file = null;
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
				if (tn.file != null && tn.file.wanted) {
					wanted = true; break;
				}
				wanted = tn.isAnyChildWanted();
				if (wanted) { break; }
			}
			return wanted;
		}
		
		public boolean isAnyChildIncomplete() {
			boolean complete = false;
			for (TreeNode tn : this.children) {
				if (tn.file != null && !tn.file.isFinishedDownloading()) {
					complete = true; break;
				}
				complete = tn.isAnyChildIncomplete();
				if (complete) { break; }
			}			
			return complete;
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
			return Util.getBestRate(ts);
		}
		
		public String getDownloadLink(User u) {
			Node n = u.getNode();
			String fname = (file != null) ? file.name : name;
			String status = (isComplete()) ? "complete" : "incomplete";
			try {
				return String.format("http://%s/openseedbox-server/download.php?user_name=%s&file_path=%s&status=%s",
					n.ipAddress, URLEncoder.encode(u.emailAddress, "UTF-8"), URLEncoder.encode(fname, "UTF-8"), status);
			} catch (UnsupportedEncodingException ex) {
				return "Platform doesnt support UTF-8 encoding??";
			}
		}		
		
		public String getZipDownloadLink(User u) {
			return String.format("%s&type=zip", getDownloadLink(u));
		}
		
	}	

	public class TransmissionFile {

		public int id;
		public boolean wanted;
		public long bytesCompleted;
		public long length;
		public int priority;
		public String name;
		
		public Boolean isFinishedDownloading() {
			return (bytesCompleted == length);
		}
		
		public String getPercentComplete() {
			double percent = ((double) bytesCompleted / length) * 100;
			return String.format("%.2f", percent);
		}
	}

	public class TransmissionPeer {

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
		
		public String getDownloadSpeed() {
			return Util.getRateKb(this.rateToClient);
		}
		
		public String getUploadSpeed() {
			return Util.getRateKb(this.rateToPeer);
		}
		
	}

	public class TransmissionPeerFrom {

		public int fromCache;
		public int fromDht;
		public int fromIncoming;
		public int fromLpd;
		public int fromLtep;
		public int fromPex;
		public int fromTracker;
	}

	public class TransmissionTrackerStats {

		public String announce;
		public int downloadCount;
		public boolean hasAnnounced;
		public boolean hasScraped;
		public String host;
		public int id;
		public boolean isBackup;
		public int lastAnnouncePeerCount;
		public String lastAnnounceResult;
		public int lastAnnounceStartTime;
		public boolean lastAnnounceSucceeded;
		public long lastAnnounceTime;
		public boolean lastAnnounceTimedOut;
		public String lastScrapeResult;
		public long lastScrapeStartTime;
		public boolean lastScrapeSucceeded;
		public long lastScrapeTime;
		public boolean lastScrapeTimedOut;
		public int leecherCount;
		public long nextAnnounceTime;
		public long nextScrapeTime;
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
		
		public String getDownloadCount() {
			return (this.downloadCount == -1) ? "N/A" : String.valueOf(this.downloadCount);
		}
	}
}

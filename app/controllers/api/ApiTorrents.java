package controllers.api;

import com.openseedbox.backend.IFile;
import com.openseedbox.backend.IPeer;
import com.openseedbox.backend.ITracker;
import com.openseedbox.code.Util;
import com.openseedbox.models.TorrentEvent;
import com.openseedbox.models.User;
import com.openseedbox.models.UserTorrent;
import com.openseedbox.models.UserTorrent.TreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class ApiTorrents extends Api {
	
	/* GET /api/torrents/details?hash=foo
	 * GET /api/torrents/details?hashes[]=foo&hashes[]=bar */
	public static void details(String hash, List<String> hashes) {
		User u = getApiUser();
		validateOrError(hash, hashes);
		List<UserTorrent> uts = new ArrayList<UserTorrent>();
		if (!StringUtils.isBlank(hash)) {
			uts.add(UserTorrent.getByUser(u, hash));
		} else {
			uts.addAll(UserTorrent.getByUser(u, hashes));
		}		
		result(getList(uts, u));
	}
	
	/* GET /torrents/list 
	 * GET /torrents/list?group=Music */
	public static void list(String group) {
		User u = getApiUser();
		List<UserTorrent> uts;
		if (!StringUtils.isBlank(group)) {
			uts = u.getTorrentsInGroup(group);			
		} else {
			uts = u.getTorrents();
		}
		result(getList(uts, u));
	}
	
	/* GET /api/torrents/files?hash=foo
	 * GET /api/torrents/files?hash=foo&tree=true
	 * GET /api/torrents/files?hashes[]=foo&hashes[]=bar
	 * GET /api/torrents/files?hashes[]=foo&hashes[]=bar&tree=true */
	public static void files(String hash, List<String> hashes, boolean tree) {
		List<UserTorrent> uts = getUsableHashes(hash, hashes);
		if (tree) {
			Map<String, List<TreeNode>> torrents = new HashMap<String, List<TreeNode>>();
			for (UserTorrent ut : uts) {
				torrents.put(ut.getTorrentHash(), ut.getFilesAsTree());
			}
			result(torrents);
		}
		Map<String, List<IFile>> torrents = new HashMap<String, List<IFile>>();
		for (UserTorrent ut : uts) {
			torrents.put(ut.getTorrentHash(), ut.getTorrent().getFiles());
		}
		result(torrents);
	}
	
	/* GET /api/torrents/peers?hash=foo
	 * GET /api/torrents/peers?hashes[]=foo&hashes[]=bar */	 
	public static void peers(String hash, List<String> hashes) {
		List<UserTorrent> uts = getUsableHashes(hash, hashes);
		Map<String, List<IPeer>> peers = new HashMap<String, List<IPeer>>();
		for (UserTorrent ut : uts) {
			peers.put(ut.getTorrentHash(), ut.getTorrent().getPeers());
		}
		result(peers);
	}
	
	/* GET /api/torrents/trackers?hash=foo
	 * GET /api/torrents/trackers?hashes[]=foo&hashes[]=bar */	
	public static void trackers(String hash, List<String> hashes) {
		List<UserTorrent> uts = getUsableHashes(hash, hashes);
		Map<String, List<ITracker>> trackers = new HashMap<String, List<ITracker>>();
		for (UserTorrent ut : uts) {
			trackers.put(ut.getTorrentHash(), ut.getTorrent().getTrackers());
		}
		result(trackers);
	}	
	
	private static List<UserTorrent> getUsableHashes(String hash, List<String> hashes) {
		validateOrError(hash, hashes);
		User u = getApiUser();
		List<UserTorrent> ret = new ArrayList<UserTorrent>();
		if (!StringUtils.isBlank(hash)) {
			ret.add(UserTorrent.getByUser(u, hash));
		}
		if (hashes != null && !hashes.isEmpty()) {
			ret.addAll(UserTorrent.getByUser(u, hashes));
		}
		return ret;
	}
	
	private static void validateOrError(String hash, List<String> hashes) {
		if (StringUtils.isBlank(hash) && (hashes == null || hashes.isEmpty())) {
			resultError("Please specify a 'hash' or 'hashes'");
		}		
	}
	
	private static Map<String,Object> getList(List<UserTorrent> uts, User u) {
		List<TorrentEvent> torrentAddEvents = TorrentEvent.getIncompleteForUser(u, TorrentEvent.TorrentEventType.ADDING);		
		List<TorrentEvent> torrentRemoveEvents = TorrentEvent.getIncompleteForUser(u, TorrentEvent.TorrentEventType.REMOVING);		
		return Util.convertToMap(new Object[] {
			"torrents", uts,
			"adding-torrent-count", torrentAddEvents.size(),
			"removing-torrent-count", torrentRemoveEvents.size()
		});
	}
	
}

package controllers;

import code.Transmission;
import code.MessageException;
import java.util.Arrays;
import java.util.List;
import models.*;
import models.Torrent.TorrentGroup;
import models.User.UserStats;
import org.h2.util.StringUtils;
import play.Logger;
import play.mvc.Before;
import play.mvc.Http.Header;

public class ClientController extends BaseController {
	
	@Before(unless={"login","auth"})
	public static void before() {
		User u = getCurrentUser();
		if (u == null) {
			redirect("/auth/login");
		}
		//check that a plan has been purchased
		if (u.getPrimaryAccount().getPlan() == null) {
			redirect("/account/");
		}
	}
	
	public static void index() throws MessageException {
		User u = getCurrentUser();
		List<TorrentGroup> torrentGroups = u.getTorrentGroups();
		UserStats userStats = u.getUserStats();
		render("client/index.html", torrentGroups, userStats);
	}
	
	public static void userStats() throws MessageException {
		UserStats _arg = getCurrentUser().getUserStats();
		render("tags/user-stats.html", _arg);
	}
	
	public static void tabs(String active) throws MessageException {
		List<TorrentGroup> _arg = getCurrentUser().getTorrentGroups();
		render("tags/client-tabs.html", _arg, active);
	}
	
	/*
	public static void addTorrent(String urlOrMagnet, boolean paused) throws MessageException {
		if (StringUtils.isNullOrEmpty(urlOrMagnet)) {
			resultError("Please enter a valid URL or magent link.");
		}
		Node n = getUserNode();
		Torrent t = n.getTransmission().addTorrent(urlOrMagnet, paused);
		if (t != null) {
			Logger.debug("Got here");
			User currentUser = getCurrentUser();			
			t.user = currentUser;
			t.save();
			result(t);
		}
		resultError("Error occured adding torrent.");
	}
	
	public static void startTorrent(String torrentHash) throws MessageException {
		Node n = getUserNode();
		Torrent t = Torrent.findById(torrentHash);
		t.status = 4; //Downloading
		t.save();		
		result(n.getTransmission().startTorrent(torrentHash));
	}
	
	public static void pauseTorrent(String torrentHash) throws MessageException {
		Node n = getUserNode();
		Torrent t = Torrent.findById(torrentHash);
		t.status = 0; //Paused
		t.save();
		result(n.getTransmission().pauseTorrent(torrentHash));		
	}
	
	public static void removeTorrent(String torrentHash, Boolean torrentOnly) throws MessageException {
		if (torrentOnly == null) { torrentOnly = true; }
		Node n = getUserNode();
		if (n.getTransmission().removeTorrent(torrentHash, torrentOnly)) {
			//remove from db too
			Torrent t = Torrent.findById(torrentHash);
			t.delete();
		}
		result(true);
	}
	
	public static void removeTorrents(String[] hashes) throws MessageException {
		if (hashes == null) { result(true); }
		Node n = getUserNode();
		n.getTransmission().removeTorrent(Arrays.asList(hashes), false);
		result(true);
	}
	
	public static void renderTorrentList(String group) throws MessageException {
		List<Torrent> torrents;
		User u = getCurrentUser();
		List<Torrent> userTorrents = u.getTorrents();
		checkUserTorrents();
		if (group == null || group.equals("All")) {
			torrents = userTorrents;
		} else if (group.equals("Downloading")) {
			torrents = u.getTorrentsWithStatus(4);
		} else if (group.equals("Seeding")) {
			torrents = u.getTorrentsWithStatus(8);
		} else if (group.equals("Paused")) {
			torrents = u.getTorrentsWithStatus(0);
		} else if (group.equals("Finished")) {
			torrents = u.getTorrentsWithStatus(16);
		} else {
			torrents = u.getTorrentsWithGroup(group);
		}
		render("client/torrent-list.html", torrents, group);
	}
	
	public static void renderTorrent(String torrentHash) throws MessageException {
		checkUserTorrents();
		Torrent _arg = Torrent.findById(torrentHash);
		if (_arg == null) {
			throw new MessageException("No such torrent for user: %s", torrentHash);
		}
		render("tags/torrent.html", _arg);
	}
	
	public static void addTorrentGroup(String torrentHash, String group) throws MessageException {
		Torrent t = Torrent.findById(torrentHash);
		if (group != null && !group.trim().isEmpty()) {
			TorrentGroup temp = new TorrentGroup(group);
			if (!t.groups.contains(temp)) {
				t.groups.add(temp);
			}
			t.save();
		}
		result(true);
	}
	
	public static void removeTorrentGroup(String torrentHash, String group) throws MessageException {
		Torrent t = Torrent.findById(torrentHash);
		if (group != null && !group.trim().isEmpty()) {
			t.groups.remove(new TorrentGroup(group));
			t.save();
		}
		result(true);		
	}
	
	public static void renderTorrentInfo(String torrentHash) throws MessageException {
		//torrent info is seeders, peers, files, tracker stats
		User u = getCurrentUser();
		TorrentInfo info = u.getNode().getTransmission().getTorrentInfo(torrentHash);
		Torrent torrent = Torrent.findById(torrentHash);
		if (params.get("ext") != null && !params.get("ext").equals("html")) {
			result(info);
		}
		renderTemplate("client/torrent-info.html", info, torrent);
	}
	
	public static void setIncludedTorrentFiles(String torrentHash, String[] fw, String[] ph,
			  String[] pn, String[] pl) throws MessageException {
		User u = getCurrentUser();
		Transmission t = u.getNode().getTransmission();
		if (fw == null) { fw = new String[]{}; }
		if (fw != null) {
			t.setFilesWanted(torrentHash, Arrays.asList(fw));
		}
		if (ph != null) {
			t.setFilePriority(torrentHash, Arrays.asList(ph), "high");
		}
		if (pn != null) {
			t.setFilePriority(torrentHash, Arrays.asList(pn), "normal");
		}
		if (pl != null) {
			t.setFilePriority(torrentHash, Arrays.asList(pl), "low");
		}
		result(true);
	}
	
	public static void torrentDetail(String torrentHash) {
		Torrent t = Torrent.findById(torrentHash);
		if (t == null) {
			resultError("No such torrent with hash: " + torrentHash);
		}
		result(t);
	}
	
	public static void renderAddTorrent(String torrentHash) {
		Torrent torrent = Torrent.findById(torrentHash);
		renderTemplate("client/torrent-add.html", torrent);
	}
	
	public static void setActiveUser(long id) {
		session.put("actualUserId", id);
		Header r = request.headers.get("Referer");
		String referer = "/client/index";
		if (r != null) {
			referer = r.value();
		}
		redirect(referer);
	}
	
	private static Node getUserNode() throws MessageException {
		User cu = getCurrentUser();
		Node n = cu.getNode();
		Logger.debug("Node name: %s (username: %s)", n.name, cu.emailAddress);
		if (n == null) {
			throw new MessageException(String.format("User %s is not assigned to any node.", cu.emailAddress));
		}
		return n;
	}
	
	private static void checkUserTorrents() throws MessageException{
		//make sure user isnt exceeding their limit. this can happen if they add
		//a torrent from a magnet link, because we dont know the total size
		//at the time they add it
		User u = getCurrentUser();
		UserStats us = u.getUserStats();
		if (Double.parseDouble(us.usedSpaceGb) > Double.parseDouble(us.maxSpaceGb)) {
			//delete the last torrent
			Torrent newest = null;
			for(Torrent t : u.getTorrents()) {
				if (newest != null) {
					if (t.createDateUTC.after(newest.createDateUTC)) {
						newest = t;
					}
				} else {
					newest = t;
				}
			}
			if (newest != null) {
				u.getNode().getTransmission().removeTorrent(newest.hashString, false);
				newest.delete();
			}
		}			
	}		*/
}

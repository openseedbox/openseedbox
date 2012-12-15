package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.openseedbox.backend.ITorrent;
import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.code.MessageException;
import com.openseedbox.code.Util;
import com.openseedbox.jobs.GenericJob;
import com.openseedbox.jobs.GenericJobResult;
import java.io.File;
import java.util.*;
import models.Node;
import models.Torrent;
import models.User;
import models.UserTorrent;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.libs.F.Promise;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Before;

public class Client extends Base {
	
	@Before(unless={"login","auth"})
	public static void before() {	
		User u = getCurrentUser();
		if (u == null) {
			Auth.login();
		}
		//check that a plan has been purchased
		/*
		if (u.getPrimaryAccount().getPlan() == null) {
			Accounts.index();
		}*/

		//check that limits have not been exceeded. if they have, pause all the torrents and notify user
		/*
		try {
			if (u.hasExceededLimits()) {
				List<Torrent> running = u.getRunningTorrents();
				List<String> hashes = new ArrayList<String>();
				for (Torrent t : running) {
					hashes.add(t.getHashString());
				}
				//Note: cant use async/continuations in @Before method, potential bottleneck
				TorrentControlJobResult res = 
						new TorrentControlJob(getActiveAccount(), hashes, TorrentAction.STOP).doJobWithResult();
				if (res.hasError()) {
					addGeneralError(res.error);
				}
				u.notifyLimitsExceeded();
			} else {
				u.removeLimitsExceeded();
			}
		} catch (Exception ex) {
			addGeneralError(ex);
		}	*/	
	}
	
	public static void index() {		
		render("client/index.html");
	}
	
	public static void update(String group) {
		//this is intended to be invoked via ajax
		List<UserTorrent> torrents = getCurrentUser().getTorrents();
		String data = renderToString("client/torrent-list.html", Util.convertToMap(new Object[] { "torrents", torrents }));
		result(data);
	}
	
	public static void addTorrent(final String urlOrMagnet, final File fileFromComputer) {		
		if (StringUtils.isEmpty(urlOrMagnet) && fileFromComputer == null) {
			setGeneralErrorMessage("Please enter a valid URL or magent link, or choose a valid file to upload.");
		} else {					
			final User u = getCurrentUser();
			Promise<GenericJobResult> p = new GenericJob() {
				@Override
				public Object doGenericJob() {	
					Node node = Node.getBestForNewTorrent();
					ITorrentBackend backend = node.getNodeBackend();
					//TODO: check that we dont already have the torrent somewhere in the system
					ITorrent added = (fileFromComputer != null)
							  ? backend.addTorrent(fileFromComputer)
							  : backend.addTorrent(urlOrMagnet);
					//put in database
					
					Torrent t = new Torrent();
					t.setHashString(added.getTorrentHash());
					t.setStatus(added.getStatus());
					t.setName(added.getName());
					t.setNode(node);
					t.save();
					UserTorrent ut = new UserTorrent();
					ut.setUser(u);
					ut.setTorrentHash(added.getTorrentHash());
					ut.insert();	
					return null;
				}
			}.now();					  
			successOrError(p);
		}		
		index();
	}
	
	public static void torrentInfo(String hash) {		
		//torrent info is seeders, peers, files, tracker stats
		final Torrent fromDb = Torrent.getByHash(hash);
		Promise<GenericJobResult> p = new GenericJob() {

			@Override
			public Object doGenericJob() {
				//trigger the caching of these objects, inside a job because the WS
				//calls could take ages
				fromDb.getPeers();
				fromDb.getTrackers();
				fromDb.getFiles();
				return fromDb;
			}
			
		}.now();
		ITorrent torrent = (ITorrent) successOrError(p);
		renderTemplate("client/torrent-info.html", torrent);
	}		
	
	public static void action(String what, String hash, List<String> hashes) {
		if (!StringUtils.isEmpty(what)) {
			if (what.equals("start")) {
				doTorrentAction(hash, hashes, TorrentAction.START);
			} else if (what.equals("stop")) {
				doTorrentAction(hash, hashes, TorrentAction.STOP);
			} else if (what.equals("remove")) {
				doTorrentAction(hash, hashes, TorrentAction.REMOVE);
			}
		} else {
			setGeneralErrorMessage("Please specify an 'action'");
		}
		index();
	}
	
	private enum TorrentAction { START, STOP, REMOVE }
	private static void doTorrentAction(final String hash, final List<String> hashes, final TorrentAction action) {
		final User user = getCurrentUser();
		Promise<GenericJobResult> p = new GenericJob() {
			@Override
			public Object doGenericJob() {				
				if (!StringUtils.isEmpty(hash)) {
					UserTorrent ut = UserTorrent.getByUser(user, hash);
					if (ut == null) {
						setGeneralErrorMessage("User has no such torrent with hash: " + hash);
						return null;
					}
					ITorrentBackend backend = ut.getTorrent().getNode().getNodeBackend();			
					switch(action) {
						case START:							
							backend.startTorrent(hash);
							break;
						case STOP:
							backend.stopTorrent(hash);
							break;
						case REMOVE:
							backend.removeTorrent(hash);	
							ut.delete();
							removeTorrentsCompletelyIfRequired(hash);
							break;
					}			
				} else if (hashes != null) {
					List<UserTorrent> utList = UserTorrent.getByUser(user, hashes);
					for (UserTorrent ut : utList) {
						ITorrentBackend backend = ut.getTorrent().getNode().getNodeBackend();
						switch(action) {
							case START:
								backend.startTorrent(hashes);
								break;
							case STOP:
								backend.stopTorrent(hashes);
								break;
							case REMOVE:
								backend.removeTorrent(hashes);
								UserTorrent.batch().delete(utList);
								removeTorrentsCompletelyIfRequired(hashes);
								break;
						}	
					}			
				} else {
					setGeneralErrorMessage("Please specify a 'hash' or 'hashes'");
				}		
				return null;
			}			
		}.now();
		successOrError(p);
	}
	
	private static void removeTorrentsCompletelyIfRequired(String hash) {
		List<String> l = new ArrayList<String>();
		l.add(hash);			
		removeTorrentsCompletelyIfRequired(l);
	}
	
	private static void removeTorrentsCompletelyIfRequired(final List<String> hashList) {
		Promise<GenericJobResult> p = new GenericJob() {
			@Override
			public Object doGenericJob() {
				for(String hash : hashList) {
					if (UserTorrent.getUsersWithTorrentCount(hash) == 0) {
						Torrent to = Torrent.getByHash(hash);
						to.getNode().getNodeBackend().removeTorrent(hash);
						to.delete();
					}
				}
				return null;
			}			
		}.now();
		successOrError(p);
	}	
	
	/*
	public static void startTorrent(String torrentHash) {
		handleTorrentControlRequest(torrentHash, TorrentAction.START);	
	}
	
	public static void startTorrents(String[] torrentHashes) {
		handleTorrentControlRequest(torrentHashes, TorrentAction.START);
	}
	
	public static void pauseTorrent(String torrentHash) {
		handleTorrentControlRequest(torrentHash, TorrentAction.STOP);			
	}
	
	public static void pauseTorrents(String[] torrentHashes) {
		handleTorrentControlRequest(torrentHashes, TorrentAction.STOP);			
	}
	
	public static void removeTorrent(String torrentHash) {
		handleTorrentControlRequest(torrentHash, TorrentAction.REMOVE);
	}	
	
	public static void removeTorrents(String[] torrentHashes) {
		handleTorrentControlRequest(torrentHashes, TorrentAction.REMOVE);
	}	*/
	/*
	public static void addTorrentGroup(String torrentHash, String group) throws MessageException {
		Torrent t = getActiveAccount().getPrimaryUser().getTorrent(torrentHash);
		if (!StringUtils.isEmpty(group)) {
			TorrentGroup temp = new TorrentGroup(group);
			if (!t.groups.contains(temp)) {
				t.groups.add(temp);
			}
			t.save();
		}
		result(true);
	}
	
	public static void addTorrentGroups(String[] torrentHashes, String group) {
		if (torrentHashes != null && torrentHashes.length > 0 && !StringUtils.isEmpty(group)) {
			List<Torrent> all = Torrent.all()
					.filter("user", getActiveAccount().getPrimaryUser())
					.filter("hashString IN", Arrays.asList(torrentHashes)).fetch();
			for(Torrent t : all) {
				TorrentGroup temp = new TorrentGroup(group);
				if (!t.groups.contains(temp)) {
					t.groups.add(temp);
				}
				t.save();			
			}
		}
		result(true);
	}
	
	public static void removeTorrentGroup(String torrentHash, String group) throws MessageException {
		Torrent t = getActiveAccount().getPrimaryUser().getTorrent(torrentHash);
		if (!StringUtils.isEmpty(group)) {
			t.groups.remove(new TorrentGroup(group));
			t.save();
		}
		result(true);		
	}	*/
	
//	public static void update(String group) {
		/*
		Account a = getActiveAccount();
		
		//use a job to prevent tying up server if backend transmission-daemon is being slow
		Promise<GetTorrentsJobResult> job = new GetTorrentsJob(a, group).now();
		GetTorrentsJobResult result = await(job);
		if (result.hasError()) {
			resultError(Util.getStackTrace(result.error));
		}
		List<Torrent> torrents = result.torrents;
		
		//torrent list - changes all the time depending on progress, speed etc
		Map<String, Object> tlist_params = new HashMap<String, Object>();
		tlist_params.put("torrents", torrents);
		tlist_params.put("group", group);
		String tlist = renderToString("client/torrent-list.html", tlist_params);
		
		//user stats - change whenever the torrent-list changes
		Map<String, Object> us_params = new HashMap<String, Object>();
		us_params.put("stats", a.getPrimaryUser().getUserStats());
		String us = renderToString("tags/user-stats.html", us_params);

		//tabs - included here because they may change as part of addGroup/removeGroup calls
		Map<String, Object> ct_params = new HashMap<String, Object>();
		ct_params.put("_groups", a.getPrimaryUser().getTorrentGroups());
		ct_params.put("_active", group);
		String ct = renderToString("tags/client-tabs.html", ct_params);	
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("torrent-list", tlist);
		res.put("user-stats", us);
		res.put("client-tabs", ct);
		result(res);*/
//	}
	

	
	public static void setIncludedTorrentFiles(String torrentHash, String[] fw, String[] fa,
			String[] ph, String[] pn, String[] pl) throws MessageException {/*
		//fw = files wanted, fa = all files, ph = priority high, pn = priority normal, pl = priority low
		Transmission t = getActiveAccount().getPrimaryUser().getTransmission();	
		if (fw != null) {
			t.setFilesWanted(torrentHash, Arrays.asList(fw), Arrays.asList(fa));
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
		result(true);*/
	}
	
	public static void searchIsohunt(String query) {
		if (!StringUtils.isEmpty(query)) {
			Promise<HttpResponse> promise = 
					WS.url("http://ca.isohunt.com/js/json.php?ihq=%s&rows=20&sort=seeds", query).getAsync();
			HttpResponse res = await(promise);
			if (res.getJson() != null) {
				JsonObject itemsObject = res.getJson().getAsJsonObject().getAsJsonObject("items");
				if (itemsObject != null) {
					JsonArray items = itemsObject.getAsJsonArray("list");
					//copy only the parts we need into a map structure
					List<Map<String, String>> results = new ArrayList<Map<String, String>>();
					for (JsonElement i : items) {
						JsonObject it = i.getAsJsonObject();
						Map<String, String> ite = new HashMap<String, String>();
						ite.put("title",  Util.stripHtml(it.get("title").getAsString()));
						ite.put("length", Util.getBestRate(it.get("length").getAsLong()));
						ite.put("seeds", it.get("Seeds").getAsString());
						ite.put("leechers", it.get("leechers").getAsString());
						ite.put("label", String.format("%s (%s) - S: <span style='color:green'>%s</span>, L: <span style='color:red'>%s</span>",
								ite.get("title"), ite.get("length"), ite.get("seeds"), ite.get("leechers")));
						ite.put("torrent_url", it.get("enclosure_url").getAsString());
						results.add(ite);
					}
					renderJSON(results);
				}
			}
		}
		renderJSON(new ArrayList<String>());
	}
	
	protected static Object successOrError(Promise<GenericJobResult> p) {
		GenericJobResult res = await(p);
		if (res.hasError()) {
			if (res.getError() instanceof MessageException) {
				setGeneralErrorMessage(res.getError().getMessage());
				return null;
			}
			Logger.info(res.getError(), "Error occured in job.");
			throw new RuntimeException(res.getError());
		}
		return res.getResult();
	}		
}

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
import java.io.IOException;
import java.util.*;
import models.Node;
import models.Torrent;
import models.User;
import models.UserTorrent;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.binding.As;
import play.libs.F.Promise;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Before;

public class Client extends Base {
	
	@Before(unless={"newUser"})
	public static void checkPlan() {
		User u = getCurrentUser();
		//check that a plan has been purchased		
		if (u.getPlan() == null) {
			newUser();
		}
	}
	
	@Before(unless={"login","auth"})
	public static void before() {	
		User u = getCurrentUser();
		if (u == null) {
			Auth.login();
		}

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
	
	public static void index(String group) {		
		if (group == null) {
			group = "All";
		}
		renderArgs.put("currentGroup", group);
		List<UserTorrent> torrents = getCurrentUser().getTorrentsInGroup(group);
		String torrentList = renderToString("client/torrent-list.html", Util.convertToMap(new Object[] { "torrents", torrents }));
		List<String> groups = getCurrentUser().getGroups();
		render("client/index.html", torrentList, groups, torrents);
	}
	
	public static void update(String group) {
		if (group == null) {
			group = "All";
		}		
		//this is intended to be invoked via ajax
		List<UserTorrent> torrents = getCurrentUser().getTorrentsInGroup(group);
		String data = renderToString("client/torrent-list.html", Util.convertToMap(new Object[] { "torrents", torrents }));
		result(data);
	}
	
	public static void addTorrent(final String urlOrMagnet, final File fileFromComputer) throws IOException {			
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
		index(null);
	}
	
	public static void torrentInfo(String hash) {		
		//torrent info is seeders, peers, files, tracker stats
		final UserTorrent fromDb = UserTorrent.getByUser(getCurrentUser(), hash);
		if (fromDb == null) {
			resultError("No such torrent for user: " + hash);
		}
		Promise<GenericJobResult> p = new GenericJob() {

			@Override
			public Object doGenericJob() {
				//trigger the caching of these objects, inside a job because the WS
				//calls could take ages
				fromDb.getTorrent().getPeers();
				fromDb.getTorrent().getTrackers();
				fromDb.getTorrent().getFiles();
				return fromDb;
			}
			
		}.now();
		UserTorrent torrent = (UserTorrent) successOrError(p);
		renderTemplate("client/torrent-info.html", torrent);
	}	
	
	public static void addGroup(String group) {
		if (!StringUtils.isEmpty(group)) {
			User user = getCurrentUser();
			List<String> groups = user.getGroups();
			if (group.length() > 12) {
				group = group.substring(0, 12);
			}
			groups.add(group);
			user.setGroups(groups);
			user.save();
		} else {
			setGeneralErrorMessage("Please enter a group name.");
		}
		index(null);
	}
	
	public static void removeGroup(String group) {
		if (!StringUtils.isEmpty(group)) {
			User user = getCurrentUser();
			user.getGroups().remove(group);
			user.save();
			UserTorrent.blankOutGroup(user, group);
		}
		index(null);
	}
	
	public static void addToGroup(List<String> hashes, String group) {
		List<UserTorrent> uts = UserTorrent.getByUser(getCurrentUser(), hashes);
		for (UserTorrent ut : uts) {
			ut.setGroupName(group);
		}
		UserTorrent.batch().update(uts);
		index(group);
	}
	
	public static void removeFromGroup(String group) {
		UserTorrent.blankOutGroup(getCurrentUser(), group);
		index(null);
	}
	
	public static void action(String what, String hash, @As(",") List<String> hashes) {
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
		index(null);
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
						throw new MessageException("User has no such torrent with hash: " + hash);						
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
								backend.startTorrent(ut.getTorrentHash());
								break;
							case STOP:
								backend.stopTorrent(ut.getTorrentHash());
								break;
							case REMOVE:
								backend.removeTorrent(ut.getTorrentHash());								
								break;
						}	
					}
					if (action == TorrentAction.REMOVE) {
						UserTorrent.batch().delete(utList);
						removeTorrentsCompletelyIfRequired(hashes);
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
	
	private static void removeTorrentsCompletelyIfRequired(List<String> hashList) {
		//called within a job, and you cant have nested jobs or await() will never work
		for(String hash : hashList) {
			if (UserTorrent.getUsersWithTorrentCount(hash) == 0) {
				Torrent to = Torrent.getByHash(hash);
				to.getNode().getNodeBackend().removeTorrent(hash);
				to.delete();
			}
		}
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
		if (p == null) { throw new IllegalArgumentException("You cant give me a null promise!"); }
		GenericJobResult res = await(p);
		if (res.hasError()) {
			if (res.getError() instanceof MessageException) {
				setGeneralErrorMessage(res.getError().getMessage());
				return null;
			}
			if (res.getError().getMessage().contains("Connection refused")) {
				setGeneralErrorMessage("Unable to connect to backend! The administrators have been notified.");
				//TODO: send error email
				return null;
			}
			Logger.info(res.getError(), "Error occured in job.");
			throw new RuntimeException(res.getError());
		}
		return res.getResult();
	}
	
	public static void newUser() {
		render("client/new-user.html");
	}
	
	public static void downloadButton(String hash) {
		final UserTorrent fromDb = UserTorrent.getByUser(getCurrentUser(), hash);
		if (fromDb == null) {
			resultError("No such torrent for user: " + hash);
		}
		Promise<GenericJobResult> p = new GenericJob() {
			@Override
			public Object doGenericJob() {
				fromDb.getTorrent().getFiles();
				return fromDb;
			}			
		}.now();
		UserTorrent torrent = (UserTorrent) successOrError(p);		
		render("client/download-button.html", torrent);
	}
}

package controllers;

import code.MessageException;
import code.Util;
import code.jobs.AddTorrentJob;
import code.jobs.AddTorrentJob.AddTorrentJobResult;
import code.jobs.GetTorrentsJob;
import code.jobs.GetTorrentsJob.GetTorrentsJobResult;
import code.jobs.TorrentControlJob;
import code.jobs.TorrentControlJob.TorrentAction;
import code.jobs.TorrentControlJob.TorrentControlJobResult;
import code.transmission.Transmission;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.*;
import models.Account;
import models.Torrent;
import models.Torrent.TorrentGroup;
import models.User;
import models.UserMessage;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Validation;
import play.libs.F.Promise;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Before;

public class ClientController extends BaseController {
	
	@Before(unless={"login","auth"})
	public static void before() {	
		User u = getCurrentUser();
		if (u == null) {
			AuthController.login();
		}
		//check that a plan has been purchased
		if (u.getPrimaryAccount().getPlan() == null) {
			AccountController.index();
		}

		//check that limits have not been exceeded. if they have, pause all the torrents and notify user
		try {
			if (u.hasExceededLimits()) {
				List<Torrent> running = u.getRunningTorrents();
				List<String> hashes = new ArrayList<String>();
				for (Torrent t : running) {
					hashes.add(t.hashString);
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
		}		
	}
	
	public static void index() {		
		render("client/index.html");
	}
	
	public static void dismissMessage(Long messageId) {
		UserMessage um = UserMessage.getByKey(messageId);
		if (um != null) {
			um.dismissDateUtc = new Date();
			um.save();
		}
		result(true);
	}
	
	public static void addTorrent(String urlOrMagnet, File fileFromComputer) {
		if (StringUtils.isEmpty(urlOrMagnet) && fileFromComputer == null) {
			setGeneralErrorMessage("Please enter a valid URL or magent link, or choose a valid file to upload.");
		} else {
			Account a = getActiveAccount();
			Promise<AddTorrentJobResult> p = new AddTorrentJob(a, urlOrMagnet, fileFromComputer).now();
			AddTorrentJobResult result = await(p);
			if (result.hasError()) {
				addGeneralError(result.error);
			}
		}
		Validation.keep();
		index();
	}
	
	public static void setActiveAccount(Long id, String returnTo) {
		session.put("activeAccountId", id);
		Cache.delete(getActiveAccountCacheKey()); //remove old active account from cache
		//Account a = getActiveAccount();
		//setGeneralMessage("Account changed to " + a.getDisplayName() + ".");
		redirect(returnTo);
	}
	
	
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
	}	
	
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
	}	
	
	public static void update(String group) {
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
		result(res);
	}
	
	public static void renderTorrentInfo(String torrentHash) throws MessageException {
		//torrent info is seeders, peers, files, tracker stats
		Promise<GetTorrentsJobResult> job = new GetTorrentsJob(getActiveAccount(), null, torrentHash).now();
		GetTorrentsJobResult res = await(job);
		Torrent torrent = res.torrents.get(0);
		renderTemplate("client/torrent-info.html", torrent);
	}
	
	public static void setIncludedTorrentFiles(String torrentHash, String[] fw, String[] fa,
			String[] ph, String[] pn, String[] pl) throws MessageException {
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
		result(true);
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
	
	protected static void handleTorrentControlRequest(String torrentHash, TorrentAction action) {
		handleTorrentControlRequest(new String[] { torrentHash }, action);
	}
	
	protected static void handleTorrentControlRequest(String[] torrentHashes, TorrentAction action) {
		TorrentControlJobResult res = runTorrentControlJob(Arrays.asList(torrentHashes), action);
		if (res.hasError()) {
			resultError(Util.getStackTrace(res.error));
		}
		result(res.success);	
	}
	
	protected static TorrentControlJobResult runTorrentControlJob(String torrentHash, TorrentAction action) {
		return runTorrentControlJob(Arrays.asList(new String[] { torrentHash }), action);
	}	
	
	protected static TorrentControlJobResult runTorrentControlJob(List<String> torrentHashes, TorrentAction action) {
		Promise<TorrentControlJobResult> tcj = new TorrentControlJob(getActiveAccount(), torrentHashes, action).now();
		TorrentControlJobResult res = await(tcj);
		return res;
	}
	
	
		
}

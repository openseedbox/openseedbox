package controllers;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.openseedbox.Config;
import com.openseedbox.code.MessageException;
import com.openseedbox.code.Util;
import com.openseedbox.jobs.GenericJob;
import com.openseedbox.jobs.GenericJobResult;
import com.openseedbox.jobs.torrent.*;
import com.openseedbox.models.*;
import com.openseedbox.models.TorrentEvent.TorrentEventType;
import com.openseedbox.plugins.OpenseedboxPlugin;
import com.openseedbox.plugins.OpenseedboxPlugin.PluginSearchResult;
import com.openseedbox.plugins.PluginManager;

import play.Logger;
import play.cache.Cache;
import play.data.binding.As;
import play.i18n.Messages;
import play.libs.F.Promise;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Before;

public class Client extends Base {
	
	public static final String CLIENT_INDEX_VIEW_ACTIVE = "client.index.view.active";

	@Before(unless={"newUser"})
	public static void checkPlan() {
		User u = getCurrentUser();
		//check that a plan has been purchased

		if (u != null && u.getPlan() == null) {
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
		if (u.hasExceededLimits()) {
			List<UserTorrent> running = u.getRunningTorrents();
			for (UserTorrent ut : running) {
				new StartStopTorrentJob(ut.getTorrentHash(), TorrentAction.STOP, u.getId()).now();
				ut.setPaused(true);
			}
			UserTorrent.batch().update(running);
			setGeneralErrorMessage("You have exceeded your plan limits! All your torrents will be paused until you free up some space.");
		}
	}
	
	public static void index(String group, String view) {
		if (!StringUtils.isEmpty(group)) {
			setCurrentGroupName(group);
		}
		renderArgs.put("users", Util.toSelectItems(User.all().fetch(), "id", "emailAddress"));
		User user = getCurrentUser();
		List<UserTorrent> torrents;
		String torrentList;
		if (Objects.equals(view, Messages.get(CLIENT_INDEX_VIEW_ACTIVE))) {
			renderArgs.put("currentView", CLIENT_INDEX_VIEW_ACTIVE);
			torrents = user.getActiveTorrents();
			torrentList = renderTorrentList(user, torrents);
		} else {
			group = getCurrentGroupName();
			renderArgs.put("currentGroup", group);
			torrents = user.getTorrentsInGroup(group);
			torrentList = renderTorrentList(group);
		}
		List<String> groups = user.getGroups();
		List<OpenseedboxPlugin> searchPlugins = PluginManager.getSearchPlugins();
		List<UserMessage> userMessages = UserMessage.retrieveForUser(user);		
		renderTemplate("client/index.html", torrentList, groups, torrents, searchPlugins, userMessages);
	}

	private static void index(String group) {
		index(group, null);
	}
	
	public static void update(String group, String view) {
		//this is intended to be invoked via ajax		
		User user = getCurrentUser();
		List<UserMessage> messages = UserMessage.retrieveForUser(user);
		List<Object> messagesAsObjects = new ArrayList<Object>();
		for (UserMessage um : messages) {
			messagesAsObjects.add(Util.convertToMap(new Object[] {
				"state", um.getState(),
				"heading", um.getHeading(),
				"message", um.getMessage()
			}));
		}
		result(Util.convertToMap(new Object[] {
			"torrent-list", Objects.equals(view, Messages.get(CLIENT_INDEX_VIEW_ACTIVE)) ?
				renderTorrentList(user, user.getActiveTorrents()) : renderTorrentList(group),
			"user-messages", messagesAsObjects
		}));
	}
	
	private static String renderTorrentList(String group) {	
		if (StringUtils.isEmpty(group)) {
			group = "Ungrouped";
		}
		User user = getCurrentUser();
		List<UserTorrent> torrents = user.getTorrentsInGroup(group);
		return renderTorrentList(user, torrents);
	}

	private static String renderTorrentList(User user, List<UserTorrent> torrents) {
		List<TorrentEvent> torrentAddEvents = TorrentEvent.getIncompleteForUser(user, TorrentEventType.ADDING);
		List<TorrentEvent> torrentRemoveEvents = TorrentEvent.getIncompleteForUser(user, TorrentEventType.REMOVING);
		return renderToString("client/torrent-list.html", Util.convertToMap(
				  new Object[] { "torrents", torrents, "torrentAddEvents", torrentAddEvents, "torrentRemoveEvents", torrentRemoveEvents }));
	}
	
	public static void addTorrent(@As("\n") final String[] urlOrMagnet, final File[] fileFromComputer) throws IOException {					
		boolean hasError = false;
		if (urlOrMagnet == null && fileFromComputer == null) {
			hasError = true;
		} else {					
			int count = 0;
			User user = getCurrentUser();
			if (urlOrMagnet != null) {
				for (String s : urlOrMagnet) {					
					if (!s.isEmpty()) {
						new AddTorrentJob(s, null, user.getId(), getCurrentGroupName()).now();
						count++;
					}
				}
			}
			if (fileFromComputer != null) {
				for (File f : fileFromComputer) {
					//copy the file to somewhere more permanent because Play! deletes it when the action completes so the Job cant use it					
					File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".torrent");
					FileUtils.copyFile(f, tempFile);
					new AddTorrentJob(null, tempFile, user.getId(), getCurrentGroupName()).now();
					count++;
				}
			}	
			if (count > 0) {
				if (count > 1) {
					setGeneralMessage(count + " torrents have been scheduled for downloading! They will begin shortly.");
				} else {
					setGeneralMessage("Your torrent has been scheduled for downloading! It will begin shortly.");
				}
			} else {
				hasError = true;
			}
		}				
		if (hasError) {
			setGeneralErrorMessage("Please enter a valid URL or magent link, or choose a valid file to upload.");
		}
		index(getCurrentGroupName());
	}
	
	public static void search(String query, String providerClass) {
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		try {
			OpenseedboxPlugin provider = (OpenseedboxPlugin) Class.forName(providerClass).newInstance();
			if (provider.isSearchPlugin()) {
				List<PluginSearchResult> res = provider.doSearch(query);
				for (PluginSearchResult psr : res) {
					ret.add(Util.convertToMap(new Object[] {
						"label", String.format("%s", psr.getTorrentName()),
						"url", psr.getTorrentUrl()
					}));
				}
			}
		} catch (ClassNotFoundException ex) {
			resultError("Unable to find class: " + providerClass);
		} catch (InstantiationException ex) {
			Logger.error(ex, "Unable to instantiate class: %s", providerClass);
		} catch (IllegalAccessException ex) {
			Logger.error(ex, "Unable to instantiate class: %s", providerClass);
		}
		renderJSON(ret);
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
		GenericJobResult res = await(p);
		if (res.hasError()) {
			resultError(res.getError());
		}
		UserTorrent torrent = (UserTorrent) res.getResult();
		renderTemplate("client/torrent-info.html", torrent);
	}	
	
	public static void torrentDownload(String hash, String type) {
		//torrent download is just for files
		final UserTorrent fromDb = UserTorrent.getByUser(getCurrentUser(), hash);
		if (fromDb == null) {
			resultError("No such torrent for user: " + hash);
		}
		if (StringUtils.equalsIgnoreCase(type, "zip")) {
			Promise<GenericJobResult> p = new GenericJob() {
				@Override
				protected Object doGenericJob() throws Exception {
					Node n = fromDb.getTorrent().getNode();
					HttpResponse res = WS.url(fromDb.getTorrent().getZipDownloadLink()).get();
					JsonObject result = n.handleWebServiceResponse(res).getAsJsonObject();
					JsonElement dl = result.get("download-link");
					String downloadLink = (dl != null) ? dl.getAsString() : null;
					return Util.convertToMap(new Object[] {
						"percent-complete", result.get("percent-complete").getAsString(),
						"download-link", downloadLink
					});
				}				
			}.now();
			GenericJobResult res = await(p);
			if (res.hasError()) {
				resultError(res.getError());
			}
			result(res.getResult());
		} else {
			Promise<GenericJobResult> p = new GenericJob() {
				@Override
				public Object doGenericJob() {
					fromDb.getTorrent().getFiles();
					return fromDb;
				}	
			}.now();
			GenericJobResult res = await(p);
			if (res.hasError()) {
				resultError(res.getError());
			}
			UserTorrent torrent = (UserTorrent) res.getResult();
			renderTemplate("client/torrent-download.html", torrent);
		}
	}		
	
	public static void addGroup(String group) {
		if (!StringUtils.isBlank(group)) {
			User user = getCurrentUser();
			List<String> groups = user.getGroups();
			if (group.length() > 12) {
				group = group.substring(0, 12);
			}
			if (groups.contains(group)) {
				setGeneralErrorMessage("Group '" + group + "' already exists!");
			} else {
				groups.add(group);
				user.setGroups(groups);
				user.save();
				Account.uncacheUser();
			}
		} else {
			setGeneralErrorMessage("Please enter a group name.");
		}
		index(getCurrentGroupName());
	}
	
	public static void removeGroup(String group) {
		if (!StringUtils.isBlank(group)) {
			User user = getCurrentUser();
			user.removeTorrentGroup(group);
			UserTorrent.blankOutGroup(user, group);
			Account.uncacheUser();
		} else {
			setGeneralErrorMessage("Please enter a group to remove.");
		}
		String currentGroup = getCurrentGroupName();
		if (!currentGroup.equals(group)) {
			index(getCurrentGroupName());
		}
		index(null);
	}
	
	public static void addToGroup(@As(",") List<String> hashes, String group, String new_group) {
		User user = getCurrentUser();
		List<UserTorrent> uts = UserTorrent.getByUser(getCurrentUser(), hashes);
		if (!StringUtils.isEmpty(new_group)) {
			if (new_group.length() > 12) {
				new_group = new_group.substring(0, 12);
			}
			user.addTorrentGroup(new_group);
		}
		String groupName = (!StringUtils.isEmpty(new_group)) ? new_group : group;
		if (!getCurrentGroupName().equals(groupName)) {
			for (UserTorrent ut : uts) {
				if (groupName.equals(User.TORRENT_GROUP_UNGROUPED)) {
					ut.setGroupName(null);
				} else {
					ut.setGroupName(groupName);
				}
			}
			UserTorrent.batch().update(uts);
			Account.uncacheUser();
		}
		index(groupName);
	}
	
	public static void removeFromGroup(String group) {
		UserTorrent.blankOutGroup(getCurrentUser(), group);
		index(group);
	}
	
	public static void action(String what, String hash, @As(",") List<String> hashes) {
		if (!StringUtils.isEmpty(hash)) { hashes = new ArrayList<String>(); }
		if (hashes.isEmpty()) {
			if (StringUtils.isEmpty(hash)) {
				setGeneralErrorMessage("Please specify a 'hash' or 'hashes'");
			}
			hashes.add(hash);		
		}		
		if (!hashes.isEmpty() && !StringUtils.isEmpty(what)) {
			if (what.equals("start")) {
				doTorrentAction(hashes, TorrentAction.START);
			} else if (what.equals("stop")) {
				doTorrentAction(hashes, TorrentAction.STOP);
			} else if (what.equals("remove")) {
				doTorrentAction(hashes, TorrentAction.REMOVE);
			}
		} else {
			setGeneralErrorMessage("Please specify an 'action'");
		}			
		index(getCurrentGroupName());
	}
	
	public enum TorrentAction { START, STOP, REMOVE }
	private static void doTorrentAction(List<String> hashes, TorrentAction action) {
		User user = getCurrentUser();						
		if (action == TorrentAction.REMOVE) {
			for (String h : hashes) {
				if (StringUtils.isEmpty(h)) {
					continue;
				}
				new RemoveTorrentJob(h, user.getId()).now();
			}
			if (hashes.size() > 1) {
				setGeneralMessage(hashes.size() + " torrents are now scheduled for deletion.");
			} else {
				setGeneralMessage("This torrent is now scheduled for deletion.");
			}			
		} else {
			for (String hash : hashes) {
				UserTorrent ut = UserTorrent.getByUser(user, hash);
				if (ut == null) {
					throw new MessageException("User has no such torrent with hash: " + hash);						
				}					
				ut.setPaused(action == TorrentAction.STOP);
				ut.setRunning(action == TorrentAction.START);
				ut.save(); //so client updates instantly
				new StartStopTorrentJob(hash, action, user.getId()).now();				
			}			
		}		
	}
	
	protected static Object successOrError(Promise<GenericJobResult> p) {
		if (p == null) { throw new IllegalArgumentException("You cant give me a null promise!"); }
		GenericJobResult res = await(p);
		if (res == null) { return null; }
		if (res.hasError()) {
			if (res.getError() instanceof MessageException) {
				setGeneralErrorMessage(res.getError());
				return null;
			}
			if (StringUtils.contains(res.getError().getMessage(), "Connection refused")) {
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
	
	public static void switchUser(long user_id) {
		User u = getCurrentUser();
		if (!u.isAdmin()) {
			resultError("You have to be admin to do this!");
		}
		session.put("currentUserId", user_id);
		Cache.clear();
		index(null);
	}
	
	public static void downloadMultiple(@As(",") List<String> hashes, String debug) {
		if (!Config.isZipEnabled()) {
			notFound("Zip has been disabled.");
		}
		//Since the torrents are spread out over multiple nodes, we cant zip up multiple torrents into a single zip at the node level
		//Basically, we send HEAD requests to get the Content-Length, and then point nginx's mod_zip to an internal route that makes nginx fetch the torrent zip files from the upstream server
		if (hashes == null || hashes.isEmpty()) {
			notFound("Please specify some hashes.");
		}
		String all = "";
		for (Torrent t : Torrent.getByHash(hashes)) {
			String link = t.getZipDownloadLink();			
			String len = WS.url(link).head().getHeader("Content-Length");
			//strip scheme and replace localhost with 127.0.0.1 so nginx resolver doesnt time out
			link = link.replace("http://", "").replace("https://", "").replace("localhost", "127.0.0.1");
			link += "&scheme=" + t.getNode().getScheme(); //note: we specify scheme here to help nginx incase the frontend is running on http and the backend is running on https
			//no CRC32 because theres no way of knowing the CRC32 of the upstream zipfiles without downloading them first
			all += String.format("- %s %s/%s /%s\n", len, Config.getZipPath(), link, t.getName() + ".zip");
		}
		if (!Config.isZipManifestOnly() && debug == null) {
			response.setHeader("X-Archive-Files", "zip"); //tell NGINX to create us a zip
			response.setHeader("Content-Disposition", "attachment; filename=\"" + UUID.randomUUID().toString() + ".zip" + "\"");
		}
		response.setHeader("Last-Modified", Util.getLastModifiedHeader(System.currentTimeMillis()));
		renderText(all);
	}
	
	//intended to be called via ajax
	public static void updateGroupOrder(List<String> newOrder) {
		if (newOrder != null && !newOrder.isEmpty()) {
			User u = getCurrentUser();
			u.setGroups(newOrder);
			u.save();
			Account.uncacheUser();
		}
		result(Util.convertToMap(new Object[] {
			"success", true
		}));
	}
	
	private static void setCurrentGroupName(String group) {
		Cache.set(getCurrentGroupNameCacheKey(), group, "3d");	
	}
	
	private static String getCurrentGroupName() {
		String name = Cache.get(getCurrentGroupNameCacheKey(), String.class);
		if (StringUtils.isEmpty(name)) {
			return User.TORRENT_GROUP_UNGROUPED;
		}
		return name;
	}
	
	private static String getCurrentGroupNameCacheKey() {
		return session.getId() + "_currentGroupName";
	}
}

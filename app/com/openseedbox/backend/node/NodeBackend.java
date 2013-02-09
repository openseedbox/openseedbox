package com.openseedbox.backend.node;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.openseedbox.backend.IFile;
import com.openseedbox.backend.IPeer;
import com.openseedbox.backend.ISessionStatistics;
import com.openseedbox.backend.ITorrent;
import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.backend.ITracker;
import com.openseedbox.backend.TorrentState;
import com.openseedbox.code.MessageException;
import com.openseedbox.code.Util;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openseedbox.models.Node;
import com.openseedbox.models.Torrent;
import play.libs.WS.FileParam;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;

/**
 * Essentially this is a "pass-thru" torrent backend that delegates
 * directly to the openseedbox-server API (for whatever node this class
 * was initialised with).
 * The openseedbox-server API then uses this same interface to interact with
 * whatever backend its using
 * @author Erin Drummond
 */
public class NodeBackend implements ITorrentBackend {
	
	private Node node;
	
	public NodeBackend(Node node) {
		this.node = node;
	}

	public boolean isInstalled() {
		return node.getNodeStatus().isBackendInstalled();
	}

	public String getName() {
		return "openseedbox-node";
	}

	public String getVersion() {
		return "0.1";
	}

	public void start() {		
		HttpResponse res = node.getWebService("/backend/start").get();
		getResponseBodyOrError(res);		
	}

	public void stop() {
		HttpResponse res = node.getWebService("/backend/stop").get();
		getResponseBodyOrError(res);
	}

	public void restart() {
		HttpResponse res = node.getWebService("/backend/restart").get();
		getResponseBodyOrError(res);
	}
	
	public void cleanup() {
		HttpResponse res = node.getWebService("/backend/cleanup").get();
		getResponseBodyOrError(res);
	}

	public boolean isRunning() {
		return node.getNodeStatus().isBackendRunning();
	}

	public ITorrent addTorrent(File file) {
		return add(file, null);
	}

	public ITorrent addTorrent(String urlOrMagnet) {
		return add(null, urlOrMagnet);
	}
	
	private ITorrent add(File file, String urlOrMagnet) {
		WSRequest req = node.getWebService("/torrents/add");
		req.timeout("5min"); //some torrents can take ages to add due to the encryption and having to be allocated
		HttpResponse res;
		if (file != null) {
			FileParam fp = new FileParam(file, "torrent");
			req.files(fp);
			res = req.post();
		} else {
			req.setParameter("url", urlOrMagnet);
			res = req.get();
		}
		JsonObject ob = getResponseBodyOrError(res).getAsJsonObject();		
		return Util.getGson().fromJson(ob.get("torrent"), NodeTorrent.class);		
	}

	public void removeTorrent(String hash) {
		WSRequest req = node.getWebService("/torrents/remove", hash);		
		getResponseBodyOrError(req.get());		
	}

	public void removeTorrent(List<String> hashes) {
		WSRequest req = node.getWebService("/torrents/remove", hashes);		
		getResponseBodyOrError(req.get());			
	}

	public void startTorrent(String hash) {		
		WSRequest req = node.getWebService("/torrents/start", hash);		
		getResponseBodyOrError(req.get());
		ITorrent status = getTorrentStatus(hash);
		if (status.isSeeding()) {
			updateDatabaseTorrent(hash, TorrentState.SEEDING);
		} else if (status.isMetadataDownloading()) {
			updateDatabaseTorrent(hash, TorrentState.METADATA_DOWNLOADING);
		} else {
			updateDatabaseTorrent(hash, TorrentState.DOWNLOADING);
		}
	}

	public void startTorrent(List<String> hashes) {
		WSRequest req = node.getWebService("/torrents/start", hashes);		
		getResponseBodyOrError(req.get());	
		List<ITorrent> status = getTorrentStatus(hashes);
		List<String> metadataHashes = new ArrayList<String>();
		List<String> normalHashes = new ArrayList<String>();
		List<String> seedingHashes = new ArrayList<String>();
		for (ITorrent it : status) {
			String hash = it.getTorrentHash();
			if (it.isSeeding()) {
				seedingHashes.add(hash);
			} else if (it.isMetadataDownloading()) {
				metadataHashes.add(hash);
			} else {
				normalHashes.add(hash);
			}		
		}	
		updateDatabaseTorrent(metadataHashes, TorrentState.METADATA_DOWNLOADING);
		updateDatabaseTorrent(normalHashes, TorrentState.DOWNLOADING);
		updateDatabaseTorrent(seedingHashes, TorrentState.SEEDING);
	}

	public void stopTorrent(String hash) {
		WSRequest req = node.getWebService("/torrents/stop", hash);		
		getResponseBodyOrError(req.get());
		updateDatabaseTorrent(hash, TorrentState.PAUSED);
	}

	public void stopTorrent(List<String> hashes) {
		WSRequest req = node.getWebService("/torrents/stop", hashes);		
		getResponseBodyOrError(req.get());	
		updateDatabaseTorrent(hashes, TorrentState.PAUSED);
	}

	public ITorrent getTorrentStatus(String hash) {
		List<String> list = new ArrayList<String>();
		list.add(hash);
		return getTorrentStatus(list).get(0);
	}

	public List<ITorrent> getTorrentStatus(List<String> hashes) {
		WSRequest req = node.getWebService("/torrents/status", hashes);			
		JsonObject body = getResponseBodyOrError(req.get()).getAsJsonObject();		
		JsonArray list = body.getAsJsonArray("status");			
		return parseToList(list);
	}

	public List<IPeer> getTorrentPeers(String hash) {
		return getTorrentPeers(Arrays.asList(new String[] { hash })).get(hash);
	}

	public Map<String, List<IPeer>> getTorrentPeers(List<String> hashes) {
		WSRequest req = node.getWebService("/torrents/peers", hashes);
		JsonObject peers = getResponseBodyOrError(req.get()).getAsJsonObject().getAsJsonObject("peers");
		java.lang.reflect.Type listType = new TypeToken<HashMap<String, ArrayList<NodePeer>>>() {}.getType();
		Map<String, List<IPeer>> ret = Util.getGson().fromJson(peers, listType);
		return ret;
	}

	public List<ITracker> getTorrentTrackers(String hash) {
		return getTorrentTrackers(Arrays.asList(new String[] { hash })).get(hash);
	}

	public Map<String, List<ITracker>> getTorrentTrackers(List<String> hashes) {
		WSRequest req = node.getWebService("/torrents/trackers", hashes);
		JsonObject files = getResponseBodyOrError(req.get()).getAsJsonObject().getAsJsonObject("trackers");
		java.lang.reflect.Type listType = new TypeToken<HashMap<String, ArrayList<NodeTracker>>>() {}.getType();
		Map<String, List<ITracker>> ret = Util.getGson().fromJson(files, listType);
		return ret;	
	}

	public List<IFile> getTorrentFiles(String hash) {
		return getTorrentFiles(Arrays.asList(new String[] { hash })).get(hash);
	}

	public Map<String, List<IFile>> getTorrentFiles(List<String> hashes) {
		WSRequest req = node.getWebService("/torrents/files", hashes);
		JsonObject files = getResponseBodyOrError(req.get()).getAsJsonObject().getAsJsonObject("files");
		java.lang.reflect.Type listType = new TypeToken<HashMap<String, ArrayList<NodeFile>>>() {}.getType();
		Map<String, List<IFile>> ret = Util.getGson().fromJson(files, listType);
		for (String hash : ret.keySet()) {
			List<IFile> li = ret.get(hash);
			for (IFile f : li) {
				NodeFile nf = (NodeFile) f;
				nf.setNode(this.node);
				nf.setTorrentHash(hash);
			}
		}
		return ret;		
	}

	public void modifyTorrentFiles(String hash, List<IFile> files) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void modifyTorrent(String hash, double seedRatio, long uploadLimitBytes, long downloadLimitBytes) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public List<ITorrent> listTorrents() {
		return list(false);
	}

	public List<ITorrent> listRecentlyActiveTorrents() {
		return list(true);
	}
	
	public ISessionStatistics getSessionStatistics() {
		throw new UnsupportedOperationException("Not supported yet.");
	}	
	
	private List<ITorrent> list(boolean recentlyActive) {
		WSRequest req = node.getWebService("/torrents/list");
		if (recentlyActive) {
			req.setParameter("recentlyActive", true);
		}
		JsonArray body = getResponseBodyOrError(req.get()).getAsJsonArray();
		return parseToList(body);
	}
	
	private List<ITorrent> parseToList(JsonArray list) {
		java.lang.reflect.Type listType = new TypeToken<ArrayList<NodeTorrent>>() {}.getType();
		List<NodeTorrent> ret = Util.getGson().fromJson(list, listType);
		for (NodeTorrent nt : ret) {
			nt.setNode(this.node);
		}
		return new ArrayList<ITorrent>(ret);			
	}

	/* this exists so that the database (and therefore, the UI) is updated immediately when there is a state change
	 * instead of the user waiting 10 seconds for it to take effect when the next node poll occurs
	 * Also, some actions eg 'pausing' a torrent actually take a while to complete,
	 * so forceState instantly updates the state so the UI can be updated right away
	 */
	private void updateDatabaseTorrent(String hash, TorrentState forceState) {
		List<String> hashes = new ArrayList<String>();
		hashes.add(hash);
		updateDatabaseTorrent(hashes, forceState);
	}
	
	private void updateDatabaseTorrent(List<String> hashes, TorrentState forceState) {		
		List<Torrent> torrents = Torrent.getByHash(hashes);
		for (Torrent t : torrents) {			
			t.setStatus(forceState);									
		}
		Torrent.batch().update(torrents);
	}	
	
	private JsonElement getResponseBodyOrError(HttpResponse res) {
		if (res.success()) {							
			try {
				JsonObject ob = res.getJson().getAsJsonObject();
				if (ob.has("error")) {
					throw new MessageException(ob.get("error").getAsString());
				}
				return ob.get("data");
			} catch (Exception ex) {
				throw new MessageException(ex.getMessage());
			}
		}
		throw new MessageException(res.getStatusText());
	}
	
}

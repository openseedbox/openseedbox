package com.openseedbox.backend.node;

import com.google.gson.JsonObject;
import com.openseedbox.backend.IFile;
import com.openseedbox.backend.IPeer;
import com.openseedbox.backend.ISessionStatistics;
import com.openseedbox.backend.ITorrent;
import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.backend.ITracker;
import com.openseedbox.code.MessageException;
import java.io.File;
import java.util.List;
import java.util.Map;
import models.Node;
import play.libs.WS.HttpResponse;

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

	public boolean isRunning() {
		return node.getNodeStatus().isBackendRunning();
	}

	public ITorrent addTorrent(File file) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ITorrent addTorrent(String urlOrMagnet) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void removeTorrent(String hash) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void removeTorrent(List<String> hashes) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void startTorrent(String hash) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void startTorrent(List<String> hashes) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void stopTorrent(String hash) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void stopTorrent(List<String> hashes) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ITorrent getTorrentStatus(String hash) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public List<ITorrent> getTorrentStatus(List<String> hashes) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public List<IPeer> getTorrentPeers(String hash) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Map<String, List<IPeer>> getTorrentPeers(List<String> hashes) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public List<ITracker> getTorrentTrackers(String hash) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Map<String, List<ITracker>> getTorrentTrackers(List<String> hashes) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public List<IFile> getTorrentFiles(String hash) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Map<String, List<IFile>> getTorrentFiles(List<String> hashes) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void modifyTorrentFiles(String hash, List<IFile> files) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void modifyTorrent(String hash, double seedRatio, long uploadLimitBytes, long downloadLimitBytes) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public List<ITorrent> listTorrents() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public List<ITorrent> listRecentlyActiveTorrents() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ISessionStatistics getSessionStatistics() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	protected JsonObject getResponseBodyOrError(HttpResponse res) {
		if (res.success()) {							
			try {
				JsonObject ob = res.getJson().getAsJsonObject();
				if (ob.has("error")) {
					throw new MessageException(ob.get("error").getAsString());
				}
				return ob.getAsJsonObject("data");
			} catch (Exception ex) {
				throw new MessageException(ex.getMessage());
			}
		}
		throw new MessageException(res.getStatusText());
	}
	
}

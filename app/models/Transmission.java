package models;

import code.MessageException;
import code.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import play.Logger;
import play.modules.siena.EnhancedModel;
import siena.*;

@Table("transmission")
public class Transmission extends EnhancedModel {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	private Node node;
	
	@Column("port")
	public int port;
	
	@Column("config_file_location")
	public String configFileLocation = "/etc/transmission-daemon/settings.json";
	
	public Node getNode() {
		return Model.all(Node.class).getByKey(node.id);
	}

	public TransmissionConfig getConfig() throws MessageException {
		try {
			String contents = getNode().readFile("/etc/transmission-daemon/settings.json");
			Gson g = new GsonBuilder().create();
			return g.fromJson(contents, TransmissionConfig.class);
			//return (JSONObject) JSONValue.parse(contents);
		} catch (JSchException | SftpException ex) {
			Logger.error(ex, "Unable to get transmission config");
			throw new MessageException(ex.toString());
		}
	}

	public void start() {
		if (!isRunning()) {
			Node n = getNode();
			Logger.debug("Starting transmission-daemon on node %s", n.name);
			n.executeSsh("/etc/init.d/transmission-daemon start");
		}
	}

	public void stop() {
		if (isRunning()) {
			Node n = getNode();
			Logger.debug("Stopping transmission-daemon on node %s", n.name);
			n.executeSsh("/etc/init.d/transmission-daemon stop");
		}
	}

	public Boolean isRunning() {
		Node n = getNode();
		String test = n.executeSsh("ps -A | grep transmission-da");
		return (!test.isEmpty());
	}

	public void reloadConfig() {
		if (isRunning()) {
			Node n = getNode();
			Logger.debug("Reloading transmission-daemon config on node %s", n.name);
			n.executeSsh("killall -HUP transmission-daemon");
		}
	}
	
	/* RPC method wrappers */
	public Torrent addTorrent(String urlOrMagnet) throws MessageException {
		return addTorrent(urlOrMagnet, false);
	}
	
	public Torrent addTorrent(String urlOrMagnet, Boolean paused) throws MessageException {
		RpcRequest req = new RpcRequest("torrent-add");
		req.arguments.put("filename", urlOrMagnet);
		req.arguments.put("paused", paused);
		RpcResponse res = executeRpc(req);
		if (res.success) {
			JSONObject addedTorrent = (JSONObject) res.arguments.get("torrent-added");
			String hash = addedTorrent.get("hashString").toString();
			String name = addedTorrent.get("name").toString();
			Torrent ret = new Torrent();
			ret.hashString = hash;
			ret.name = name;
			return ret;
		} else {
			throw new MessageException("Unable to add torrent: " + res.errorMessage);
		}
	}	
	
	public Boolean removeTorrent(String id, Boolean torrentOnly) throws MessageException {
		List<String> l = new ArrayList<>();
		l.add(id);
		return removeTorrent(l, torrentOnly);
	}
	
	public Boolean removeTorrent(List<String> ids, Boolean torrentOnly) throws MessageException {
		RpcRequest r = new RpcRequest("torrent-remove", ids);
		/* convert the List<String> into a List<Object>. Transmission-daemon wont
		 * remove torrents by id if the submitted id is a string and not an int.
		 * However, we might also want to remove torrents by hash in the same request.
		 * Therefore, we need both strings and ints to be in the ids array */
		r.arguments.put("delete-local-data", !torrentOnly);
		RpcResponse res = executeRpc(r);
		return res.successOrExcept();
	}
	
	public Boolean startTorrent(String torrentHash) throws MessageException {	
		List<String> s = new ArrayList<>();
		s.add(torrentHash);
		return startTorrents(s);
	}
	
	public Boolean startTorrents(List<String> torrentHashes) throws MessageException {
		RpcRequest r = new RpcRequest("torrent-start");
		r.arguments.put("ids", getTorrentIds(torrentHashes));
		RpcResponse res = executeRpc(r);
		return res.successOrExcept();
	}
	
	public Boolean pauseTorrent(String torrentHash) throws MessageException {
		List<String> s = new ArrayList<>();
		s.add(torrentHash);
		return pauseTorrents(s);		
	}
	
	public Boolean pauseTorrents(List<String> torrentHashes) throws MessageException {
		RpcRequest r = new RpcRequest("torrent-stop", torrentHashes);
		RpcResponse res = executeRpc(r);
		return res.successOrExcept();		
	}	

	public List<Torrent> getTorrents() throws MessageException {
		List<String> fields = Arrays.asList(new String[] {
			"id", "name", "percentDone", "rateDownload", "rateUpload", "errorString",
			"hashString", "totalSize", "downloadedEver", "uploadedEver", "status",
			"metadataPercentComplete", "downloadDir", "files", "wanted", "peers",
			"peersFrom", "priorities", "trackerStats"
		});		
		RpcRequest req = new RpcRequest("torrent-get");
		req.arguments.put("fields", fields);
		RpcResponse r = executeRpc(req);
		JSONArray torrents = (JSONArray) r.arguments.get("torrents");
		List<Torrent> ret = new ArrayList<>();
		for (Object torrent : torrents) {
			JSONObject t = (JSONObject) torrent;
			ret.add(Torrent.fromJson(t.toJSONString()));
		}
		return ret;
	}
	
	public TorrentInfo getTorrentInfo(String torrentHash) throws MessageException {
		//get extra info for torrent and append to passed in torrent
		RpcRequest rpc = new RpcRequest("torrent-get", torrentHash);
		rpc.arguments.put("fields", Arrays.asList(new String[] {
			"files", "peers", "peersFrom", "priorities", "trackerStats", "wanted"
		}));
		RpcResponse res = executeRpc(rpc);
		JSONObject torrent = (JSONObject) ((JSONArray) res.arguments.get("torrents")).get(0);	
		return TorrentInfo.fromJson(torrent.toJSONString());
	}
	
	public Boolean setFilesWanted(String torrentHash, List<String> ids) throws MessageException {
		RpcRequest rpc = new RpcRequest("torrent-set", torrentHash);
		Torrent to = Torrent.findById(torrentHash);
		rpc.arguments.put("files-unwanted", getTorrentIds(to.getFileIds())); //unwant all files
		rpc.arguments.put("files-wanted", getTorrentIds(ids));
		RpcResponse res = executeRpc(rpc);
		return res.successOrExcept();
	}
	
	// priorities: high, normal, low
	public Boolean setFilePriority(String torrentHash, List<String> ids, String priority) throws MessageException {
		RpcRequest rpc = new RpcRequest("torrent-set", torrentHash);
		rpc.arguments.put(String.format("priority-%s", priority), getTorrentIds(ids));
		RpcResponse res = executeRpc(rpc);
		return res.successOrExcept();
	}
	
	private List<Object> getTorrentIds(List<String> ids) {
		List<Object> actual_ids = new ArrayList<>();
		for (String eyedee : ids) {
			try {
				actual_ids.add(Integer.parseInt(eyedee));
			} catch (NumberFormatException ex) {
				actual_ids.add(eyedee);
			}
		}	
		return actual_ids;
	}	
	
	private RpcResponse executeRpc(RpcRequest rpc) throws MessageException {
		return executeRpc(rpc, new HashMap<String, String>());
	}

	private String sessionId = null;
	private RpcResponse executeRpc(RpcRequest rpc, Map<String, String> headers) throws MessageException {
		try {			
			TransmissionConfig tc = this.getConfig();
			HttpClient hc = new DefaultHttpClient();
			HttpPost hp = new HttpPost(this.getTransmissionUrl());
			if (headers == null) {
				headers = new HashMap<>();
			}			
			if (sessionId != null) {				
				headers.put("X-Transmission-Session-Id", sessionId);
			}
			if (headers != null) {
				for (Entry<String, String> header : headers.entrySet()) {
					hp.addHeader(header.getKey(), header.getValue());
				}
			}
			UsernamePasswordCredentials up = new UsernamePasswordCredentials(tc.rpcUsername, tc.getRpcPassword());
			hp.addHeader(new BasicScheme().authenticate(up, hp));
			Gson g = new GsonBuilder().create();
			String body = g.toJson(rpc);
			Logger.debug("Executing rpc: %s", body);
			HttpEntity he = new StringEntity(body);
			hp.setEntity(he);
			HttpResponse res = hc.execute(hp);
			int status = res.getStatusLine().getStatusCode();
			if (status == 409) {
				//resubmit request with X-Transmission-Session-Id header
				sessionId = res.getLastHeader("X-Transmission-Session-Id").getValue();
				Logger.debug("Got 409, retrying using X-Transmission-Session-Id");
				return executeRpc(rpc, headers);
			} else if (status == 401) {
				throw new MessageException("Transmission on node " + node.name + " returned 401 unauthroized.");
			}
			RpcResponse rpc_r = new RpcResponse();
			JSONObject ob = (JSONObject) JSONValue.parse(new InputStreamReader(res.getEntity().getContent()));
			if (!ob.get("result").equals("success")) {
				rpc_r.success = false;
				rpc_r.errorMessage = ob.get("result").toString();
			}
			rpc_r.arguments = (JSONObject) ob.get("arguments");
			rpc_r.jsonData = ob.toJSONString();
			Logger.debug("Response from td: %s ", ob.toJSONString());
			return rpc_r;
		} catch (NoHttpResponseException ex) {
			throw new MessageException("Node not responding");
		} catch (IOException ex) {
			throw new MessageException(getRpcErrorMessage(rpc.method, ex));
		} catch (AuthenticationException ex) {
			throw new MessageException("Bad RPC username/password!");
		}

	}
	
	private String getRpcErrorMessage(String method, Throwable t) {
		Node n = getNode();
		return String.format("Unable to execute RPC command %s on node %s, reason: %s",
					  method, n.name, Util.getStackTrace(t));
	}

	private String getTransmissionUrl() throws MessageException {
		Node n = getNode();
		TransmissionConfig c = this.getConfig();
		return String.format("http://%s:%s%srpc", n.ipAddress, c.rpcPort, c.rpcUrl);
	}	

	@Override
	public String toString() {
		Node n = getNode();
		return "transmission-daemon for node: " + n.name;
	}
	
	public class RpcRequest {
		public String method;
		public Map<String, Object> arguments = new HashMap<>();
		
		public RpcRequest() {}
		
		public RpcRequest(String method) {
			this.method = method;
		}
		
		public RpcRequest(String method, String torrentHash) {
			this(method);
			this.arguments.put("ids", getTorrentIds(Arrays.asList(new String[] { torrentHash })));
		}
		
		public RpcRequest(String method, List<String> torrentHashes) {
			this(method);
			this.arguments.put("ids", getTorrentIds(torrentHashes));
		}
	}
	
	public class RpcResponse {
		public Boolean success = true;
		public String errorMessage = "";
		public String jsonData = "{}";
		public Map<String, Object> arguments;
		
		public Boolean successOrExcept() throws MessageException {
			if (!success) {
				throw new MessageException(errorMessage);
			}
			return success;
		}
		
		@Override
		public String toString() {
			if (success) {
				return "Successful RPC response: " + jsonData;
			} else {
				return "Failed RPC Response: " + errorMessage;
			}
		}
	}

	public class TransmissionConfig {

		/*
		 * Bandwidth
		 */
		@SerializedName("alt-speed-enabled")
		public Boolean altSpeedEnabled = false;
		@SerializedName("alt-speed-up")
		public Integer altSpeedUp = 50;
		@SerializedName("alt-speed-down")
		public Integer altSpeedDown = 50;
		@SerializedName("speed-limit-down")
		public Integer speedLimitDown = 100;
		@SerializedName("speed-limit-down-enabled")
		public Boolean speedLimitDownEnabled = false;
		@SerializedName("speed-limit-up")
		public Integer speedLimitUp = 100;
		@SerializedName("speed-limit-up-enabled")
		public Boolean speedLimitUpEnabled = false;
		@SerializedName("upload-slots-per-torrent")
		public Integer uploadSlotsPerTorrent = 14;
		/*
		 * Blocklists
		 */
		@SerializedName("blocklist-enabled")
		public Boolean blocklistEnabled = true;
		@SerializedName("blocklist-url")
		public String blocklistUrl = "http://www.bluetack.co.uk/config/level1.gz";
		/*
		 * Files and Locations
		 */
		public Integer PREALLOCATION_OFF = 0;
		public Integer PREALLOCATION_FAST = 1;
		public Integer PREALLOCATION_FULL = 2;
		@SerializedName("download-dir")
		public String downloadDir = "/var/www/torrents/complete";
		@SerializedName("incomplete-dir")
		public String incompleteDir = "/var/www/torrents/incomplete";
		@SerializedName("incomplete-dir-enabled")
		public Boolean incompleteDirEnabled = true;
		@SerializedName("preallocation")
		public Integer preallocation = PREALLOCATION_FAST;
		@SerializedName("rename-partial-files")
		public Boolean renamePartialFiles = true;
		@SerializedName("start-added-torrents")
		public Boolean startAddedTorrents = true;
		@SerializedName("trash-original-torrent-files")
		public Boolean trashOriginalTorrentFiles = false;
		@SerializedName("umask")
		public Integer umask = 18;
		@SerializedName("watch-dir")
		public String watchDir = "/var/www/torrents/watch";
		@SerializedName("watch-dir-enabled")
		public Boolean watchDirEnabled = true;
		/*
		 * Misc
		 */
		public Integer ENCRYPTION_PREFER_UNENCRYPTED;
		public Integer ENCRYPTION_PREFER_ENCRYPTED;
		public Integer ENCRYPTION_REQUIRE_ENCRYPTED;
		public Integer MESSAGE_LEVEL_NONE = 0;
		public Integer MESSAGE_LEVEL_ERROR = 1;
		public Integer MESSAGE_LEVEL_INFO = 2;
		public Integer MESSAGE_LEVEL_DEBUG = 3;
		@SerializedName("cache-size-mb")
		public Integer cacheSizeMb = 4;
		@SerializedName("dht-enabled")
		public Boolean dhtEnabled;
		@SerializedName("encryption")
		public Integer encryption = ENCRYPTION_REQUIRE_ENCRYPTED;
		@SerializedName("lazy-bitfield-enabled")
		public Boolean lazyBitfieldEnabled = true;
		@SerializedName("lpd-enabled")
		public Boolean localPeerDiscoveryEnabled = false;
		@SerializedName("message-level")
		public Integer messageLevel = MESSAGE_LEVEL_INFO;
		@SerializedName("pex-enabled")
		public Boolean pexEnabled = true;
		@SerializedName("prefetch-enabled")
		public Boolean prefetchEnabled = true;
		@SerializedName("scrape-paused-torrents-enabled")
		public Boolean scrapePausedTorrentsEnabled = true;
		@SerializedName("script-torrent-done-enabled")
		public Boolean scriptTorrentDoneEnabled = false;
		@SerializedName("script-torrent-done-filename")
		public String scriptTorrentDoneFilename = "";
		@SerializedName("utp-enabled")
		public Boolean utpEnabled = true;
		/*
		 * Peers
		 */
		public String PEER_SOCKET_TOS_DEFAULT = "default";
		public String PEER_SOCKET_TOS_LOWCOST = "lowcost";
		public String PEER_SOCKET_TOS_THROUGHPUT = "throughput";
		public String PEER_SOCKET_TOS_LOWDELAY = "lowdelay";
		public String PEER_SOCKET_TOS_RELIABILITY = "reliability";
		@SerializedName("bind-address-ipv4")
		public String bindAddressIpv4 = "0.0.0.0";
		@SerializedName("bind-address-ipv6")
		public String bindAddressIpv6 = "::";
		@SerializedName("peer-congestion-algorithm")
		public String peerCongestionAlgorithm = "";
		@SerializedName("peer-limit-global")
		public Integer peerLimitGlobal = 240;
		@SerializedName("peer-limit-per-torrent")
		public Integer peerLimitPerTorrent = 60;
		@SerializedName("peer-socket-tos")
		public String peerSocketTos = PEER_SOCKET_TOS_DEFAULT;
		@SerializedName("peer-port")
		public Integer peerPort = 51413;
		@SerializedName("peer-port-random-high")
		public Integer peerPortRandomHigh = 65535;
		@SerializedName("peer-port-random-low")
		public Integer peerPortRandomLow = 1024;
		@SerializedName("peer-port-random-on-start")
		public Boolean peerPortRandomOnStart = false;
		@SerializedName("port-forwarding-enabled")
		public Boolean portForwardingEnabled = true;
		/*
		 * Queuing
		 */
		@SerializedName("download-queue-enabled")
		public Boolean downloadQueueEnabled = true;
		@SerializedName("download-queue-size")
		public Integer downloadQueueSize = 5;
		@SerializedName("queue-stalled-enabled")
		public Boolean queueStalledEnabled = true;
		@SerializedName("queue-stalled-minutes")
		public Integer queueStalledMinutes = 30;
		@SerializedName("seed-queue-enabled")
		public Boolean seedQueueEnabled = false;
		@SerializedName("seed-queue-size")
		public Integer seedQueueSize = 10;
		/*
		 * RPC
		 */
		@SerializedName("rpc-authentication-required")
		public Boolean rpcAuthenticationRequired = true;
		@SerializedName("rpc-bind-address")
		public String rpcBindAddress = "0.0.0.0";
		@SerializedName("rpc-enabled")
		public Boolean rpcEnabled = true;
		@SerializedName("rpc-password")
		public String rpcPassword = "";
		@SerializedName("rpc-password-unencrypted")
		public String rpcPasswordUnencrypted = "";
		@SerializedName("rpc-port")
		public Integer rpcPort = 9091;
		@SerializedName("rpc-url")
		public String rpcUrl = "/transmission/";
		@SerializedName("rpc-username")
		public String rpcUsername = "";
		@SerializedName("rpc-whitelist")
		public String rpcWhitelist = "";
		@SerializedName("rpc-whitelist-enabled")
		public Boolean rpcWhitelistEnabled = false;
		/*
		 * Scheduling
		 */
		@SerializedName("alt-speed-time-enabled")
		public Boolean altSpeedTimeEnabled = false;
		@SerializedName("alt-speed-time-begin")
		public Integer altSpeedTimeBegin = 540;
		@SerializedName("alt-speed-time-end")
		public Integer altSpeedTimeEnd = 1020;
		@SerializedName("alt-speed-time-day")
		public Integer altSpeedTimeDay = 127;
		@SerializedName("idle-seeding-limit")
		public Integer idleSeedingLimit = 30;
		@SerializedName("idle-seeding-limit-enabled")
		public Boolean idleSeedingLimitEnabled = false;
		@SerializedName("ratio-limit")
		public double ratioLimit = 2.0;
		@SerializedName("ratio-limit-enabled")
		public Boolean ratioLimitEnabled = true;

		public void setDefaults() {
			this.altSpeedEnabled = false;
			this.altSpeedUp = 50;
			this.altSpeedDown = 50;
			this.speedLimitDown = 100;
			this.speedLimitDownEnabled = false;
			this.speedLimitUp = 100;
			this.speedLimitUpEnabled = false;
			this.uploadSlotsPerTorrent = 14;
			this.blocklistEnabled = true;
			this.blocklistUrl = "http://www.bluetack.co.uk/config/level1.gz";
			this.downloadDir = "/var/www/torrents/complete";
			this.incompleteDir = "/var/www/torrents/incomplete";
			this.incompleteDirEnabled = true;
			this.preallocation = PREALLOCATION_FAST;
			this.renamePartialFiles = true;
			this.startAddedTorrents = true;
			this.trashOriginalTorrentFiles = false;
			this.umask = 18;
			this.watchDir = "/var/www/torrents/watch";
			this.watchDirEnabled = true;
			this.cacheSizeMb = 4;
			this.dhtEnabled = true;
			this.encryption = ENCRYPTION_REQUIRE_ENCRYPTED;
			this.lazyBitfieldEnabled = true;
			this.localPeerDiscoveryEnabled = false;
			this.messageLevel = MESSAGE_LEVEL_INFO;
			this.pexEnabled = true;
			this.prefetchEnabled = true;
			this.scrapePausedTorrentsEnabled = true;
			this.scriptTorrentDoneEnabled = false;
			this.scriptTorrentDoneFilename = "";
			this.utpEnabled = true;
			this.bindAddressIpv4 = "0.0.0.0";
			this.bindAddressIpv6 = "::";
			this.peerCongestionAlgorithm = "";
			this.peerLimitGlobal = 240;
			this.peerLimitPerTorrent = 60;
			this.peerSocketTos = PEER_SOCKET_TOS_DEFAULT;
			this.peerPort = 51413;
			this.peerPortRandomHigh = 65535;
			this.peerPortRandomLow = 1024;
			this.peerPortRandomOnStart = false;
			this.portForwardingEnabled = true;
			this.downloadQueueEnabled = true;
			this.downloadQueueSize = 5;
			this.queueStalledEnabled = true;
			this.queueStalledMinutes = 30;
			this.seedQueueEnabled = false;
			this.seedQueueSize = 10;
			this.rpcAuthenticationRequired = true;
			this.rpcBindAddress = "0.0.0.0";
			this.rpcEnabled = true;
			this.rpcPassword = "";
			this.rpcPort = 9091;
			this.rpcUrl = "/transmission/";
			this.rpcUsername = "";
			this.rpcWhitelist = "";
			this.rpcWhitelistEnabled = false;
			this.altSpeedTimeEnabled = false;
			this.altSpeedTimeBegin = 540;
			this.altSpeedTimeEnd = 1020;
			this.altSpeedTimeDay = 127;
			this.idleSeedingLimit = 30;
			this.idleSeedingLimitEnabled = false;
			this.ratioLimit = 2.0;
			this.ratioLimitEnabled = true;
		}
		
		public String getRpcPassword() {
			if (this.rpcPassword.startsWith("{")) {
				return this.rpcPasswordUnencrypted;
			}
			return this.rpcPassword;
		}
		
		public void setRpcPassword(String password) {
			this.rpcPassword = password;
			this.rpcPasswordUnencrypted = password;
		}

		public void save(Node n) throws MessageException {
			try {
				this.rpcPassword = this.rpcPasswordUnencrypted;
				Gson g = new GsonBuilder().create();
				n.writeFile(g.toJson(this), "/etc/transmission-daemon/settings.json");
			} catch (JSchException | SftpException ex) {
				Logger.error(ex, "Unable to save transmission-daemon config to node " + n.name);
				throw new MessageException("Unable to save config!");
			}
		}
	}
}

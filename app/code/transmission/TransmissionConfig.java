package code.transmission;

import code.MessageException;
import com.google.gson.annotations.SerializedName;
import models.Node;

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
		/*
		 * try { this.rpcPassword = this.rpcPasswordUnencrypted; Gson g = new
		 * GsonBuilder().create(); n.writeFile(g.toJson(this),
		 * "/etc/transmission-daemon/settings.json"); } catch (JSchException ex)
		 * { Logger.error(ex, "Unable to save transmission-daemon config to node
		 * " + n.name); throw new MessageException("Unable to save config!"); }
		 * catch (SftpException ex) { Logger.error(ex, "Unable to save
		 * transmission-daemon config to node " + n.name); throw new
		 * MessageException("Unable to save config!");
			}
		 */
	}
}

package com.openseedbox.models;

import com.google.gson.JsonObject;
import com.openseedbox.backend.INodeStatus;
import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.backend.NodeStatus;
import com.openseedbox.backend.node.NodeBackend;
import com.openseedbox.code.MessageException;
import com.openseedbox.code.Util;
import java.net.InetAddress;
import java.util.List;
import play.data.validation.Required;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import siena.Column;
import siena.Table;
import org.apache.commons.lang.StringUtils;

@Table("node")
public class Node extends ModelBase {

	@Required private String name;
	
	@Required @Column("ip_address") private String ipAddress;
	
	@Required private String scheme;		
	
	@Required @Column("api_key") private String apiKey;
	
	private boolean down;	
		
	private boolean active;
	
	public static Node getBestForNewTorrent(User u) {
		//TODO: work this out properly. For now, just use the first one
		if (u.hasDedicatedNode()) {
			return u.getDedicatedNode();
		}
		return Node.all().limit(1).get();
	}
	
	public static List<Node> getActiveNodes() {
		return Node.all().filter("active", true).fetch();
	}
	
	public INodeStatus getNodeStatus() {
		try {
			HttpResponse res = getWebService("/status").get();
			if (res.success()) {			
				JsonObject fullResponse = res.getJson().getAsJsonObject();
				if (!fullResponse.get("success").getAsBoolean()) {
					String error = fullResponse.get("error").getAsString();
					throw new MessageException(error);
				}
				INodeStatus status = Util.getGson().fromJson(fullResponse.get("data"), NodeStatus.class);
				return status;
			}
			throw new MessageException("Node returned status: " + res.getStatus() + ". Probably java isnt running.");
		} catch (Exception ex) {
			if (ex.getMessage().contains("Connection refused")) {
				throw new MessageException("Connection refused");
			} else if (ex.getMessage().contains("No route to host")) {
				throw new MessageException("Unable to contact node at all! No route to host.");
			}
			throw new MessageException(ex.getMessage());
		}
	}
	
	public ITorrentBackend getNodeBackend() {
		return new NodeBackend(this);
	}
	
	public boolean isReachable() {
		try {
			return InetAddress.getByName(this.ipAddress).isReachable(10000);
		} catch (Exception ex) {
			return false;
		}
	}	
	
	public WSRequest getWebService(String action) {		
		if (!action.startsWith("/")) {
			action = "/" + action;
		}
		String url = getNodeUrl() + action;
		return WS.url(url)
				  .setParameter("ext", "json")
				  .setParameter("api_key", this.apiKey);
	}
	
	public WSRequest getWebService(String action, String hash) {		
		WSRequest req = getWebService(action);
		req.setParameter("hash", hash);
		return req;
	}
	
	public WSRequest getWebService(String action, List<String> hashes) {		
		WSRequest req = getWebService(action);
		req.setParameter("hashes", hashes);
		return req;
	}	
	
	public String getNodeUrl() {
		return String.format("%s://%s", getScheme(), getIpAddress());
	}
	
	/* Getters and Setters */

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}	
	
	public boolean isDown() {
		return down;
	}

	public void setDown(boolean down) {
		this.down = down;
	}	

	public String getScheme() {
		if (StringUtils.isEmpty(scheme)) {
			return "http";
		}
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}
}

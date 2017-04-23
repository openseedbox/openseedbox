package com.openseedbox.models;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.openseedbox.backend.*;
import com.openseedbox.backend.node.NodeBackend;
import com.openseedbox.code.MessageException;
import com.openseedbox.code.Util;

import play.data.validation.Required;
import play.jobs.Job;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import siena.Column;
import siena.Table;
import siena.embed.Embedded;

@Table("node")
public class Node extends ModelBase {

	@Required private String name;	
	@Required @Column("ip_address") private String ipAddress;	
	@Required private String scheme;			
	@Required @Column("api_key") private String apiKey;	
	private boolean down;			
	private boolean active;
	@Embedded private NodeStatus status;
	
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
	
	/**
	 * Returns the total space available from all the nodes in the system
	 * @return The space, in bytes
	 */
	public static long getTotalSpaceBytes() {
		long space = 0;
		List<Node> nodes = getActiveNodes();
		for (Node n : nodes) {
			INodeStatus s = n.getNodeStatus(true);
			if (s != null) {
				space += s.getTotalSpaceBytes();
			}
		}
		return space;
	}
	
	/**
	 * Returns the total space used on all the nodes in the system
	 * @return The space, in bytes
	 */
	public static long getUsedSpaceBytes() {
		long space = 0;
		List<Node> nodes = getActiveNodes();
		for (Node n : nodes) {
			INodeStatus s = n.getNodeStatus(true);
			if (s != null) {
				space += s.getUsedSpaceBytes();
			}
		}
		return space;		
	}
	
	public INodeStatus getNodeStatus() {
		return getNodeStatus(false);
	}
	
	public INodeStatus getNodeStatus(boolean fromDb) {
		if (fromDb) {
			return status;
		}
		HttpResponse res = getWebService("/status").get();
		if (res.success()) {
			JsonObject fullResponse = handleWebServiceResponse(res).getAsJsonObject();
			status = Util.getGson().fromJson(fullResponse, NodeStatus.class);
			new Job() {
				@Override
				public void doJob() throws Exception {
					update();
				}
			}.now();
			return status;
		} else {
			throw new MessageException("Node returned status: " + res.getStatus() + ". Probably java isnt running.");
		}
	}
	
	public JsonElement handleWebServiceResponse(HttpResponse res) {
		try {			
			if (res.success()) {			
				JsonObject fullResponse = res.getJson().getAsJsonObject();
				if (!fullResponse.get("success").getAsBoolean()) {
					String error = fullResponse.get("error").getAsString();
					throw new MessageException(error);
				}
				return fullResponse.get("data");
			}
			throw new MessageException("Unsuccessful webservice call, status: " + res.getStatusText());
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
	
	public void setNodeStatus(NodeStatus status) {
		this.status = status;
	}
	
	/* End Getters and Setters */	
}

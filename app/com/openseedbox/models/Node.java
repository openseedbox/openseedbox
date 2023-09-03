package com.openseedbox.models;

import com.avaje.ebean.annotation.DbJson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.openseedbox.backend.INodeStatus;
import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.backend.NodeStatus;
import com.openseedbox.backend.node.NodeBackend;
import com.openseedbox.code.ws.CustomSSLContext;
import com.openseedbox.code.MessageException;
import com.openseedbox.code.Util;
import com.openseedbox.models.util.EmbeddableNodeStatus;
import com.openseedbox.mvc.validation.IsCertificateOrBlankString;
import org.apache.commons.lang.StringUtils;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.jobs.Job;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;

import javax.net.ssl.SSLContext;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
public class Node extends ModelBase {

	@Required private String name;
	@Required private String ipAddress;
	@Required private String scheme;
	private String downloadIpAddress;
	private String downloadScheme;
	@Required private String apiKey;
	@CheckWith(IsCertificateOrBlankString.class)
	private String certificatesToTrust;
	private boolean down;
	private boolean active;
	@DbJson
	private NodeStatus status;

	@Override
	public void save() {
		super.save();

		//reload SSLContext
		reloadSSLContext();
	}
	
	public static Node getBestForNewTorrent(User u) {
		if (u.hasDedicatedNode()) {
			return u.getDedicatedNode();
		}
		//TODO: work this out properly. For now, just use the first one
		List<Node> nodes = Node.<Node>all()
				.where()
				.eq("active", true)
				.setMaxRows(1)
				.findList();
		if (nodes == null || nodes.size() == 0) {
			throw new IllegalStateException("No available Node was found for you!");
		}
		return nodes.get(0);
	}
	
	public static List<Node> getActiveNodes() {
		return Node.<Node>all()
				.where()
				.eq("active", true)
				.findList();
	}

	public static List<Node> getNodesWithCertificates() {
		return Node.<Node>all()
				.where()
				.isNotNull("certificatesToTrust")
				.findList();
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

	public static List<Map.Entry<String, String>> getAllUriWithCertificate(List<Node> nodes) {
		StringBuilder certificatesStringBuilder = new StringBuilder();
		List<Map.Entry<String, String>> entryList = new ArrayList<Map.Entry<String, String>>();
		for (Node n : nodes) {
			if (!StringUtils.isEmpty(StringUtils.trimToEmpty(n.getCertificatesToTrust()))) {
				entryList.add(new AbstractMap.SimpleEntry<String, String>(n.getIpAddress(), n.getCertificatesToTrust()));
			}
		}
		return entryList;
	}

	public static void reloadSSLContext() {
		List<Map.Entry<String, String>> uriWithcertificatesToTrust = getAllUriWithCertificate(getNodesWithCertificates());
		if (uriWithcertificatesToTrust.size() > 0) {
			SSLContext customSslContext = CustomSSLContext.getSslContext(uriWithcertificatesToTrust);
			SSLContext.setDefault(customSslContext);
		}
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
			status = Util.getGson().fromJson(fullResponse, EmbeddableNodeStatus.class);
			new Job() {
				@Override
				public void doJob() throws Exception {
					save();
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
			throw new MessageException(Util.getStackTrace(ex));
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

	public String getNodeDownloadUrl() {
		return String.format("%s://%s", getDownloadScheme(), getDownloadIpAddress());
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

	public String getDownloadIpAddress() {
		return StringUtils.isBlank(downloadIpAddress) ? ipAddress : downloadIpAddress;
	}

	public void setDownloadIpAddress(String downloadIpAddress) {
		this.downloadIpAddress = downloadIpAddress;
	}

	public String getDownloadScheme() {
		return StringUtils.isBlank(downloadScheme) ? scheme : downloadScheme;
	}

	public void setDownloadScheme(String downloadScheme) {
		this.downloadScheme = downloadScheme;
	}

	public String getCertificatesToTrust() {
		return certificatesToTrust;
	}

	public void setCertificatesToTrust(String certificatesToTrust) {
		this.certificatesToTrust = certificatesToTrust;
	}

	/* End Getters and Setters */
}

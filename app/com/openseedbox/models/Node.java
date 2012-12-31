package com.openseedbox.models;

import com.google.gson.Gson;
import com.openseedbox.Config;
import com.openseedbox.backend.INodeStatus;
import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.backend.NodeStatus;
import com.openseedbox.backend.node.NodeBackend;
import com.openseedbox.code.MessageException;
import com.openseedbox.code.Util;
import java.net.InetAddress;
import java.util.List;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import siena.Column;
import siena.Table;
import com.openseedbox.mvc.validation.IsWholeNumber;

@Table("node")
public class Node extends ModelBase {

	@Required private String name;
	
	@Required @Column("ip_address") private String ipAddress;
	
	@Required @CheckWith(IsWholeNumber.class) private String port;
	
	@Required @Column("api_key") private String apiKey;
	
	private boolean down;	
		
	private boolean active;
	
	public static Node getBestForNewTorrent() {
		//TODO: work this out properly. For now, just use the first one
		return Node.all().limit(1).get();
	}
	
	public static List<Node> getActiveNodes() {
		return Node.all().filter("active", true).fetch();
	}
	
	public INodeStatus getNodeStatus() {
		try {
			HttpResponse res = getWebService("/status").get();
			if (res.success()) {			
				INodeStatus status = Util.getGson().fromJson(res.getJson().getAsJsonObject().get("data"), NodeStatus.class);
				return status;
			}	
		} catch (Exception ex) {
			if (ex.getMessage().contains("Connection refused")) {
				throw new MessageException("Connection refused");
			}
			throw new MessageException(ex.getMessage());
		}
		return null;
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
		return String.format("%s://%s:%s", Config.getNodeAccessType(), this.ipAddress, this.port);
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

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
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
	
	/* End Getters and Setters */
	
	/*
	public interface ISshOutputReporter {
		public void onOutputLine(String line);
	}*/

		/*
	public void writeFileToNode(String data, String destinationLocation)
			throws JSchException, IOException, SftpException {
		writeFileToNode(data, destinationLocation, 0644);
	}
	
	public void writeFileToNode(String data, String destinationLocation, int chmod)
			  throws JSchException, IOException, SftpException {
		Session session = getConnectedSession();
		try {
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			InputStream is = new ByteArrayInputStream(data.getBytes());
			sftpChannel.put(is, destinationLocation);
			sftpChannel.chmod(chmod, destinationLocation);
			sftpChannel.exit();
			is.close();
		} finally {
			session.disconnect();
		}
	}
	
	public void transferFileToNode(File source, String destinationLocation)
			  throws JSchException, IOException, SftpException {
		transferFileToNode(source, destinationLocation, 0644);
	}

	public void transferFileToNode(File source, String destinationLocation, int chmod)
			  throws JSchException, IOException, SftpException {
		Session session = getConnectedSession();
		try {
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			if (destinationLocation.equals("~/") || destinationLocation.equals("~")) {
				destinationLocation = sftpChannel.getHome();
			}
			sftpChannel.put(source.getAbsolutePath(), destinationLocation);
			sftpChannel.chmod(chmod, FilenameUtils.concat(destinationLocation, source.getName()));
			sftpChannel.exit();
		} finally {
			session.disconnect();
		}
	}
	
	public String executeCommandSafe(String command) {
		try {
			String res = executeCommand(command);
			if (res.startsWith("\"")) {
				res = res.substring(1);
			}
			if (res.endsWith("\"")) {
				res = res.substring(0, res.length() - 1);
			}
			return res;
		} catch (Exception ex) {
			return ex.getMessage();
		}		
	}
	
	public String executeCommand(String command) throws JSchException, IOException {
		final StringBuilder sb = new StringBuilder();
		executeCommand(command, new ISshOutputReporter() {
			public void onOutputLine(String line) {
				sb.append(line);
			}		
		}, false);
		return sb.toString().trim();
	}
	
	public void executeCommand(String command, ISshOutputReporter reporter) throws JSchException, IOException {
		executeCommand(command, reporter, true);
	}

	public void executeCommand(String command, ISshOutputReporter reporter, boolean verbose)
			  throws JSchException, IOException {
		Session session = this.getConnectedSession();
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand(command);
		InputStream in = channel.getInputStream();
		channel.connect();
		if (verbose) {
			reporter.onOutputLine("Unix system connected...");
		}
		byte[] tmp = new byte[1024];
		while (true) {
			while (in.available() > 0) {
				int i = in.read(tmp, 0, 1024);
				if (i < 0) {
					break;
				}
				reporter.onOutputLine(new String(tmp, 0, i));
			}
			if (channel.isClosed()) {
				if (verbose) {
					reporter.onOutputLine("exit-status: " + channel.getExitStatus());
				}
				break;
			}
			try {
				Thread.sleep(100);
			} catch (Exception ee) {
			}
		}
		if (verbose) {
			reporter.onOutputLine("Unix system disconnected.");
		}
		channel.disconnect();
		session.disconnect();
	}

	private Session getConnectedSession() throws JSchException {
		JSch jsch = new JSch();
		Session session = jsch.getSession(this.username, this.ipAddress);
		session.setConfig("StrictHostKeyChecking", "no");
		session.setPassword(this.password);
		session.connect();
		return session;
	}*/	
}

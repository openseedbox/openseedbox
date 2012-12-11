package models;

import com.openseedbox.code.Util;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.DecimalFormat;
import javax.persistence.Column;
import javax.persistence.Table;
import models.Node.NodeStatus;
import org.apache.commons.io.FilenameUtils;
import play.data.validation.Required;
import play.db.jpa.Model;

@Table(name="node")
public class Node extends Model {

	@Required
	private String name;
	
	@Required
	@Column(name="ip_address")
	private String ipAddress;
	
	@Required
	private String username;
	
	@Required
	private String password;
	
	@Required
	@Column(name="root_password")
	private String rootPassword;
	
	private boolean active;
	
	@Column(name="backend_class")
	private String backendClass;
	
	public NodeStatus getNodeStatus() {
		NodeStatus ns = new NodeStatus();
		ns.setReachable(this.isReachable());
		if (ns.isReachable()) {
			ns.setUptime(getUptime());
			ns.setFreeSpaceGb(Double.parseDouble(Util.stripNonNumeric(getFreeSpace())));
			ns.setTotalSpaceGb(Double.parseDouble(Util.stripNonNumeric(getTotalSpace())));
			ns.setStorageDirectoryWritable(storageDirectoryIsWritable());
		}
		return ns;
	}
	
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
	}
	
	private boolean isReachable() {
		try {
			return InetAddress.getByName(this.ipAddress).isReachable(10000);
		} catch (Exception ex) {
			return false;
		}
	}	

	private String getUptime() {
		return executeCommandSafe("uptime");
	}
	
	private String getFreeSpace() {
		return executeCommandSafe("df -h / | grep /dev/ | awk '{print $4}'");
	}
	
	private String getTotalSpace() {
		return executeCommandSafe("df -h / | grep /dev/ | awk '{print $2}'");
	}
	
	private boolean storageDirectoryIsWritable() {
		return false;/*
		BackendConfig bc = Settings.getBackendConfig();
		String res = executeCommandSafe("if [ -w " + bc.getBaseFolder() +
				  " ]; then echo 'yes'; else echo 'no'; fi");
		return res.trim().equals("yes");*/
	}
	
	/* Getters and Setters */
	public String getRootPassword() {
		return rootPassword;
	}

	public void setRootPassword(String rootPassword) {
		this.rootPassword = rootPassword;
	}

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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getBackendClass() {
		return backendClass;
	}

	public void setBackendClass(String backendClass) {
		this.backendClass = backendClass;
	}
	
	/* End Getters and Setters */
	
	public class NodeStatus {
		
		private double freeSpaceGb;
		private double totalSpaceGb;
		private boolean storageDirectoryWritable;
		private boolean isReachable;
		private String uptime;
		private double usedSpaceGb;
		
		public double getUsedSpaceGb() {
			return usedSpaceGb;
		}
		
		private void calculateUsedSpaceGb() {
			DecimalFormat df = new DecimalFormat("#.##");
			usedSpaceGb = Double.valueOf(df.format(totalSpaceGb - freeSpaceGb));
		}
		
		public double getFreeSpaceGb() {
			return freeSpaceGb;
		}

		public void setFreeSpaceGb(double freeSpaceGb) {
			this.freeSpaceGb = freeSpaceGb;
			calculateUsedSpaceGb();
		}

		public double getTotalSpaceGb() {
			return totalSpaceGb;
		}

		public void setTotalSpaceGb(double totalSpaceGb) {
			this.totalSpaceGb = totalSpaceGb;
			calculateUsedSpaceGb();
		}

		public boolean isStorageDirectoryWritable() {
			return storageDirectoryWritable;
		}

		public void setStorageDirectoryWritable(boolean storageDirectoryWritable) {
			this.storageDirectoryWritable = storageDirectoryWritable;
		}

		public String getUptime() {
			return uptime;
		}

		public void setUptime(String uptime) {
			this.uptime = uptime;
		}

		public boolean isReachable() {
			return isReachable;
		}

		public void setReachable(boolean isReachable) {
			this.isReachable = isReachable;
		}

	}
	
	public interface ISshOutputReporter {
		public void onOutputLine(String line);
	}

}

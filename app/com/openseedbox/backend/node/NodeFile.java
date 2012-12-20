package com.openseedbox.backend.node;

import com.openseedbox.backend.IFile;
import com.openseedbox.code.Util;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import models.Node;
import org.apache.commons.lang.StringUtils;

public class NodeFile implements IFile {
	
	private String id;
	private String name;
	private String fullPath;
	private boolean wanted;
	private boolean completed;
	private long bytesCompleted;
	private long fileSizeBytes;
	private int priority;
	private Node node;
	private String torrentHash;
	private String downloadLink;
	
	public void setNode(Node n) {
		this.node = n;
	}
	
	public void setTorrentHash(String hash) {
		this.torrentHash = hash;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public boolean isWanted() {
		return wanted;
	}

	public void setWanted(boolean wanted) {
		this.wanted = wanted;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public long getBytesCompleted() {
		return bytesCompleted;
	}

	public void setBytesCompleted(long bytesCompleted) {
		this.bytesCompleted = bytesCompleted;
	}

	public long getFileSizeBytes() {
		return fileSizeBytes;
	}

	public void setFileSizeBytes(long fileSizeBytes) {
		this.fileSizeBytes = fileSizeBytes;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getDownloadLink() {
		if (node == null) {
			throw new IllegalArgumentException("You need to call setNode() first");
		}				
		return node.getNodeUrl() + downloadLink;
	}
	
	public void setDownloadLink(String downloadLink) {
		this.downloadLink = downloadLink;
	}	

	public double getPercentComplete() {
		return (double) getBytesCompleted() / (double) getFileSizeBytes();
	}
}

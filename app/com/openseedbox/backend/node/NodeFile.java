package com.openseedbox.backend.node;

import com.openseedbox.backend.AbstractFile;
import com.openseedbox.models.Node;

public class NodeFile extends AbstractFile {
	
	private String id;
	private String name;
	private String fullPath;
	private boolean wanted;	
	private long bytesCompleted;
	private long fileSizeBytes;
	private int priority;
	private Node node;	
	private String downloadLink;
	
	public void setNode(Node n) {
		this.node = n;
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
		return node.getNodeDownloadUrl() + downloadLink;
	}
	
	public void setDownloadLink(String downloadLink) {
		this.downloadLink = downloadLink;
	}
}

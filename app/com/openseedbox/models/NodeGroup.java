package com.openseedbox.models;

import java.util.ArrayList;
import java.util.List;
import play.data.validation.Required;

import javax.persistence.Embedded;

public class NodeGroup extends ModelBase {
	
	@Required private String name;
	@Embedded private List<Long> nodesInGroup; //store ids of nodes

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Node> getNodesInGroup() {
		if (nodesInGroup == null) {
			return new ArrayList<Node>();
		}
		return Node.<Node>all()
				.where()
				.in("id", nodesInGroup)
				.findList();
	}

	public void addNodeToGroup(Node n) {
		if (this.nodesInGroup == null) {
			this.nodesInGroup = new ArrayList<Long>();
		}
		this.nodesInGroup.add(n.getId());
		this.save();
	}		
	
}

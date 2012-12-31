package com.openseedbox.jobs;

import java.util.List;
import com.openseedbox.models.Node;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class Boostrap extends Job {

	@Override
	public void doJob() throws Exception {
		//give the nodes the benefit of the doubt and set them to up
		List<Node> nodes = Node.getActiveNodes();
		for(Node n : nodes) {
			n.setDown(false);				
		}
		Node.batch().update(nodes);
	}		
	
}

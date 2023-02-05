package com.openseedbox.jobs;

import com.openseedbox.models.Node;
import com.openseedbox.mvc.TemplateNameResolver;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.mvc.Controller;

import java.util.List;

@OnApplicationStart
public class Bootstrap extends Job {

	@Override
	public void doJob() throws Exception {
		Controller.registerTemplateNameResolver(new TemplateNameResolver());

		//give the nodes the benefit of the doubt and set them to up
		List<Node> nodes = Node.getActiveNodes();
		for(Node n : nodes) {
			n.setDown(false);				
		}
		Node.batch().update(nodes);

		//apply custom certificates
		Node.reloadSSLContext();
	}
	
}

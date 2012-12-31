package com.openseedbox.jobs;

import com.openseedbox.backend.INodeStatus;
import java.util.List;
import com.openseedbox.models.EmailError;
import com.openseedbox.models.Node;
import play.jobs.Every;
import play.jobs.Job;

/**
 * @author Erin Drummond
 */
@Every("10mn")
public class HealthCheckJob extends Job {

	@Override
	public void doJob() throws Exception {
		//Logger.info("Checking health...");
		List<Node> all = Node.getActiveNodes();
		for (Node n : all) {
			if (n.isReachable()) {
				try {
					INodeStatus status = n.getNodeStatus(); //will throw error if theres a problem
					if (status.isBackendRunning()) {
						if (n.isDown()) {
							n.setDown(false);
							n.save();
							EmailError.nodeBackUp(n);
						}
					} else {
						n.setDown(true); n.save();
						EmailError.nodeDown(n, null);
					}					
				} catch (Exception ex) {
					n.setDown(true); n.save();
					EmailError.nodeDown(n, ex);
				}
			} else {
				n.setDown(true); n.save();
				EmailError.nodeDown(n, null);
			}
		}
	}		
	
}

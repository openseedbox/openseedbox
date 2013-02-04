package com.openseedbox.jobs;

import com.openseedbox.backend.INodeStatus;
import com.openseedbox.models.EmailError;
import com.openseedbox.models.Node;
import java.util.List;
import play.jobs.Every;

@Every("10mn")
@JobName("Node Health Check")
public class HealthCheckJob extends LoggedJob {	

	@Override
	protected Object doGenericJob() {
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
		return null;
	}
	
}

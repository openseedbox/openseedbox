package code.jobs;

import java.util.List;
import models.Node;
import play.jobs.Every;
import play.jobs.Job;

@Every("10s")
public class NodePollerJob extends Job {

	@Override
	public void doJob() throws Exception {
		List<Node> nodes = Node.all().fetch();
		for (Node n : nodes) {
			
		}
	}
	
}

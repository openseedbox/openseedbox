package code.jobs;

import code.jobs.NodeMigrationJob.NodeMigrationJobResult;
import models.Node;
import models.User;
import play.jobs.Job;


public class NodeMigrationJob extends Job<NodeMigrationJobResult> {
	
	private User _user;
	private Node _newNode;
	
	public NodeMigrationJob(User user, Node newNode) {
		_user = user;
		_newNode = newNode;
	}

	@Override
	public NodeMigrationJobResult doJobWithResult() throws Exception {
		return super.doJobWithResult();
	}
	
	public class NodeMigrationJobResult extends JobResult {
		
	}
	
}

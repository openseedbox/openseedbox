package code.jobs;

import code.MessageException;
import code.jobs.NodeMigrationJob.NodeMigrationJobResult;
import code.jobs.PlanSwitcherJob.PlanSwitcherJobResult;
import java.util.Date;
import java.util.List;
import models.Account;
import models.FreeSlot;
import models.Node;
import models.Plan;
import models.PlanSwitch;
import models.User;
import org.apache.commons.lang.exception.ExceptionUtils;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

public class PlanSwitcherJob extends Job<PlanSwitcherJobResult> {
	
	private PlanSwitch _ps;
	
	public PlanSwitcherJob(PlanSwitch ps) {
		_ps = ps;
	}

	@Override
	public PlanSwitcherJobResult doJobWithResult() {
		/*if (_ps == null) {
			List<PlanSwitch> all = PlanSwitch.all().filter("switchDateUtc", null).fetch();
			if (all.size() > 0) {
				Logger.debug("Running PlanSwitcherJob on schedule, %s plans to switch.", all.size());
			}
			for (PlanSwitch ps : all) {
				_ps = ps;
				switchPlan();
			}
			return new PlanSwitcherJobResult();
		} else {*/
			return switchPlan();
		//}
	}
	
	protected PlanSwitcherJobResult switchPlan() {
		//Stuff we need
		FreeSlot fs = _ps.getFreeSlot();
		Node newNode = fs.getNode();
		Account a = _ps.getAccount();
		Node currentNode = a.getNode();
		Plan currentPlan = a.getPlan();
		Plan newPlan = fs.getPlan();
		User u = a.getPrimaryUser();
		
		String currentPlanName = (currentPlan != null) ? currentPlan.name : "no plan";
		String currentNodeName = (currentNode != null) ? currentNode.name : "no node";
		Logger.debug("Switching user %s from plan '%s' to plan '%s' (node '%s' to node '%s')",
				u.emailAddress, currentPlanName, newPlan.name, currentNodeName, newNode.name);
			
		PlanSwitcherJobResult res = new PlanSwitcherJobResult();
		try {
			if (_ps.inProgress) {
				Logger.debug("Aborting plan switch (switch already in progress)");
				return res;
			}
			if (!_ps.canSwitch()) {
				Logger.debug("Aborting plan switch (user cannot switch plans until invoice is paid)");
				return res;
			}
			_ps.inProgress = true;
			_ps.save();
			Logger.debug("Stopping transmission");
			try {
				u.getTransmission().stop();
			} catch (MessageException ex) {
				//dont care if transmission cant be stopped, most of the time its because "Daemon not running to start with"
			}
			if (currentNode == null || newNode.id == currentNode.id) {
				Logger.debug("Switching to same node");
				//no migration necessary
				//add a FreeSlot to the old plan (FreeSlot on the new plan should already have been removed)
				if (currentPlan != null) {
					Logger.debug("Incrementing FreeSlot count on current plan");
					FreeSlot forCurrent = FreeSlot.getForNodeAndPlan(a.getNode(), currentPlan);
					if (forCurrent != null) {
						forCurrent.freeSlots += 1;
						forCurrent.save();
					}
				}
			} else {
				Logger.info("Plan is on new node; migrating");
				//set new port on account
				int port = Account.getAvailableTransmissionPort(newNode);
				a.transmissionPort = port;
				a.save();
				
				//transfer data to new node
				NodeMigrationJob nmj = new NodeMigrationJob(u, newNode);
				NodeMigrationJobResult nmjRes = nmj.doJobWithResult();
			}
			Logger.info("Setting PlanSwitch to complete");
			_ps.inProgress = false;
			_ps.switchDateUtc = new Date();
			_ps.save();
			
			//once plans are switched, make sure transmission is running
			Logger.info("Saving User and Account");
			a.node = newNode;
			a.save();
			u.save();
			
			//set user to new plan
			Logger.debug("Switching to new plan");
			u.setPlan(newPlan);		
			
			Logger.info("Starting transmission");
			u.getTransmission().start();
			
			u.addUserMessage("Migration completed", "Migration to new plan completed successfully.");
		} catch (Exception ex) {
			_ps.error = ExceptionUtils.getStackTrace(ex);
			_ps.save();
			res.error = ex;
		}
		return res;		
	}
	
	public class PlanSwitcherJobResult extends JobResult {
		
	}
	
}

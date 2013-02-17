package controllers;

import com.openseedbox.code.MessageException;
import com.openseedbox.code.Util;
import com.openseedbox.code.Util.SelectItem;
import com.openseedbox.jobs.CleanupJob;
import com.openseedbox.jobs.HealthCheckJob;
import com.openseedbox.jobs.NodePollerJob;
import com.openseedbox.jobs.torrent.RemoveTorrentJob;
import com.openseedbox.models.JobEvent;
import com.openseedbox.models.Node;
import com.openseedbox.models.Plan;
import com.openseedbox.models.Torrent;
import com.openseedbox.models.User;
import com.openseedbox.models.UserTorrent;
import com.openseedbox.mvc.ISelectListItem;
import java.util.ArrayList;
import java.util.List;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Before;

public class Admin extends Base {

	@Before
	public static void checkAdmin() {
		User u = getCurrentUser();
		if (u == null || !u.isAdmin()) {
			redirect("/auth/logout");
		}
	}

	public static void index() {
		redirect("/admin/stats");
	}

	public static void stats() {
		String active = "stats";
		long nodeCount = Node.count();
		long userCount = User.count();
		long torrentCount = Torrent.count();
		long totalSpaceBytes = Node.getTotalSpaceBytes();
		long usedSpaceBytes = Node.getUsedSpaceBytes();
		renderTemplate("admin/stats.html", active, nodeCount, userCount, torrentCount,
				  totalSpaceBytes, usedSpaceBytes);
	}

	public static void nodes() {
		String active = "nodes";
		List<Node> nodes = Node.all().fetch();
		renderTemplate("admin/nodes.html", active, nodes);
	}
	
	public static void createNode() {
		String active = "nodes";
		renderTemplate("admin/node-edit.html", active);
	}

	public static void editNode(long id) {		
		String active = "nodes";
		Node node = Node.findById(id);		
		renderTemplate("admin/node-edit.html", active, node);
	}

	public static void updateNode(@Valid Node node) {
		if (!Validation.hasErrors()) {			
			node.insertOrUpdate();
			setGeneralMessage("Node '" + node.getName() + "' created/updated successfully.");
			nodes();
		}				
		Validation.keep();
		params.flash();
		if (node.isNew()) {
			createNode();
		}
		editNode(node.getId());
	}

	public static void deleteNode(long id) {
		Node n = Node.findById(id);
		n.delete();
		setGeneralMessage("Node '" + n.getName() + "' deleted successfully.");
		nodes();
	}

	public static void plans() {
		String active = "plans";
		List<Plan> plans = Plan.all().order("monthlyCost").fetch();
		renderTemplate("admin/plans.html", active, plans);
	}
	
	public static void createPlan() {
		String active = "plans";
		renderTemplate("admin/plan-edit.html", active);
	}

	public static void editPlan(long id) {
		String active = "plans";
		Plan plan = Plan.findById(id);
		renderTemplate("admin/plan-edit.html", active, plan);
	}

	public static void updatePlan(@Valid Plan plan) {
		if (!Validation.hasErrors()) {
			plan.insertOrUpdate();
			setGeneralMessage("Plan '" + plan.getName() + "' created/updated successfully.");
			plans();
		}
		Validation.keep();
		params.flash();
		if (plan.isNew()) {
			createPlan();
		}
		editPlan(plan.getId());
	}

	public static void deletePlan(long id) {		
		 Plan p = Plan.findById(id);
		 p.delete();
		 setGeneralMessage("Plan '" + p.getName() + "' deleted successfully.");
		 plans();
	}
	
	public static void users() {		
		 String active = "users";
		 List<User> users = User.all().fetch();	 
		 renderTemplate("admin/users.html", active, users);
	}	

	public static void editUser(long id) {		
		 String active = "users";
		 User user = User.findById(id);
		 List<Plan> all_plans = Plan.all().filter("visible", true).fetch();
		 List<Node> all_nodes = Node.getActiveNodes();
		 List<ISelectListItem> plans = new ArrayList<ISelectListItem>();
		 List<ISelectListItem> nodes = new ArrayList<ISelectListItem>();
		 plans.add(new SelectItem("None", "", false));
		 plans.addAll(Util.toSelectItems(all_plans, "id", "name"));		 
		 nodes.add(new SelectItem("None", "", false));
		 nodes.addAll(Util.toSelectItems(all_nodes, "id", "name"));
		 renderTemplate("admin/user-edit.html", active, user, plans, nodes);
	}

	public static void updateUser(@Valid User user) {
		if (!Validation.hasErrors()) {
			user.save();
			setGeneralMessage("User updated successfully.");
			users();
		}
		Validation.keep();
		params.flash();
		editUser(user.getId());
	}
	
	public static void deleteUser(long id) {
		User u = User.findById(id);
		u.delete();
		//TODO: delete all UserTorrents and invoices and shit
		List<UserTorrent> uts = u.getTorrents();
		for (UserTorrent ut : uts) {
			new RemoveTorrentJob(ut.getTorrentHash(), u.getId()).now();
		}
		setGeneralMessage("User '" + u.getEmailAddress() + "' deleted.");
		users();
	}
	
	public static void restartBackend(long id) {
		try {
			Node n = Node.getByKey(id);
			n.getNodeBackend().restart();
			setGeneralMessage("Restart request sent successfully!");
		} catch (MessageException ex) {
			setGeneralErrorMessage(ex.getMessage());
		}
		nodes();
	}
	
	public static void nodeStatus(long id) {
		Node n = Node.findById(id);
		result(n.getNodeStatus());
	}
	
	public static void jobs() {
		renderArgs.put("active", "jobs");
		List<JobEvent> pollerJobs = JobEvent.getLast(NodePollerJob.class, 5);
		List<JobEvent> healthCheckJobs = JobEvent.getLast(HealthCheckJob.class, 5);
		List<JobEvent> cleanupJobs = JobEvent.getLast(CleanupJob.class, 5);
		render("admin/jobs.html", pollerJobs, healthCheckJobs, cleanupJobs);
	}
	
	public static void runJobManually(String type) {
		if (type.equals("poller")) {
			new NodePollerJob().now();
		} else if (type.equals("healthcheck")) {
			new HealthCheckJob().now();
		} else if (type.equals("cleanup")) {
			new CleanupJob().now();
		}
		setGeneralMessage("Job started.");
		jobs();
	}
}

package controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.openseedbox.code.MessageException;
import com.openseedbox.code.Util;
import com.openseedbox.code.Util.SelectItem;
import com.openseedbox.jobs.NodePollerJob;
import com.openseedbox.jobs.torrent.RemoveTorrentJob;
import com.openseedbox.models.*;
import com.openseedbox.models.util.SafeDeleteBase;
import com.openseedbox.mvc.ISelectListItem;

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
		safelyDeleteById(new SafeDeleteBase<Node>() {
			@Override
			public Node apply() { return new Node(); }
			@Override
			public Node findById(long id) { return Node.findById(id); }
			@Override
			public String nameInsteadId(Node d) { return d.getName(); }
			@Override
			public void afterFinish() { nodes(); }
		}, id);
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
		safelyDeleteById(new SafeDeleteBase<Plan>() {
			@Override
			public Plan apply() { return new Plan(); }
			@Override
			public Plan findById(long id) { return Plan.findById(id); }
			@Override
			public String nameInsteadId(Plan d) { return d.getName(); }
			@Override
			public void afterFinish() { plans(); }
		}, id);
	}

	public static void users() {
		 String active = "users";
		 List<User> users = User.all().fetch();
		 renderTemplate("admin/users.html", active, users);
	}

	public static void editUser(long id) {
		 String active = "users";
		 User user = User.findById(id);
		 List<Plan> all_plans = Plan.all().fetch();
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
			if (user.equals(getCurrentUser())) {
				Account.uncacheUser();
			}
			users();
		}
		Validation.keep();
		params.flash();
		editUser(user.getId());
	}

	public static void deleteUser(long id) {
		safelyDeleteById(new SafeDeleteBase<User>() {
			@Override
			public User apply() { return new User(); }
			@Override
			public User findById(long id) { return User.findById(id); }
			@Override
			public boolean vetoFilter(User d) { return getCurrentUser().equals(d); }
			@Override
			public void afterVeto() {
				setGeneralErrorMessage("Ask another admin to delete your account!");
			}
			@Override
			public void afterDelete(User d) {
				//TODO: delete all UserTorrents and invoices and shit
				List<UserTorrent> uts = d.getTorrents();
				for (UserTorrent ut : uts) {
					new RemoveTorrentJob(ut.getTorrentHash(), d.getId()).now();
				}
			}
			@Override
			public String nameInsteadId(User d) { return d.getEmailAddress(); }
			@Override
			public void afterFinish() { users(); }
		}, id);
	}

	private static void safelyDeleteById(SafeDeleteBase stages, long id) {
		ModelBase c = stages.apply();
		ModelBase d = stages.findById(id);
		if (stages.vetoFilter(d)) {
			stages.afterVeto();
		} else if (d != null) {
			d.delete();
			stages.afterDelete(d);
			setGeneralMessage(c.getClass().getSimpleName() + " '" + stages.nameInsteadId(d) + "' deleted successfully.");
		} else {
			setGeneralErrorMessage("No " + c.getClass().getSimpleName() + " found with id " + id + "!");
		}
		stages.afterFinish();
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
		try {
			result(n.getNodeStatus());
		} catch (Exception ex) {
			resultError(ex.getMessage());
		}
	}

	public static void jobs() {
		renderArgs.put("active", "jobs");
		List<JobEvent> pollerJobs = JobEvent.getLastList(Arrays.asList(
				NodePollerJob.class, NodePollerJob.NodePollerWorker.class), 30);
		render("admin/jobs.html", pollerJobs);
	}

	public static void runJobManually(String type) {
		if (type.equals("poller")) {
			new NodePollerJob().now();
		}
		setGeneralMessage("Job started.");
		jobs();
	}
}

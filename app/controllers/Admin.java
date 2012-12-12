package controllers;

import com.openseedbox.code.MessageException;
import com.openseedbox.code.Util;
import com.openseedbox.code.Util.SelectItem;
import com.openseedbox.mvc.ISelectListItem;
import java.util.ArrayList;
import java.util.List;
import models.*;
import org.apache.commons.io.FileUtils;
import play.Play;
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
		int torrentCountAverage = -1;//TODO - figure out how to do this in Siena
		renderTemplate("admin/stats.html", active, nodeCount, userCount, torrentCount,
				  torrentCountAverage);
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
		 List<ISelectListItem> plans = new ArrayList<ISelectListItem>();
		 plans.add(new SelectItem("None", "", false));
		 plans.addAll(Util.toSelectItems(all_plans, "id", "name"));		 
		 renderTemplate("admin/user-edit.html", active, user, plans);
	}

	public static void updateUser(@Valid User user) {
		if (!Validation.hasErrors()) {
			user.update();
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
		setGeneralMessage("User '" + u.getEmailAddress() + "' deleted.");
		users();
	}
	
	public static void restartBackend(long id) {
		Node n = Node.getByKey(id);
		n.getNodeBackend().restart();
		setGeneralMessage("Restart request sent successfully!");
		nodes();
	}
	
	public static void nodeStatus(long id) {
		Node n = Node.findById(id);
		result(n.getNodeStatus());
	}
}

package controllers;

import code.MessageException;
import code.transmission.Transmission;
import java.util.ArrayList;
import java.util.List;
import models.*;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Before;
import siena.SienaException;

public class AdminController extends BaseController {
	
	@Before
	public static void checkAdmin() {
		User u = getCurrentUser();
		if (u == null || !u.isAdmin) {
			redirect("/auth/logout");
		}
	}
	
	public static void index() {
		redirect("/admin/stats");
	}
	
	public static void stats() {
		String active = "stats";
		renderTemplate("admin/stats.html", active);
	}
	
	public static void nodes() {
		String active = "nodes";
		List<Node> nodes = Node.all().fetch();
		renderTemplate("admin/nodes.html", active, nodes);
	}
	
	public static void editNode(Node node) {
		String active = "nodes";
		renderTemplate("admin/node-edit.html", active, node);		
	}
	
	public static void updateNode(@Valid Node node) {
		if (!Validation.hasErrors()) {
			if (node.id == null) {
				node.insert();
			} else {
				node.save();
			}
			nodes();
		}
		Validation.keep();
		editNode(node);
	}
	
	public static void deleteNode(Long id) {
		Node n = Node.getByKey(id);
		n.delete();
		nodes();
	}
	
	private static void users() {
		try {
			users(-1L);
		} catch (MessageException ex) {
			BaseController.onException(ex);
		}
	}
	
	public static void users(Long node_filter) throws MessageException {
		String active = "users";
		if (node_filter == null) {
			node_filter = -1L;
		}
		List<User> all_users = User.all().fetch();
		List<User> users = new ArrayList<User>();
		if (node_filter > 0) {
			for (User u : all_users) {
				Node n = u.getNode();
				if (n != null && n.id == node_filter) {
					users.add(u);
				}
			}
		} else {
			users = all_users;
		}
		List<Node> nodes = Node.all().fetch();
		Node dummy = new Node();
		dummy.id = -1L;
		dummy.name = "All Nodes";
		nodes.add(0, dummy);
		renderTemplate("admin/users.html", active, users, nodes, node_filter);
	}	
	
	public static void plans() {
		String active = "plans";
		List<Plan> plans = Plan.all().order("monthlyCost").fetch();
		renderTemplate("admin/plans.html", active, plans);
	}
	
	public static void editPlan(Plan plan) {
		String active = "plans";
		renderTemplate("admin/plan-edit.html", active, plan);
	}
	
	public static void updatePlan(@Valid Plan plan) {
		if (params.get("button").equals("cancel")) {
			plans();
		}
		if (params.get("plan.visible") == null) {
			plan.visible = false;
		}
		if (!Validation.hasErrors()) {
			try {
				if (plan.id == null) {
					plan.insert();
				} else {
					plan.save();
				}
				plans();
			} catch (SienaException ex) {
				if (ex.getMessage().contains("Duplicate entry")) {
					Validation.addError("plan.name", "Name already exists!");
				}
			}
		}
		Validation.keep();
		editPlan(plan);	
	}
	
	public static void deletePlan(Long id) {
		Plan p = Plan.findById(id);
		p.delete();
		plans();
	}
	
	public static void editUser(long id) {
		String active = "users";
		User user = User.findById(id);
		List<Plan> plans = Plan.getVisiblePlans();
		List<Node> nodes = Node.all().filter("active", true).fetch();
		renderTemplate("admin/user-edit.html", active, user, plans, nodes);
	}
	
	public static void editUserPost(User u, long plan_id, long node_id) throws MessageException {
		if (params.get("button").equals("cancel")) {
			users();
		}
		User us = User.findById(u.id);
		us.isAdmin = (params.get("u.isAdmin") != null);
		Account a = us.getPrimaryAccount();
		Node n = Node.findById(node_id);
		us.setNode(n);
		if (a != null) {
			a.transmissionPort = Account.getAvailableTransmissionPort(n);
			a.save();
		}
		us.save();
		users();
	}
	
	public static void slots() {
		String active = "slots";
		List<FreeSlot> slots = FreeSlot.all().fetch();
		render("admin/slots.html", active, slots);
	}
	
	public static void editSlot(long slotId) {
		String active = "slots";
		List<Node> nodes = Node.all().filter("active", true).fetch();
		List<Plan> plans = Plan.getVisiblePlans();
		FreeSlot slot = FreeSlot.findById(slotId);
		
		render("admin/slot-edit.html", active, slot, nodes, plans);
	}
	
	public static void updateSlot(@Valid FreeSlot slot) {
		if (!Validation.hasErrors()) {
				try {
				if (slot.id == null) {
					slot.insert();
				} else {
					slot.save();
				}
				slots();
			} catch (SienaException ex) {
				if (ex.getMessage().contains("Duplicate entry")) {
					Validation.addError("slot.node", "Plan/Node combination already exists!");
				}				
			}
		}
		Validation.keep();
		editSlot(slot.id);
	}
	
	public static void deleteSlot(Long id) {
		FreeSlot.findById(id).delete();
		slots();
	}
	
	public static void stopTransmission(Long userId) {
		User u = User.getByKey(userId);
		Transmission t = u.getTransmission();
		try {
			if (t != null) {
				t.stop();
			}
		} catch (MessageException ex) {
			addGeneralError(ex);
		}
		Validation.keep();
		users();
	}
	
	public static void startTransmission(Long userId) {
		User u = User.getByKey(userId);
		Transmission t = u.getTransmission();
		try {
			if (t != null) {
				t.start();
			}
			setGeneralMessage("Start request sent! Refresh the page in a few moments to see the status change.");
		} catch (MessageException ex) {
			addGeneralError(ex);
		}
		Validation.keep();
		users();
	}	
	
	public static void restartTransmission(Long userId) {
		User u = User.getByKey(userId);
		Transmission t = u.getTransmission();
		try {
			if (t != null) {
				t.restart();
			}
		} catch (MessageException ex) {
			Validation.addError("general", ex.getMessage());
		}
		Validation.keep();
		users();
	}
	
	public static void updateTransmissionConfig(String key, String value, String type) {
		String[] types = new String[] { "String", "Integer", "Boolean" };
		String active = "nodes";
		render("admin/update-transmission-config.html", active, types, key, value, type);
	}
	
	public static void updateTransmissionConfigPost(@Required String key, @Required String value, @Required String type) {
		if (!validation.errorsMap().isEmpty()) {
			Validation.keep();
		} else {
			setGeneralMessage("Config pushed to all nodes.");
		}
		updateTransmissionConfig(key, value, type);
	}
	
	public static void runPrepareScript() {
		
	}

}

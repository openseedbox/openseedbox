package controllers;

import code.MessageException;
import code.transmission.Transmission;
import java.util.List;
import models.FreeSlot;
import models.Node;
import models.Plan;
import models.User;
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
		String active = "plans";
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
	
	public static void users() {
		String active = "users";
		List<User> users = User.all().fetch();
		renderTemplate("admin/users.html", active, users);
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
		renderTemplate("admin/user-edit.html", active, user);
	}
	
	public static void editUserPost(User u) {
		if (params.get("button").equals("cancel")) {
			users();
		}
		User us = User.findById(u.id);
		us.isAdmin = (params.get("u.isAdmin") != null);
		us.save();
		users();
	}
	
	public static void slots() {
		String active = "slots";
		List<FreeSlot> slots = FreeSlot.all().fetch();
		render("admin/slots.html", active, slots);
	}
	
	public static void editSlot(FreeSlot slot) {
		String active = "slots";
		List<Node> nodes = Node.all().filter("active", true).fetch();
		List<Plan> plans = Plan.all().filter("visible", true).fetch();
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
		editSlot(slot);
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
			Validation.addError("general", ex.getMessage());
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
		} catch (MessageException ex) {
			Validation.addError("general", ex.getMessage());
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

}

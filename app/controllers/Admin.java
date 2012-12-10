package controllers;

import com.openseedbox.mvc.controllers.Base;
import com.openseedbox.code.MessageException;
import com.openseedbox.code.Util;
import com.openseedbox.backend.BackendConfig;
import com.openseedbox.backend.BackendManager;
import com.openseedbox.backend.BackendManager.SupportedBackend;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import models.*;
import models.Node.ISshOutputReporter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.db.jpa.JPA;
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
		long torrentCountAverage =
				  ((BigDecimal) JPA.em().createNativeQuery("SELECT IFNULL(AVG(torrent_count), 0) FROM (SELECT count(id) as torrent_count FROM torrent GROUP BY user_id) s").getSingleResult()).longValue();
		renderTemplate("admin/stats.html", active, nodeCount, userCount, torrentCount,
				  torrentCountAverage);
	}

	public static void nodes() {
		String active = "nodes";
		List<Node> nodes = Node.all().fetch();
		renderTemplate("admin/nodes.html", active, nodes);
	}

	public static void editNode(long id) {
		String active = "nodes";
		Node node = Node.findById(id);
		List<SupportedBackend> backends = BackendManager.getSupportedBackends();
		renderTemplate("admin/node-edit.html", active, node, backends);
	}

	public static void updateNode(@Valid Node node) {
		if (!Validation.hasErrors()) {
			boolean isNew = node.getId() == null;
			node.save();
			if (isNew) {
				prepareNode(node.getId());
			}
			nodes();
		}
		
		List<SupportedBackend> backends = BackendManager.getSupportedBackends();
		String active = "nodes";
		renderTemplate("admin/node-edit.html", active, node, backends);
	}

	public static void deleteNode(Long id) {
		Node n = Node.findById(id);
		n.delete();
		nodes();
	}

	private static void users() {
		try {
			users(-1L);
		} catch (MessageException ex) {
			Base.onException(ex);
		}
	}

	public static void users(Long node_filter) throws MessageException {
		/*
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
		 renderTemplate("admin/users.html", active, users, nodes, node_filter);*/
	}

	public static void plans() {
		String active = "plans";
		List<Plan> plans = null;// Plan.all().order("monthlyCost").fetch();
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
			plan.setVisible(false);
		}
		if (!Validation.hasErrors()) {
			plan.save();
			plans();
		}
		Validation.keep();
		editPlan(plan);
	}

	public static void deletePlan(Long id) {
		/*
		 Plan p = Plan.findById(id);
		 p.delete();
		 plans();*/
	}

	public static void editUser(long id) {
		/*
		 String active = "users";
		 User user = User.findById(id);
		 List<Plan> plans = Plan.getVisiblePlans();
		 List<Node> nodes = Node.all().filter("active", true).fetch();
		 renderTemplate("admin/user-edit.html", active, user, plans, nodes);*/
	}

	public static void editUserPost(User u, long plan_id, long node_id) throws MessageException {
		/*
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
		 users();*/
	}

	public static void slots() {
		/*
		 String active = "slots";
		 List<FreeSlot> slots = FreeSlot.all().fetch();
		 render("admin/slots.html", active, slots);*/
	}

	public static void editSlot(long slotId) {
		/*
		 String active = "slots";
		 List<Node> nodes = Node.all().filter("active", true).fetch();
		 List<Plan> plans = Plan.getVisiblePlans();
		 FreeSlot slot = FreeSlot.findById(slotId);
		
		 render("admin/slot-edit.html", active, slot, nodes, plans);*/
	}

	/*
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
	 } catch (MessageRuntimeException ex) {
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
	 } catch (MessageRuntimeException ex) {
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
	 } catch (MessageRuntimeException ex) {
	 Validation.addError("general", ex.getMessage());
	 }
	 Validation.keep();
	 users();
	 }*/
	public static void updateBackendConfig(boolean redirectToCreateNode) {
		String active = "nodes";
		BackendConfig config = Settings.getBackendConfig();
		render("admin/update-backend-config.html", active, config, redirectToCreateNode);
	}

	public static void updateBackendConfigPost(@Valid BackendConfig config, String blocklistUrls, boolean redirectToCreateNode) {
		if (!validation.errorsMap().isEmpty()) {
			Validation.keep();
			updateBackendConfig(redirectToCreateNode);
		} else {
			if (!StringUtils.isEmpty(blocklistUrls)) {
				config.setBlocklistUrls(Arrays.asList(blocklistUrls.split("\n")));
			} else {
				config.setBlocklistUrls(new ArrayList<String>());
			}
			Settings.storeBackendConfig(config);
			setGeneralMessage("Backend config updated.");
		}
		if (redirectToCreateNode) {
			editNode(-1);
		}
		nodes();
	}
	
	public static void nodeStatus(long id) {
		Node n = Node.findById(id);
		result(n.getNodeStatus());
	}

	public static void runPrepareScript() {
	}

	public static void prepareNode(long id) {
		Node node = Node.findById(id);
		String active = "nodes";
		render("admin/node-prepare.html", node, active);
	}

	public static void prepareNodeIframe(long id) {
		Node n = Node.findById(id);
		write("Checking if node is reachable...");
		if (!n.getNodeStatus().isReachable()) {
			writeLine(" it isnt. Aborting.");
			return;
		}
		writeLine(" it is.");
		writeLine(String.format("Running prepare script on node '%s'", n.getName()));
		try {
			File hostkey = Play.getFile("conf/host.key");
			File hostcert = Play.getFile("conf/host.cert");
			write("Writing prepare-node.sh...");
			n.writeFileToNode(getPrepareNodeScript(n),
					  String.format("/home/%s/prepare-node.sh", n.getUsername()), 0755);
			writeLine("done.");
			write("Transferring host.key...", hostkey.getAbsolutePath());
			n.transferFileToNode(hostkey, "~/");
			writeLine("done.");
			write("Transferring host.cert...", hostcert.getAbsolutePath());
			n.transferFileToNode(hostcert, "~/");
			writeLine("done.");
			String command = String.format("echo %s | sudo -S /home/%s/prepare-node.sh 2>&1",
					  Util.shellEscape(n.getRootPassword()), Util.shellEscape(n.getUsername()));
			writeLine("Executing '%s'", command);
			n.executeCommand(command, new ISshOutputReporter() {
				public void onOutputLine(String line) {
					writeLine(line);
				}
			});
		} catch (Exception ex) {
			writeLine("Unable to transfer file to node: %s", Util.getStackTrace(ex));
		}
		writeLine("All done.");
	}
	
	private static String getPrepareNodeScript(Node n) {
		try {
			String script = FileUtils.readFileToString(Play.getFile("conf/prepare-node.sh"));
			BackendConfig bc = Settings.getBackendConfig();
			script = script.replace("${config.completeFolder}", bc.getCompleteFolder());
			script = script.replace("${config.incompleteFolder}", bc.getIncompleteFolder());
			script = script.replace("${config.torrentFolder}", bc.getTorrentFolder());
			script = script.replace("${config.baseFolder}", bc.getBaseFolder());
			script = script.replace("${config.ipAddress}", n.getIpAddress());
			return script;
		} catch (Exception ex) {
			throw new MessageException("Unable to get prepare node script. " + ex.getMessage());
		}
	}

	public static void prepareNodeBackendIframe(long id) {
	}
}

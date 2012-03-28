package controllers;

import code.MessageException;
import com.google.gson.annotations.SerializedName;
import controllers.securesocial.SecureSocial;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Node;
import models.Transmission.TransmissionConfig;
import models.User;
import play.mvc.Before;
import play.mvc.With;

@With(SecureSocial.class)
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
		SystemStats stats = new SystemStats();
		renderTemplate("admin/stats.html", active, stats);
	}
	
	public static void nodes() {
		String active = "nodes";
		List<Node> nodes = Node.all().fetch();
		renderTemplate("admin/nodes.html", active, nodes);
	}
	
	public static void users() {
		String active = "users";
		List<User> users = User.all().fetch();
		renderTemplate("admin/users.html", active, users);
	}	
	
	public static void editUser(long id) {
		String active = "users";
		User user = User.findById(id);
		renderTemplate("admin/user-edit.html", active, user);
	}
	
	
	public static void editUserPost(User u) {
		User us = User.findById(u.id);
		us.isAdmin = (params.get("u.isAdmin") != null);
		us.maxDiskspaceGB = u.maxDiskspaceGB;
		us.save();
		users();
	}
	
	public static void renderNodeRow(long id) {
		Node _arg = Node.getByKey(id);
		renderTemplate("tags/node-row.html", _arg);
	}
	
	public static void renderEditConfigDialog(long id) {
		Node node = Node.getByKey(id);
		try {
			TransmissionConfig tc = node.getTransmission().getConfig();
			Field[] fields = tc.getClass().getDeclaredFields();
			Map<String, Field> relevantFields = new HashMap<String, Field>();
			for (Field f : fields) {
				SerializedName sn = (SerializedName) f.getAnnotation(SerializedName.class);
				if (sn != null && !sn.value().equals("rpc-password")) {
					relevantFields.put(sn.value(), f);
				}
			}
			renderTemplate("admin/node-edit-config.html", tc, relevantFields, node);
		} catch (MessageException ex) {
			resultError(ex.getMessage());
		}	
	}
	
	public static void updateNodeConfig(long id, TransmissionConfig tc) throws MessageException {
		Node node = Node.getByKey(id);
		tc.save(node);
		node.getTransmission().reloadConfig();
		result(true);
	}
	
	public static class SystemStats {
		public String totalNodes;
		public String totalUsers;
		public String totalTorrents;
		public String averageTorrentsPerUser;
		public String averageNodeUptime;
		public String totalSpaceUsed;
		public String averageSpaceUsedPerNode;
	}

}

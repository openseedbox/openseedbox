package controllers;

import code.MessageException;
import java.util.List;
import models.Node;

/**
 *
 * @author erin
 */
public class NodeController extends BaseController {

	public static void status(long id) {
		Node n = getNode(id);
	}

	public static void list() {
			List<Node> nodes = Node.all().fetch();
			for (Node n : nodes) {
				n.uptime = n.getUptime();
			}
			result(nodes);
	}

	private static Node getNode(long id) {
		Node n = Node.findById(id);
		if (n == null) {
			resultError("No such node with id: " + id);
		}
		return n;
	}
}

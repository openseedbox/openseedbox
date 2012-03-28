package controllers;

import code.MessageException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import models.Node;
import models.Transmission;
import models.Transmission.TransmissionConfig;
import models.User;

public class TransmissionController extends AdminController {
	
	public static void config(long id) throws MessageException {
		Node n = getNode(id);
		TransmissionConfig c = n.getTransmission().getConfig();
		result(c);
	}
	
	public static void setConfigItem(long id, String key, String value) throws MessageException {
		Node n = getNode(id);
		TransmissionConfig c = n.getTransmission().getConfig();
		try {
			Field f = TransmissionConfig.class.getDeclaredField(key);
			if (f.getType() == Boolean.class) {
				f.set(c, Boolean.valueOf(value));
			} else {
				f.set(c, value);
			}
			c.save(n);
		} catch (NoSuchFieldException | IllegalAccessException ex) {
			throw new MessageException("Unable to set.");
		}
		result(c);
	}
	
	public static void setDefaultConfig(long id) throws MessageException {
		Node n = getNode(id);
		TransmissionConfig c = n.getTransmission().getConfig();
		c.setDefaults();
		c.save(n);
		n.getTransmission().reloadConfig();
		result(c);
	}
	
	public static void status(long id) {
		Node n = getNode(id);
		Transmission t = n.getTransmission();
		Map<String, Object> res = new HashMap<>();
		res.put("transmission-daemon-running", t.isRunning());
		res.put("node-uptime", n.uptime);
		res.put("node-user-count", User.all().filter("node", n));
		result(res);
	}
	
	public static void start(long id) {
		Node n = getNode(id);
		n.getTransmission().start();
		result("Transmission-daemon started on node: " + n.name);
	}
	
	public static void stop(long id) {
		Node n = getNode(id);
		n.getTransmission().stop();
		result("Transmission-daemon stopped on node: " + n.name);		
	}
	
	public static void restart(long id) {
		Node n = getNode(id);
		n.getTransmission().stop();
		n.getTransmission().start();
		result("Transmission-daemon restarted on node: " + n.name);
	}
	
	public static void reload(long id) {
		Node n = getNode(id);
		n.getTransmission().reloadConfig();
		result("Transmission-daemon config reloaded on node: " + n.name);
	}
	
	public static void listTorrents(long id) throws MessageException {
		Node n = getNode(id);
		result(n.getTransmission().getTorrents());
	}
	
	public static void addTorrent(long id, String urlOrMagnet) throws MessageException {
		Node n = getNode(id);
		if (urlOrMagnet != null && !urlOrMagnet.isEmpty()) {
			result(n.getTransmission().addTorrent(urlOrMagnet));	
		}
		resultError("Please specify a valid URL or magnet link.");
	}
	
	public static void removeTorrent(long id, String torrentHash) throws MessageException {
		Node n = getNode(id);
		if (torrentHash != null && !torrentHash.isEmpty()) {
			result(n.getTransmission().removeTorrent(torrentHash, false));
		}
		resultError("Please specify a torrentHash (transmission-id or sha-1 hash)");
	}
	
	public static void listUserTorrents(long id, long userid) {
		
	}
	
	private static Node getNode(long id) {
		Node n = Node.getByKey(Node.class, id);
		if (n == null) {
			resultError("Unable to find node with id " + id);
		}
		return n;
	}

}

package controllers;

import java.util.List;
import models.Node;
import models.Torrent;

/**
 *
 * @author erin
 */
public class ClientFilesController extends ClientController {
	
	public static void index() {
		List<Torrent> torrents = getCurrentUser().getTorrents();
		renderTemplate("client/files.html", torrents);
	}
	
	public static void zip(String directoryName) {
		
	}
	
	public static void download(String fileName) {
		Node n = getCurrentUser().getNode();
		String url = String.format("http://%s/torrents/complete/%s", n.ipAddress, fileName);
		redirect(url);
	}
	
	

}

package controllers;

import code.jobs.GetTorrentsJob;
import code.jobs.GetTorrentsJob.GetTorrentsJobResult;
import java.util.List;
import models.Node;
import models.Torrent;
import play.libs.F.Promise;

public class ClientFilesController extends ClientController {
	
	public static void index() {
		Promise<GetTorrentsJobResult> job = new GetTorrentsJob(getActiveAccount()).now();
		GetTorrentsJobResult res = await(job);
		if (res.hasError()) {
			addGeneralError(res.error);
		}
		List<Torrent> torrents = res.torrents;
		renderTemplate("clientfiles/index.html", torrents);
	}
	
	public static void singleTorrent(String hashString) {
		Promise<GetTorrentsJobResult> job = new GetTorrentsJob(getActiveAccount(), null, hashString).now();
		GetTorrentsJobResult res = await(job);
		if (res.hasError()) {
			addGeneralError(res.error);
		}
		Torrent torrent = res.torrents.get(0);
		renderTemplate("clientfiles/single-torrent.html", torrent);		
	}
}

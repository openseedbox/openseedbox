package com.openseedbox.jobs;

import com.openseedbox.backend.ITorrent;
import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.backend.node.NodeBackend;
import com.openseedbox.models.JobEvent;
import com.openseedbox.models.Node;
import com.openseedbox.models.Torrent;
import com.openseedbox.models.TorrentEvent;
import com.openseedbox.models.UserTorrent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import play.jobs.Every;

@Every("5mn")
@JobName("Cleanup Job")
public class CleanupJob extends LoggedJob<JobEvent> {		

	@Override
	protected Object doGenericJob() throws Exception {
		return false;
/*
		//clean up TorrentEvents that are older than 10 mins
		List<TorrentEvent> event = TorrentEvent.getOlderThanMinutes(10);
		int deletedEvents = event.size();
		TorrentEvent.batch().delete(event);
		
		Map<Node, List<ITorrent>> allTorrentsInSystem = new HashMap<Node, List<ITorrent>>();
		
		//clean up torrents that are in the backend but not in the db
		int inBackendButNotInDb = 0;		
		List<Node> nodes = Node.getActiveNodes();
		boolean aNodeIsDown = false;
		for (Node n : nodes) {
			if (!n.isReachable()) {
				aNodeIsDown = true;
				continue;
			}
			ITorrentBackend backend = n.getNodeBackend();			
			List<ITorrent> allTorrents = backend.listTorrents();
			allTorrentsInSystem.put(n, allTorrents); //this is so we dont have to re-query all the nodes in the 'clean up torrents in db but not in backend' step
			List<String> removeMe = new ArrayList<String>();
			for (ITorrent t : allTorrents) {				
				ITorrent inDb = Torrent.getByHash(t.getTorrentHash());
				if (inDb == null) {
					removeMe.add(t.getTorrentHash());
				}
			}
			if (!removeMe.isEmpty()) {
				inBackendButNotInDb = removeMe.size();
				backend.removeTorrent(removeMe);				
			}
			if (backend instanceof NodeBackend) {
				NodeBackend b = (NodeBackend) backend;
				b.cleanup(); //remove any directories on the backend that arent in transmission for some reason
			}
		}
		
		//clean up torrents that are in the db but not the backend
		int inDbButNotInBackend = 0;
		if (!aNodeIsDown) { //if we ran this while an active node is down, we could potentially remove legitimate torrents from the db
			List<Torrent> torrents = Torrent.findAll();
			List<Torrent> deleteMe = new ArrayList<Torrent>();
			List<UserTorrent> deleteMeToo = new ArrayList<UserTorrent>();
			for (Torrent t : torrents) {
				if (!appearsSomewhere(allTorrentsInSystem, t)) {
					deleteMe.add(t);
					deleteMeToo.addAll(UserTorrent.getByHash(t.getTorrentHash()));
				}
			}
			if (!deleteMe.isEmpty()) {
				inDbButNotInBackend = deleteMe.size();
				Torrent.batch().delete(deleteMe);			
			}
			if (!deleteMeToo.isEmpty()) {
				UserTorrent.batch().delete(deleteMeToo);
			}
		}
		//clean up job log
		long now = new Date().getTime();
		now -= 1000 * 60 * 60 * 24; //1 day
		JobEvent.deleteOlderThan(new Date(now));
		
		return String.format("Deleted %s torrent events, %s torrents in DB but not in backend and %s torrents in backend but not in DB",
				  deletedEvents, inDbButNotInBackend, inBackendButNotInDb);
*/
	}

	@Override
	protected void logResult(GenericJobResult res, JobEvent event) {
		if (!res.hasError()) {
			event.setSuccessful(true);
			event.setStackTrace(res.getResult().toString());
		}
		super.logResult(res, event);
	}		
	
	private boolean appearsSomewhere(Map<Node, List<ITorrent>> allTorrents, ITorrent search) {
		Set<Entry<Node, List<ITorrent>>> it = allTorrents.entrySet();
		for (Entry<Node, List<ITorrent>> entries : it) {
			for (ITorrent i : entries.getValue()) {
				if (search.getTorrentHash().equals(i.getTorrentHash())) {
					return true;
				}
			}
		}
		return false;
	}
	
}

package com.openseedbox.jobs;

import com.openseedbox.backend.ITorrent;
import java.util.ArrayList;
import java.util.List;
import models.Node;
import models.Torrent;
import notifiers.Mails;
import play.jobs.Every;
import play.jobs.Job;

@Every("10s")
public class NodePollerJob extends Job {
	
	@Override
	public void doJob() {		
		List<Node> nodes = Node.getActiveNodes();
		for (Node n : nodes) {	
			if (!n.isDown()) {
				List<ITorrent> torrents = n.getNodeBackend().listTorrents();
				List<Torrent> fromDb = Torrent.getByHash(getHashStrings(torrents));
				for (Torrent db : fromDb) {
					ITorrent match = findMatching(db, torrents);
					if (match != null) {
						db.merge(match);
					}
				}
				Torrent.batch().update(fromDb);	
			}
		}				
	}
	
	private List<String> getHashStrings(List<ITorrent> torrents) {
		List<String> ret = new ArrayList<String>();
		for (ITorrent t : torrents) {
			ret.add(t.getTorrentHash());
		}
		return ret;
	}
	
	private ITorrent findMatching(ITorrent source, List<ITorrent> list) {
		for (ITorrent t : list) {
			if (t.getTorrentHash().equals(source.getTorrentHash())) {
				return t;
			}
		}
		return null;
	}
	
}

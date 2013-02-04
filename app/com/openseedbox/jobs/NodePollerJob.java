package com.openseedbox.jobs;

import com.openseedbox.backend.ITorrent;
import com.openseedbox.models.Node;
import com.openseedbox.models.Torrent;
import java.util.ArrayList;
import java.util.List;
import play.jobs.Every;

@Every("10s")
@JobName("Node Poller")
public class NodePollerJob extends LoggedJob {
	
	@Override
	protected Object doGenericJob() {
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
		return null;
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

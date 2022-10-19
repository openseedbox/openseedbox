package com.openseedbox.jobs;

import com.openseedbox.backend.ITorrent;
import com.openseedbox.jobs.admin.LoggedAdminJob;
import com.openseedbox.models.Node;
import com.openseedbox.models.Torrent;
import java.util.ArrayList;
import java.util.List;
import play.jobs.Every;

@Every("10s")
@JobName("Node Poller Scheduler")
public class NodePollerJob extends LoggedAdminJob {
	
	@Override
	protected Object doGenericJob() {
		List<Node> nodes = Node.getActiveNodes();
		for (Node n : nodes) {
			new NodePollerWorker(n).now();
		}
		return null;
	}

	@JobName("Node Poller")
	public class NodePollerWorker extends LoggedAdminJob {
		Node n;

		public NodePollerWorker(Node node) {
			this.n = node;
		}

		@Override
		protected Object doGenericJob() throws Exception {
			List<ITorrent> torrents = n.getNodeBackend().listTorrents();
			List<Torrent> fromDb = Torrent.getByHash(getHashStrings(torrents));
			for (Torrent db : fromDb) {
				ITorrent match = findMatching(db, torrents);
				if (match != null) {
					db.merge(match);
				}
			}
			Torrent.batch().update(fromDb);
			return null;
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

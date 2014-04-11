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

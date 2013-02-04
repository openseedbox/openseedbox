package com.openseedbox.jobs.torrent;

import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.code.MessageException;
import com.openseedbox.jobs.GenericJob;
import com.openseedbox.models.User;
import com.openseedbox.models.UserTorrent;
import controllers.Client.TorrentAction;
import java.util.List;
import org.apache.commons.lang.StringUtils;

public class StartStopTorrentJob extends GenericJob {
	
	private TorrentAction action;
	private List<String> hashes;
	private User user;
	
	public StartStopTorrentJob(List<String> hashes, TorrentAction action, User user) {
		this.action = action;
		this.hashes = hashes;
		this.user = user;				
	}

	@Override
	protected Object doGenericJob() throws Exception {		
		for (String hash : hashes) {	
			if (StringUtils.isEmpty(hash)) {
				continue;
			}
			UserTorrent ut = UserTorrent.getByUser(user, hash);
			if (ut == null) {
				throw new MessageException("User has no such torrent with hash: " + hash);						
			}
			//note: the torrents may be spread out over different backend so we cant assume theyre all on the same one
			ITorrentBackend backend = ut.getTorrent().getNode().getNodeBackend();
			if (action == TorrentAction.START) {
				backend.startTorrent(hash);
			} else if (action == TorrentAction.STOP) {
				backend.stopTorrent(hash);
			}
		}
		return null;
	}
	
}

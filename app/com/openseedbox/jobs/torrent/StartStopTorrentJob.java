package com.openseedbox.jobs.torrent;

import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.jobs.GenericJob;
import com.openseedbox.models.User;
import com.openseedbox.models.UserTorrent;
import controllers.Client.TorrentAction;
import org.apache.commons.lang.StringUtils;

public class StartStopTorrentJob extends GenericJob {
	
	private TorrentAction action;
	private String hash;
	private User user;
	
	public StartStopTorrentJob(String hash, TorrentAction action, User user) {
		this.action = action;
		this.hash = hash;
		this.user = user;				
	}

	@Override
	protected Object doGenericJob() throws Exception {				
		if (StringUtils.isEmpty(hash)) {
			return null;
		}
		UserTorrent ut = UserTorrent.getByUser(user, hash);		
		ITorrentBackend backend = ut.getTorrent().getNode().getNodeBackend();
		boolean do_action;
		if (action == TorrentAction.STOP) {
			//only actually stop if all the users have stopped
			do_action = UserTorrent.isTorrentStoppedByAllUsers(hash);						
		} else {
			//only start if no other users have it started
			do_action = UserTorrent.isTorrentStartedByAUser(hash);
		}
		if (do_action) {		
			if (action == TorrentAction.START) {				
				backend.startTorrent(hash);
			} else if (action == TorrentAction.STOP) {
				backend.stopTorrent(hash);
			}		
		}
		return null;
	}
	
}

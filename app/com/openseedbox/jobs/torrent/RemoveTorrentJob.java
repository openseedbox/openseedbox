package com.openseedbox.jobs.torrent;

import com.openseedbox.Config;
import com.openseedbox.code.MessageException;
import com.openseedbox.jobs.LoggedJob;
import com.openseedbox.models.Torrent;
import com.openseedbox.models.TorrentEvent;
import com.openseedbox.models.TorrentEvent.TorrentEventType;
import com.openseedbox.models.User;
import com.openseedbox.models.UserTorrent;

public class RemoveTorrentJob extends LoggedJob<TorrentEvent> {
	
	private String hash;
	private TorrentEvent event;
	private User user;
	
	public RemoveTorrentJob(String hash, long userId) {
		event = new TorrentEvent(TorrentEventType.REMOVING, user);
		event.setTorrentHash(hash);
		this.user = User.findById(userId);
		this.hash = hash;
	}

	@Override
	protected TorrentEvent getJobEvent() {
		return event;
	}		

	@Override
	protected Object doGenericJob() throws Exception {
		if (Config.isTestMode()) {
			sleep(2); //add in some lag to simulate slow backend
		}		
		UserTorrent ut = UserTorrent.getByUser(user, hash);
		if (ut == null) {
			throw new MessageException("User has no such torrent with hash: " + hash);						
		}		
		ut.delete();
		//check if any other users have this torrent too
		if (UserTorrent.getUsersWithTorrentCount(hash) == 0) {
			Torrent to = Torrent.getByHash(hash);
			to.getNode().getNodeBackend().removeTorrent(hash);
			to.delete();
		}		
		event.setUserNotified(false);
		return null;
	}
	
}

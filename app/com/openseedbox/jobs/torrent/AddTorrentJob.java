package com.openseedbox.jobs.torrent;

import com.openseedbox.Config;
import com.openseedbox.backend.ITorrent;
import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.jobs.LoggedJob;
import com.openseedbox.models.Node;
import com.openseedbox.models.Torrent;
import com.openseedbox.models.TorrentEvent;
import com.openseedbox.models.TorrentEvent.TorrentEventType;
import com.openseedbox.models.User;
import com.openseedbox.models.UserTorrent;
import java.io.File;

public class AddTorrentJob extends LoggedJob<TorrentEvent> {
	
	private String urlOrMagnet;
	private File file;
	private TorrentEvent event;
	private User user;
	
	public AddTorrentJob(String urlOrMagnet, File file, User user) {
		this.urlOrMagnet = urlOrMagnet;
		this.file = file;
		this.user = user;
		this.event = new TorrentEvent(TorrentEventType.ADDING, user);
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
		//TODO: check that we dont already have the torrent somewhere in the system. Hard because we dont know the torrent hash at this point
		Node node = Node.getBestForNewTorrent();
		ITorrentBackend backend = node.getNodeBackend();			
		ITorrent added = (file != null) ? backend.addTorrent(file) : backend.addTorrent(urlOrMagnet);		
		
		Torrent t = new Torrent();
		t.setHashString(added.getTorrentHash());
		t.setStatus(added.getStatus());
		t.setName(added.getName());
		t.setNode(node);
		t.save();
		
		UserTorrent ut = new UserTorrent();
		ut.setUser(user);
		ut.setTorrentHash(added.getTorrentHash());
		ut.insert();		
		
		event.setTorrentHash(added.getTorrentHash());
		event.setUserNotified(false);		
		
		return null;
	}
	
}

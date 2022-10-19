package com.openseedbox.jobs.torrent;

import com.openseedbox.Config;
import com.openseedbox.backend.ITorrent;
import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.code.Util;
import com.openseedbox.jobs.LoggedJob;
import com.openseedbox.models.Node;
import com.openseedbox.models.Torrent;
import com.openseedbox.models.TorrentEvent;
import com.openseedbox.models.TorrentEvent.TorrentEventType;
import com.openseedbox.models.User;
import com.openseedbox.models.UserMessage;
import com.openseedbox.models.UserTorrent;
import java.io.File;
import org.apache.commons.io.FileUtils;

public class AddTorrentJob extends LoggedJob<TorrentEvent> {
	
	private String urlOrMagnet;
	private File file;
	private TorrentEvent event;
	private User user;
	private String groupName;
	
	public AddTorrentJob(String urlOrMagnet, File file, long userId, String groupName) {
		this.urlOrMagnet = urlOrMagnet;
		this.file = file;
		this.user = User.findById(userId);
		this.event = new TorrentEvent(TorrentEventType.ADDING, user);
		this.groupName = groupName;
	}

	@Override
	protected TorrentEvent getEvent() {
		return event;
	}		

	@Override
	protected Object doGenericJob() throws Exception {			
		if (Config.isTestMode()) {
			sleep(2); //add in some lag to simulate slow backend
		}
		//TODO: check that we dont already have the torrent somewhere in the system. Hard because we dont know the torrent hash at this point
		Node node = Node.getBestForNewTorrent(user);
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
		user.addTorrentGroup(groupName);
		ut.setGroupName(groupName);	
		ut.setRunning(true);
		ut.save();
		
		event.setTorrentHash(added.getTorrentHash());
		event.setUserNotified(false);	
		
		if (file != null) {
			FileUtils.deleteQuietly(file);
		}
		
		return null;
	}

	@Override
	protected void onException(Exception ex) {
		new UserMessage(user,"An error occured adding a torrent!", Util.getStackTrace(ex)).save();
	}		
	
}

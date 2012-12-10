package code.jobs;

import code.jobs.TorrentControlJob.TorrentControlJobResult;
import com.openseedbox.backend.transmission.Transmission;
import java.util.List;
import models.Account;
import models.Torrent;
import models.User;
import play.jobs.Job;

public class TorrentControlJob extends Job<TorrentControlJobResult> {
	
	private List<String> _hashes;
	private TorrentAction _action;
	private User _user;

	public TorrentControlJob(Account account, List<String> hashes, TorrentAction ta) {
		this._hashes = hashes;
		this._action = ta;
		this._user = account.getPrimaryUser();
	}
	
	@Override
	public TorrentControlJobResult doJobWithResult() throws Exception {
		Transmission t = null;//_user.getTransmission();
		TorrentControlJobResult res = new TorrentControlJobResult();
		try {
			if (_action == TorrentAction.START) {
				res.success = t.startTorrent(_hashes);
			} else if (_action == TorrentAction.STOP) {
				res.success = t.pauseTorrent(_hashes);
			} else if (_action == TorrentAction.REMOVE) {
				res.success = t.removeTorrent(_hashes, false);
				if (res.success && _hashes.size() > 0) {
					//delete all the db entries for the torrents
					//Torrent.all().filter("hashString IN", _hashes).filter("user", _user).delete();
				}
			}
		} catch (Exception ex) {
			res.error = ex;
		}
		return res;
	}
	
	public enum TorrentAction {
		START, STOP, REMOVE
	}
	
	public class TorrentControlJobResult extends JobResult {
		public boolean success;
	}
	
}

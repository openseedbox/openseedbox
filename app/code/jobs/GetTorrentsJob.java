package code.jobs;

import code.jobs.GetTorrentsJob.GetTorrentsJobResult;
import java.util.ArrayList;
import java.util.List;
import models.Account;
import models.Torrent;
import models.User;
import org.apache.commons.lang.StringUtils;
import play.jobs.Job;

public class GetTorrentsJob extends Job<GetTorrentsJobResult> {
	
	private User _user;
	private String _group, _hash;
	
	/**
	 * Gets all the torrents in the specified users transmission-daemon
	 * @param user The user to get torrents for
	 */
	public GetTorrentsJob(Account account) {
		this(account, null);
	}
	
	/**
	 * Gets all the torrents in the specified group in the specified users transmission-daemon
	 * @param user The user to get torrents for
	 * @param group The group to get torrents in
	 */
	public GetTorrentsJob(Account account, String group) {
		this(account, group, null);
	}	
	
	/**
	 * Gets the specified torrent in the specified users transmission-daemon
	 * @param user The user to get torrents for
	 * @param group Ignore this parameter, only exists to make the method signature different
	 * @param torrentHash The hash of a specific torrent to get details for
	 */
	public GetTorrentsJob(Account account, String group, String torrentHash) {
		_user = account.getPrimaryUser();
		_group = group;
		_hash = torrentHash;
	}		
	
	@Override
	public GetTorrentsJobResult doJobWithResult() throws Exception {
		GetTorrentsJobResult res = new GetTorrentsJobResult();
		try {
			if (!StringUtils.isEmpty(_hash)) {
				res.torrents = new ArrayList<Torrent>();
				res.torrents.add(_user.getTorrent(_hash, true));
			} else {
				if (!StringUtils.isEmpty(_group)) {
					res.torrents = _user.getTorrents(_group);
				} else {
					res.torrents = _user.getTorrents();
				}
			}
		} catch (Exception ex) {
			res.error = ex;
		}
		//Logger.info("End of job, torrents are %s", res.torrents);
		return res;
	}
	
	public class GetTorrentsJobResult extends JobResult {
		public List<Torrent> torrents;
	}
	
}

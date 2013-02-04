package com.openseedbox.jobs;

import com.openseedbox.models.JobEvent;
import play.jobs.Every;

@Every("30s")
@JobName("Cleanup Job")
public class CleanupJob extends LoggedJob<JobEvent> {

	@Override
	protected Object doGenericJob() throws Exception {
		//clean up TorrentEvents that are older than 10 mins
		//clean up torrents that are in the db but not the backend, and vice versa
		return null;
	}
	
}

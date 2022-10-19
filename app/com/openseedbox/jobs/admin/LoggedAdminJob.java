package com.openseedbox.jobs.admin;

import com.openseedbox.jobs.LoggedJob;
import com.openseedbox.models.JobEvent;

public abstract class LoggedAdminJob extends LoggedJob<JobEvent> {

	@Override
	protected JobEvent getEvent() {
		return new JobEvent(this);
	}

}

package com.openseedbox.jobs.admin;

import com.openseedbox.jobs.GenericJobResult;
import com.openseedbox.models.JobEvent;
import play.Logger;

/**
 * As the name suggests the run is logged in case of error only.
 */
public abstract class ErrorLoggedAdminJob extends LoggedAdminJob {

	@Override
	protected void logResult(GenericJobResult res, JobEvent event) {
		super.logResult(res, event);
		if (!res.hasError()) {
			Logger.trace("deleting actual %s record: %d", this, event.getId());
			try {
				event.delete();
			}
			catch (Exception e) {
	                        Logger.warn(e, "unable delete actual %s record: %s", this, event.getId());
			}
		}
	}
}

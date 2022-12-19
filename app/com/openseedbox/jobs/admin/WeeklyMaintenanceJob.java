package com.openseedbox.jobs.admin;

import com.openseedbox.Config;
import com.openseedbox.jobs.JobName;
import com.openseedbox.models.JobEvent;
import play.Logger;
import play.i18n.Messages;
import play.jobs.Every;

import java.util.Calendar;

@Every("7d")
@JobName("Weekly Maintenance Scheduler")
public class WeeklyMaintenanceJob extends ErrorLoggedAdminJob {

    @Override
    protected Object doGenericJob() throws Exception {
        new JobEventJob().now();
        return null;
    }

    @JobName("JobEvent Maintenance")
    public class JobEventJob extends LoggedAdminJob {
        @Override
        protected Object doGenericJob() throws Exception {
            Calendar someWeekAgo = Calendar.getInstance();
            someWeekAgo.add(Calendar.DAY_OF_YEAR, -7 * Config.getMaintenanceJobEventOlderThanWeeks());
            Logger.debug("delete before date %s", someWeekAgo.toInstant().toString());
            int deleted = JobEvent.deleteOlderThan(someWeekAgo.getTime());
            Logger.debug("deleted %d rows", deleted);
            return Messages.get("maintenance.job.rows.deleted", deleted, JobEvent.class.getSimpleName(),someWeekAgo.toInstant());
        }
    }
}

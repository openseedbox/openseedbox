package com.openseedbox.jobs.admin;

import com.openseedbox.Config;
import com.openseedbox.jobs.JobName;
import com.openseedbox.models.TorrentEvent;
import play.Logger;
import play.i18n.Messages;
import play.jobs.Every;

import java.util.Calendar;

@Every("30d")
@JobName("Monthly Maintenance Scheduler")
public class MonthlyMaintenanceJob extends ErrorLoggedAdminJob {

    @Override
    protected Object doGenericJob() throws Exception {
        new TorrentEventJob().now();
        return null;
    }

    @JobName("TorrentEvent Maintenance")
    public class TorrentEventJob extends LoggedAdminJob {
        @Override
        protected Object doGenericJob() throws Exception {
            Calendar fewMonthsAgo = Calendar.getInstance();
            fewMonthsAgo.add(Calendar.DAY_OF_YEAR, -30 * Config.getMaintenanceTorrentEventOlderThanMonths());
            Logger.debug("delete before date %s", fewMonthsAgo.toInstant().toString());
            int deleted = TorrentEvent.deleteOlderThan(fewMonthsAgo.getTime());
            Logger.debug("deleted %d rows", deleted);
            return Messages.get("maintenance.job.rows.deleted", deleted, TorrentEvent.class.getSimpleName(),fewMonthsAgo.toInstant());
        }
    }
}

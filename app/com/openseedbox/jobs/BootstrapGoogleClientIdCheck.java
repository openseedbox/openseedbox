package com.openseedbox.jobs;

import com.openseedbox.Config;
import play.exceptions.ConfigurationException;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class BootstrapGoogleClientIdCheck extends Job {

    @Override
    public void doJob() throws Exception {
        String clientId = Config.getGoogleClientId();
        if (clientId == null || clientId.isEmpty() || clientId.contains("${GOOGLE_CLIENTID}")) {
            String msg = "You need to specify your Google ClientID " +
                    "(in the GOOGLE_CLIENTID environment variable or in the \"google.clientid\" property of conf/application.conf file) " +
                    "or you wont be able to log in!";
            throw new ConfigurationException(msg);
        }
    }
}

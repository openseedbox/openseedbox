package com.openseedbox.jobs;

import org.apache.commons.lang.RandomStringUtils;

import play.Logger;
import play.Play;
import play.exceptions.ConfigurationException;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.vfs.VirtualFile;

import java.util.Properties;

@OnApplicationStart
public class BootstrapNotGeneratedApplicationConf extends Job {

    private static final String NOT_GENERATED = "NOT_GENERATED";
    private static final String APPLICATION_SECRET = "application.secret";
    private static final String SESSION_COOKIE_NAME = "application.session.cookie";
    private static final String INCLUDE_RUNTIME_CONF = "@include.runtime";

    @Override
    public void doJob() throws Exception {
        Properties modifiedProps = new Properties();

        // application.secret=NOT_GENERATED
        if (Play.configuration.getProperty(APPLICATION_SECRET, "").equals(NOT_GENERATED)) {
            Logger.info("Generating the secret key...");
            modifiedProps.setProperty(APPLICATION_SECRET, RandomStringUtils.random(64, true, true));
        }

        // application.session.cookie=NOT_GENERATED
        if (Play.configuration.getProperty(SESSION_COOKIE_NAME, "").equals(NOT_GENERATED)) {
            Logger.info("Play session cookie name has not been generated; generating ... ");
            String cookieName = ("PLAY_" + Play.configuration.getProperty("application.name","") + "_" +
                    RandomStringUtils.random(16, true, false)).toUpperCase();
            modifiedProps.setProperty(SESSION_COOKIE_NAME, cookieName);
            Logger.info("Session cookie's name is now: %s", cookieName);
        }

        if (modifiedProps.size() > 0) {
            String fileName = Play.configuration.getProperty("@include.runtime" , "");
            if (fileName.isEmpty()) {
                throw new ConfigurationException(String.format("NOT_GENERATED can only be used only with included configurations (see \"%s\" property)", INCLUDE_RUNTIME_CONF));
            }
            VirtualFile runtimeFile = VirtualFile.open(Play.applicationPath).child("conf/" + fileName);
            if (runtimeFile.isDirectory()) {
                throw new ConfigurationException(String.format("Included conf file %s is a directory (check \"%s\" property)", fileName, INCLUDE_RUNTIME_CONF));
            }

            Properties runtimeProps = new Properties();
            if (runtimeFile.exists()) {
                Logger.trace("Loading runtime configuration from existing %s", runtimeFile.getName());
                runtimeProps.load(runtimeFile.inputstream());
            }
            runtimeProps.putAll(modifiedProps);
            runtimeProps.store(runtimeFile.outputstream(), "");
            Logger.debug("Saved generated properties to %s", runtimeFile.getName());

            Play.configuration.putAll(modifiedProps);
        }
    }
}

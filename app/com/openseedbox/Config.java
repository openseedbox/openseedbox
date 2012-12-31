package com.openseedbox;

import play.Play;

/**
 * Wrapper for getting things from application.conf
 * @author Erin Drummond
 */
public class Config {
	
	public static String getNodeAccessType() {
		return Play.configuration.getProperty("openseedbox.node.access", "https").trim();
	}
	
	public static String getErrorEmailAddress() {
		return Play.configuration.getProperty("errors.mailto", "erin.dru@gmail.com");
	}
	
	public static String getErrorFromEmailAddress() {
		return Play.configuration.getProperty("errors.mailfrom", "errors@openseedbox.com");
	}	
	
	public static String getBitPayAPIKey() {
		return Play.configuration.getProperty("bitpay.apikey");
	}
	
}

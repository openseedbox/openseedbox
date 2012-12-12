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
	
}

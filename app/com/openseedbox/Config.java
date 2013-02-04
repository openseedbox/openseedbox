package com.openseedbox;

import play.Play;

/**
 * Wrapper for getting things from application.conf
 * @author Erin Drummond
 */
public class Config {
	
	public static boolean isTestMode() {
		return Play.id.equals("test");
	}
	
	public static String getAssetPrefix() {
		return Play.configuration.getProperty("openseedbox.assets.prefix", "/public");
	}
	
	public static String getNodeAccessType() {
		return Play.configuration.getProperty("openseedbox.node.access", "https").trim();
	}
	
	public static String getErrorEmailAddress() {
		return Play.configuration.getProperty("openseedbox.errors.mailto", "erin.dru@gmail.com");
	}
	
	public static String getErrorFromEmailAddress() {
		return Play.configuration.getProperty("openseedbox.errors.mailfrom", "errors@openseedbox.com");
	}	
	
	public static Boolean isZipEnabled() {
		String z = Play.configuration.getProperty("openseedbox.zip", "false");
		return Boolean.valueOf(z);
	}
	
	public static Boolean isZipManifestOnly() {
		String z = Play.configuration.getProperty("openseedbox.zip.manifestonly", "false");
		return Boolean.valueOf(z);
	}
	
	public static String getZipPath() {
		return Play.configuration.getProperty("openseedbox.zip.path", "/rdr");
	}
	
}

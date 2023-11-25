package com.openseedbox;

import play.Play;

import static com.openseedbox.code.libs.KeyCloakOIDC.DEFAULT_RESPONSE_MODE;

/**
 * Wrapper for getting things from application.conf
 * @author Erin Drummond
 */
public class Config {

	public static boolean isTestMode() {
		return Play.id.equals("test");
	}

        public static String getGoogleClientId() {
                return Play.configuration.getProperty("google.clientid");
        }

	public static String getGoogleClientSecret() {
		return Play.configuration.getProperty("openseedbox.auth.google.clientsecret");
	}

	public static String getGoogleOpenIdConfigurationUrl() {
		return Play.configuration.getProperty("openseedbox.auth.google.configuration.url", "https://accounts.google.com/.well-known/openid-configuration");
	}

	public static String getKeyCloakClientId() {
		return Play.configuration.getProperty("openseedbox.auth.keycloak.clientid");
	}

	public static String getKeyCloakClientSecret() {
		return Play.configuration.getProperty("openseedbox.auth.keycloak.clientsecret");
	}

	public static String getKeyCloakOpenIdConfigurationUrl() {
		return Play.configuration.getProperty("openseedbox.auth.keycloak.openid.configuration.url");
	}

	public static String getKeyCloakResponseMode() {
		return Play.configuration.getProperty("openseedbox.auth.keycloak.response.mode", DEFAULT_RESPONSE_MODE);
	}

	public static String getGitHubClientId() {
		return Play.configuration.getProperty("openseedbox.auth.github.clientid");
	}

	public static String getGitHubClientSecret() {
		return Play.configuration.getProperty("openseedbox.auth.github.clientsecret");
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
		String z = Play.configuration.getProperty("openseedbox.zip", "true");
		return Boolean.valueOf(z);
	}

	public static Boolean isMultiZipEnabled() {
		String z = Play.configuration.getProperty("openseedbox.multizip", "true");
		return Boolean.valueOf(z);
	}

	public static Boolean isZipManifestOnly() {
		String z = Play.configuration.getProperty("openseedbox.zip.manifestonly", "false");
		return Boolean.valueOf(z);
	}

	public static String getZipPath() {
		return Play.configuration.getProperty("openseedbox.zip.path", "/rdr");
	}

	public static Integer getMaintenanceJobEventOlderThanWeeks() {
		return Integer.max(
			Integer.parseInt(Play.configuration.getProperty("openseedbox.maintenance.jobevent.older.than.weeks", "1"))
			, 1
		);
	}

	public static Integer getMaintenanceTorrentEventOlderThanMonths() {
		return Integer.max(
			Integer.parseInt(Play.configuration.getProperty("openseedbox.maintenance.torrentevent.older.than.months", "1"))
			, 1
		);
	}

	public static String getClientIndexViewsPosition() {
		return Play.configuration.getProperty("openseedbox.client.index.view.position", "hide");
	}

}

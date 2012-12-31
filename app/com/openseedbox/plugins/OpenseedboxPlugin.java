package com.openseedbox.plugins;

import java.util.List;
import com.openseedbox.models.User;

public abstract class OpenseedboxPlugin {
	
	protected User user;
	
	public OpenseedboxPlugin(User u) {
		this.user = u;
	}
	
	/**
	 * Gets the plugin name, used for identifying the plugin in the interface
	 * @return The plugin name
	 */
	public String getPluginName() {
		throw new RuntimeException("Plugin should override this!");
	}
	
	/**
	 * Should return 'true' if the plugin provides a search interface
	 * Typically this is searching a 3rd party torrent site
	 * If this method is overridden to return 'true', doSearch() should
	 * be implemented
	 * @return True if this plugin has a search interface, false if it doesnt
	 */
	public boolean isSearchPlugin() {
		return false;
	}
	
	/**
	 * Should be implemented if isSearchPlugin() is overridden to return true
	 * @param terms The users search query
	 * @return A list of PluginSearchResult items that result from searching a 3rd
	 * party website
	 */
	public List<PluginSearchResult> doSearch(String terms) {
		return null;
	}
	
	/**
	 * Should return 'true' if this plugin has user-modifiable settings.
	 * This method is used to check if the plugin has user-configurable settings
	 * so they can be written out on the User's plugin settings page
	 * @return True if this plugin has configurable settings, false otherwise
	 */
	public boolean hasSettings() {
		return false;
	}
	
	/**
	 * If hasSettings() is overridden to return true, this method should
	 * return a list of PluginSetting objects representing the user-configurable
	 * settings of this plugin
	 * @return A list of PluginSetting objects, or null if there are no settings
	 */
	public List<PluginSetting> getSettings() {
		return null;
	}
	
	public class PluginSetting {
		private String name;
		private String value;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
	
	public class PluginSearchResult {
		private String torrentName;
		private String torrentUrl;
		private String currentSeeders;
		private String currentPeers;
		private String fileSize;

		public String getTorrentName() {
			return torrentName;
		}

		public void setTorrentName(String torrentName) {
			this.torrentName = torrentName;
		}

		public String getTorrentUrl() {
			return torrentUrl;
		}

		public void setTorrentUrl(String torrentUrl) {
			this.torrentUrl = torrentUrl;
		}

		public String getCurrentSeeders() {
			return currentSeeders;
		}

		public void setCurrentSeeders(String currentSeeders) {
			this.currentSeeders = currentSeeders;
		}

		public String getCurrentPeers() {
			return currentPeers;
		}

		public void setCurrentPeers(String currentPeers) {
			this.currentPeers = currentPeers;
		}

		public String getFileSize() {
			return fileSize;
		}

		public void setFileSize(String fileSize) {
			this.fileSize = fileSize;
		}
		
	}
	
}

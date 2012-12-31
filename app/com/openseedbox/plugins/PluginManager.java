package com.openseedbox.plugins;

import java.util.ArrayList;
import java.util.List;
import play.Logger;
import play.Play;

public class PluginManager {
	
	public static List<OpenseedboxPlugin> getSearchPlugins() {
		List<OpenseedboxPlugin> ret = new ArrayList<OpenseedboxPlugin>();
		List<Class> plugins = Play.classloader.getAssignableClasses(OpenseedboxPlugin.class);
		for (Class p : plugins) {
			try {
				OpenseedboxPlugin pl = (OpenseedboxPlugin) p.newInstance();
				if (pl.isSearchPlugin()) {
					ret.add(pl);
				}
			} catch (InstantiationException ex) {
				Logger.error(ex, "Unable to instantiate plugin %s", p.getName());
			} catch (IllegalAccessException ex) {
				Logger.error(ex, "Unable to instantiate plugin %s", p.getName());
			}
		}
		return ret;
	}
	
}

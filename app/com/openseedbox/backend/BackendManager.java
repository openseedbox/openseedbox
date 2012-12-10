package com.openseedbox.backend;

import com.openseedbox.backend.transmission.Transmission;
import com.openseedbox.mvc.ISelectListItem;
import java.util.ArrayList;
import java.util.List;
import models.Account;
import play.Play;

/**
 * A manager for enumerating backends
 * @author Erin Drummond
 */
public class BackendManager {
	
	/**
	 * @return A list of supported backends for showing to a user
	 */
	public static List<SupportedBackend> getSupportedBackends() {
		List<Class> backends = Play.classloader.getAnnotatedClasses(TorrentBackend.class);
		List<SupportedBackend> ret = new ArrayList<SupportedBackend>();
		for (Class backend : backends) {
			TorrentBackend tb = (TorrentBackend) backend.getAnnotation(TorrentBackend.class);
			ret.add(new SupportedBackend(tb.name(), backend.getName()));
		}
		return ret;
	}
	
	public static ITorrentBackend getForAccount(Account a) {
		return new Transmission(null);
	}
	
	public static class SupportedBackend implements ISelectListItem {
		
		private String _name, _className;
		
		public SupportedBackend(String name, String className) {
			_name = name;
			_className = className;
		}
		
		public String getName() {
			return _name;
		}
		
		public String getClassName() {
			return _className;
		}

		public String getValue() {
			return _className;
		}

		public boolean isSelected() {
			return false;
		}
		
	}
}

package com.openseedbox.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openseedbox.models.ModuleSetting;
import play.PlayPlugin;
import play.mvc.Router.Route;
import play.templates.JavaExtensions;

public abstract class OpenSeedboxModuleBase extends PlayPlugin implements IOpenSeedboxModule {
	
	public Map<String, String> getAllSettings() {
		List<ModuleSetting> settings = ModuleSetting.getAllSettings(getSettingNamespace());
		Map<String, String> ret = new HashMap<String, String>();
		for (ModuleSetting ms : settings) {
			ret.put(ms.getSettingKey(), ms.getSettingValue());
		}
		return ret;
	}
	
	public String getSetting(String settingName) {
		return getSetting(settingName, null);
	}
	
	public String getSetting(String settingName, String defaultValue) {
		ModuleSetting ms = getModuleSetting(settingName);
		return (ms == null) ? defaultValue : ms.getSettingValue();
	}
	
	public void storeSetting(String settingName, String settingValue) {
		ModuleSetting.storeSetting(getSettingNamespace(), settingName, settingValue);
	}

	@Override
	public void onRequestRouting(Route route) {
		if (route.path.startsWith(String.format("/module/%s", getModuleSlug()))) {

		}
	}

	@Override
	public void onApplicationStart() {
		//register routes
	}
	
	protected String getModuleSlug() {
		return JavaExtensions.slugify(this.getClass().getName(), true);
	}
	
	private String getSettingNamespace() {
		return this.getClass().getPackage().getName();
	}
	
	private ModuleSetting getModuleSetting(String settingName) {
		return ModuleSetting.getSetting(getSettingNamespace(), settingName);
	}
	
}

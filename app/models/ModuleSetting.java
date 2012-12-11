package models;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import play.db.jpa.Model;

@Table(name="module_setting")
public class ModuleSetting extends Model {
	
	@Column(name="namespace")
	private String settingNamespace;
	
	@Column(name="setting_key")
	private String settingKey;
	
	@Column(name="setting_value")
	private String settingValue;
	
	public static ModuleSetting getSetting(String namespace, String key) {
		return ModuleSetting.find("settingNamespace = ? AND settingKey = ?", namespace, key).first();
	}
	
	public static ModuleSetting storeSetting(String namespace, String key, String value) {
		//check that the setting isnt already there
		ModuleSetting ms = getSetting(namespace, key);
		if (ms == null) {
			ms = new ModuleSetting();
		}
			
		ms.setSettingNamespace(namespace);
		ms.setSettingKey(key);
		ms.setSettingValue(value);
		ms.save();

		return ms;
	}
	
	public static List<ModuleSetting> getAllSettings(String namespace) {
		return ModuleSetting.find("settingNamespace = ?", namespace).fetch();
	}
	
	//Getters and Setters
	public String getSettingNamespace() {
		return settingNamespace;
	}

	public void setSettingNamespace(String settingNamespace) {
		this.settingNamespace = settingNamespace;
	}

	public String getSettingKey() {
		return settingKey;
	}

	public void setSettingKey(String settingKey) {
		this.settingKey = settingKey;
	}

	public String getSettingValue() {
		return settingValue;
	}

	public void setSettingValue(String settingValue) {
		this.settingValue = settingValue;
	}	
	
}

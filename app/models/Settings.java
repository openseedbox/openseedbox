package models;

import com.openseedbox.code.Util;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import play.db.jpa.Model;

@Table(name="setting")
public class Settings extends Model {
	
	public static final transient String SETTING_BACKEND_CONFIG = "openseedbox.backend.config";
	
	@Column(name="setting_key") //Note: 'key' is a reserved word in MySQL so table creation breaks
	private String key;
	
	@Lob
	private String value;
	
	public static String getValue(String key) {
		Settings s = get(key);
		return (s != null) ? s.getValue() : null;
	}
	
	public static Settings get(String key) {
		Settings s = Settings.find("key = ?", key).first();
		return (s != null) ? s : null;
	}
	
	public static void store(String key, String value) {
		Settings s = get(key);
		if (s == null) {
			s = new Settings();
			s.setKey(key);
		}
		s.setValue(value);
		s.save();
	}

	/* Getters and Setters */
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}

package models;

import com.openseedbox.backend.BackendManager;
import com.openseedbox.backend.ITorrentBackend;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import play.db.jpa.Model;

@Entity
@Table(name="account")
public class Account extends Model {
	
	@OneToOne
	@JoinColumn(name="primary_user_id")
	protected User primaryUser;
	
	@OneToOne
	@JoinColumn(name="plan_id")
	protected Plan plan;
	
	public Plan getPlan() {
		return plan;
	}
	
	public User getPrimaryUser() {
		return primaryUser;
	}
	
	public void setPrimaryUser(User u) {
		this.primaryUser = u;
	}
	
	public String getDisplayName() {
		return getPrimaryUser().getDisplayName();
	}
	
	public ITorrentBackend getTorrentBackend() {
		return BackendManager.getForAccount(this);
	}	
}

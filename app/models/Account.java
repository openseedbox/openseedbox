package models;

import code.MessageException;
import code.transmission.Transmission;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.modules.siena.EnhancedModel;
import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Table;

@Table("account")
public class Account extends EnhancedModel {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Column("primary_user_id")
	protected User primaryUser;
	
	@Column("plan_id")
	protected Plan plan;
	
	@Column("node_id")
	public Node node;
	
	@Column("transmission_port")
	public int transmissionPort;
	
	public Plan getPlan() {
		if (plan == null) { return null; }
		return Plan.getByKey(plan.id);
	}
	
	private transient User _primaryUser;
	public User getPrimaryUser() {
		if (_primaryUser == null) {
			_primaryUser = User.getByKey(primaryUser.id);
		}
		return _primaryUser;
	}
	
	public void setPrimaryUser(User u) {
		this._primaryUser = null;
		this.primaryUser = u;
	}
	
	private transient String _displayName;
	public String getDisplayName() {
		if (_displayName == null) {
			_displayName = getPrimaryUser().getDisplayName();
		}
		return _displayName;
	}
	
	private transient Transmission _transmission;
	public Transmission getTransmission() {
		if (_transmission == null) {
			_transmission = new Transmission(this);
		}
		return _transmission;
	}
	
	public int getTransmissionPort() throws MessageException {
		if (this.transmissionPort == 0) {
			throw new MessageException("Transmission port not set!");
		}
		return this.transmissionPort;
	}
	
	private transient String _transmissionPassword;
	public String getTransmissionPassword() throws MessageException {
		Node n = this.getNode();
		if (StringUtils.isEmpty(this._transmissionPassword)) {
			String pw = n.name + this.getPrimaryUser().emailAddress;
			this._transmissionPassword = DigestUtils.md5Hex(pw);
		}
		return this._transmissionPassword;
	}
	
	public Node getNode() {
		if (this.node != null) {
			return Node.getByKey(this.node.id);
		}
		return null;
	}
	
	public static int getAvailableTransmissionPort(Node n) throws MessageException {
		Account biggestPort =
				Account.all()
				.filter("node", n)
				.filter("transmissionPort >", 0)
				.order("-transmissionPort").limit(1).get();
		int p;
		if (biggestPort != null) {
			p = biggestPort.getTransmissionPort();
			if (p > 0) {
				p++;
			} else {
				p = 3000; //start at 3000
			}
		} else {
			p = 3000;
		}
		return p;		
	}	
	
}

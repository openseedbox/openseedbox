package models;

import java.util.Date;
import play.modules.siena.EnhancedModel;
import siena.Column;
import siena.DateTime;
import siena.Generator;
import siena.Id;
import siena.Table;

@Table("user_message")
public class UserMessage extends EnhancedModel {
	
	public static enum State {
		MESSAGE, ERROR
	}
	
	public static enum Type {
		GENERAL, SWITCHPLAN, LIMITSEXCEEDED
	}
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	public State state;
	
	public Type type;
	
	public String heading;
	
	public String message;
	
	@Column("user_id")
	public User user;
	
	@DateTime
	@Column("create_date_utc")
	public Date createDateUtc;
	
	@DateTime
	@Column("dismiss_date_utc")
	public Date dismissDateUtc;
	
}

package models.checkout;

import play.modules.siena.EnhancedModel;
import siena.*;

@Table("google_notification")
public class GoogleNotification extends EnhancedModel {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Column("serial_number")
	@Unique("serial_number_unique")
	public String serialNumber;
	
}

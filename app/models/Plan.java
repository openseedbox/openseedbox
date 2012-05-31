package models;

import java.math.BigDecimal;
import java.util.List;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.modules.siena.EnhancedModel;
import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Table;
import siena.Unique;
import validation.IsDecimalNumber;
import validation.IsWholeNumber;

@Table("plan")
public class Plan extends EnhancedModel {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Required
	@Unique("name_unique")
	@Column("name")
	public String name;
	
	@Required
	@CheckWith(IsWholeNumber.class)
	@Column("max_diskspace_gb")
	public int maxDiskspaceGb;
	
	@Required
	@CheckWith(IsWholeNumber.class)
	@Column("max_active_torrents")
	public int maxActiveTorrents;
	
	@Required
	@CheckWith(IsDecimalNumber.class)
	@Column("monthly_cost")
	public BigDecimal monthlyCost;
	
	@Column("visible")
	public boolean visible;
	
	public int getUsedSlots() {
		return Account.all().filter("plan", this).count();
	}
	
	public int getFreeSlots() {
		List<FreeSlot> slots = FreeSlot.all().filter("plan", this).fetch();
		int ret = 0;
		for (FreeSlot fs : slots) {
			ret += fs.freeSlots;
		}
		return ret;
 	}
	
}

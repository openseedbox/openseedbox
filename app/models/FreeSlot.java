package models;

import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.modules.siena.EnhancedModel;
import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Table;
import siena.Unique;
import validation.IsWholeNumber;

@Table("free_slot")
public class FreeSlot extends EnhancedModel {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Unique("node_plan_unique")
	@Required
	@Column("node_id")
	public Node node;
	
	@Unique("node_plan_unique")
	@Required
	@Column("plan_id")
	public Plan plan;
	
	@Column("free_slots")
	@Required
	@CheckWith(IsWholeNumber.class)
	public int freeSlots;
	
	public Node getNode() {
		return Node.getByKey(node.id);
	}
	
	public Plan getPlan() {
		return Plan.getByKey(plan.id);
	}
	
	public int getUsedSlots() {
		//used slots is the amount of accounts on this node that are using this plan
		return Account.all().filter("plan", this.plan).filter("node", this.node).count();
	}
	
}

package models;

import com.openseedbox.code.BigDecimalUtils;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.db.jpa.Model;
import validation.IsDecimalNumber;
import validation.IsWholeNumber;

@Entity
@Table(name="plan")
public class Plan extends Model {
	
	@Required
	@Column(name="name")
	private String name;
	
	@Required
	@CheckWith(IsWholeNumber.class)
	@Column(name="max_diskspace_gb")
	private int maxDiskspaceGb;
	
	@Required
	@CheckWith(IsWholeNumber.class)
	@Column(name="max_active_torrents")
	private int maxActiveTorrents;
	
	@Required
	@CheckWith(IsDecimalNumber.class)
	@Column(name="monthly_cost")
	private BigDecimal monthlyCost;
	
	@Column(name="visible")
	private boolean visible;
	
	@Column(name="totalSlots")
	private int totalSlots;
	
	public boolean isFree() {
		return (BigDecimalUtils.LessThanOrEqual(monthlyCost, BigDecimal.ZERO));
	}
	
	public int getUsedSlots() {
		return (int) Account.count("plan = ?", this);
	}
	
	public int getFreeSlots() {
		int used = getUsedSlots();
		return totalSlots - used;
	}
	
	public int getTotalSlots() {
		return totalSlots;
 	}
	
	public String getInvoiceLineName() {
		return String.format("Seedbox Plan: %s", this.name);
	}
	
	public String getInvoiceLineDescription() {
		return "One months service";
	}
	
	public static List<Plan> getVisiblePlans() {
		return Plan.find("visible = ?", true).fetch();
	}
	
	/* Getters and Setters */

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMaxDiskspaceGb() {
		return maxDiskspaceGb;
	}

	public void setMaxDiskspaceGb(int maxDiskspaceGb) {
		this.maxDiskspaceGb = maxDiskspaceGb;
	}

	public int getMaxActiveTorrents() {
		return maxActiveTorrents;
	}

	public void setMaxActiveTorrents(int maxActiveTorrents) {
		this.maxActiveTorrents = maxActiveTorrents;
	}

	public BigDecimal getMonthlyCost() {
		return monthlyCost;
	}

	public void setMonthlyCost(BigDecimal monthlyCost) {
		this.monthlyCost = monthlyCost;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
}

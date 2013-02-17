package com.openseedbox.models;

import com.openseedbox.code.BigDecimalUtils;
import com.openseedbox.mvc.validation.IsDecimalNumber;
import com.openseedbox.mvc.validation.IsWholeNumber;
import java.math.BigDecimal;
import java.util.List;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import siena.Column;
import siena.Table;

@Table("plan")
public class Plan extends ModelBase {
	
	@Required @Column("name") private String name;	
	@Required @CheckWith(IsWholeNumber.class)
	@Column("max_diskspace_gb") private int maxDiskspaceGb;	
	@Required @CheckWith(IsWholeNumber.class)
	@Column("max_active_torrents") private int maxActiveTorrents;	
	@Required @CheckWith(IsDecimalNumber.class)
	@Column("monthly_cost") private BigDecimal monthlyCost;	
	@Column("visible") private boolean visible;	
	@Column("totalSlots") private int totalSlots;
	
	public boolean isFree() {
		return (BigDecimalUtils.LessThanOrEqual(monthlyCost, BigDecimal.ZERO));
	}
	
	public int getUsedSlots() {
		return User.all().filter("plan", this).count();
	}
	
	public int getFreeSlots() {
		int used = getUsedSlots();
		return totalSlots - used;
	}
	
	public String getInvoiceLineName() {
		return String.format("Seedbox Plan: %s", this.name);
	}
	
	public String getInvoiceLineDescription() {
		return "One months service";
	}
	
	public static List<Plan> getVisiblePlans() {
		return Plan.all().filter("visible", true).fetch();
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
	
	public long getMaxDiskspaceBytes() {
		long ret = getMaxDiskspaceGb();
		return ret * (1024L * 1024L * 1024L);
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

	public int getTotalSlots() {
		return totalSlots;
	}

	public void setTotalSlots(int totalSlots) {
		this.totalSlots = totalSlots;
	}	
}

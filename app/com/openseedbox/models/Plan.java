package com.openseedbox.models;

import com.openseedbox.code.BigDecimalUtils;
import com.openseedbox.gson.SerializedAccessorName;
import com.openseedbox.gson.UseAccessor;
import com.openseedbox.mvc.validation.IsDecimalNumber;
import com.openseedbox.mvc.validation.IsWholeNumber;
import java.math.BigDecimal;
import java.util.List;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import siena.Column;
import siena.Table;

@Table("plan")
@UseAccessor
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
	
	@SerializedAccessorName("is-free")
	public boolean isFree() {
		return (BigDecimalUtils.LessThanOrEqual(monthlyCost, BigDecimal.ZERO));
	}
	
	@SerializedAccessorName("used-slots")
	public int getUsedSlots() {
		return User.all().filter("plan", this).count();
	}
	
	@SerializedAccessorName("free-slots")
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

	@SerializedAccessorName("name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@SerializedAccessorName("max-diskspace-gb")
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

	@SerializedAccessorName("max-active-torrents")
	public int getMaxActiveTorrents() {
		return maxActiveTorrents;
	}

	public void setMaxActiveTorrents(int maxActiveTorrents) {
		this.maxActiveTorrents = maxActiveTorrents;
	}

	@SerializedAccessorName("monthly-cost")	
	public BigDecimal getMonthlyCost() {
		return monthlyCost;
	}

	public void setMonthlyCost(BigDecimal monthlyCost) {
		this.monthlyCost = monthlyCost;
	}

	@SerializedAccessorName("is-visible")	
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@SerializedAccessorName("total-slots-available")	
	public int getTotalSlots() {
		return totalSlots;
	}

	public void setTotalSlots(int totalSlots) {
		this.totalSlots = totalSlots;
	}	
}

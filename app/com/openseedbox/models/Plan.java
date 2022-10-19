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

import javax.persistence.Column;
import javax.persistence.Entity;

@UseAccessor
@Entity
public class Plan extends ModelBase {
	
	@Required private String name;
	@Required @CheckWith(IsWholeNumber.class)
	private int maxDiskspaceGb;
	@Required @CheckWith(IsWholeNumber.class)
	private int maxActiveTorrents;
	@Required @CheckWith(IsDecimalNumber.class)
	private BigDecimal monthlyCost;
	private boolean visible;
	@Column(name = "totalslots") private int totalSlots;
	
	@SerializedAccessorName("is-free")
	public boolean isFree() {
		return (BigDecimalUtils.LessThanOrEqual(monthlyCost, BigDecimal.ZERO));
	}
	
	@SerializedAccessorName("used-slots")
	public int getUsedSlots() {
		return User.<User>all().where().eq("plan", this).findRowCount();
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
		return Plan.<Plan>all().where().eq("visible", true).findList();
	}

	@Deprecated
	public static List<Plan> getVisiblePlansOrdered(String col) {
		return Plan.<Plan>all().where().eq("visible", true).orderBy(col).findList();
	}

	@Deprecated
	public static List<Plan> getAllPlansOrdered(String col) {
		return Plan.<Plan>all().orderBy(col).findList();
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

package com.openseedbox.models;

import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
public class Invoice extends ModelBase {

	//@Index
	@Required
	@NotNull
	@ManyToOne
	private User user;

	@Required
	@NotNull
	private Date invoiceDate;
	
	private Date paymentDate;
	
	private String bitpayUrl;
	
	private String bitpayId;
	
	public static Invoice createInvoice(User u, Plan p) {
		Invoice i = new Invoice();		
		i.setUser(u);
		i.setInvoiceDate(new Date());
		i.save();
		
		InvoiceLine line = new InvoiceLine();
		line.setName(p.getInvoiceLineName());
		line.setDescription(p.getInvoiceLineDescription());
		line.setQuantity(1);
		line.setPrice(p.getMonthlyCost());
		line.setParentInvoice(i);
		line.save();
		
		return i;
	}
	
	public static List<Invoice> getUnpaidForUser(User u) {
		return Invoice.<Invoice>all()
				.where()
				.eq("user", u)
				.eq("paymentDate", null)
				.findList();
	}
	
	public static List<Invoice> getPaidForUser(User u) {
		return Invoice.<Invoice>all()
				.where()
				.eq("user", u)
				.isNotNull("paymentDate")
				.orderBy("paymentDate desc")
				.findList();
	}
	
	public boolean hasBeenPaid() {
		return paymentDate != null;
	}
	
	public List<InvoiceLine> getInvoiceLines() {
		return InvoiceLine.<InvoiceLine>all()
				.where()
				.eq("parentInvoice", this)
				.findList();
	}
	
	public BigDecimal getTotalAmount() {
		BigDecimal total = BigDecimal.ZERO;
		for (InvoiceLine line : getInvoiceLines()) {
			total = total.add(line.getPrice());
		}
		return total;
	}
	
	public String getPaymentUrl() {
		if (this.bitpayUrl == null) {
			//BitPayResponse bpr = BitPay.createInvoice(this);
			//this.bitpayId = bpr.getId();
			//this.bitpayUrl = bpr.getUrl();
			this.save();
		}
		return this.bitpayUrl;
	}
	
	/* Getters and Setters */

	public User getUser() {
		return User.findById(user.id);
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public Date getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}
	
}

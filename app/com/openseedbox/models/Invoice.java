package com.openseedbox.models;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import siena.Column;
import siena.DateTime;
import siena.Index;
import siena.Table;

@Table("invoice")
public class Invoice extends ModelBase {
	
	@Column("user_id")
	@Index("invoice_user_IDX")
	private User user;
		
	@DateTime
	@Column("invoice_date")
	private Date invoiceDate;
	
	@DateTime
	@Column("payment_date")
	private Date paymentDate;
	
	@Column("bitpay_url")
	private String bitpayUrl;
	
	@Column("bitpay_id")
	private String bitpayId;
	
	public static Invoice createInvoice(User u, Plan p) {
		Invoice i = new Invoice();		
		i.setUser(u);
		i.setInvoiceDate(new Date());
		i.insert();
		
		InvoiceLine line = new InvoiceLine();
		line.setName(p.getInvoiceLineName());
		line.setDescription(p.getInvoiceLineDescription());
		line.setQuantity(1);
		line.setPrice(p.getMonthlyCost());
		line.setParentInvoice(i);
		line.insert();
		
		return i;
	}
	
	public static List<Invoice> getUnpaidForUser(User u) {
		return Invoice.all()
				.filter("user", u)
				.filter("paymentDate", null).fetch();		
	}
	
	public static List<Invoice> getPaidForUser(User u) {
		return Invoice.all()
				.filter("user", u)
				.filter("paymentDate !=", null)
				.order("-paymentDate").fetch();
	}
	
	public boolean hasBeenPaid() {
		return paymentDate != null;
	}
	
	public List<InvoiceLine> getInvoiceLines() {
		return InvoiceLine.all().filter("parentInvoice", this).fetch();
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

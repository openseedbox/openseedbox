package com.openseedbox.models;

import java.math.BigDecimal;
import siena.Column;
import siena.Index;
import siena.Table;

@Table("invoice_line")
public class InvoiceLine extends ModelBase {
	
	@Column("invoice_id")
	@Index("invoice_line_invoice_IDX")
	private Invoice parentInvoice;
	
	@Column("name")
	private String name;
	
	@Column("description")
	private String description;
	
	@Column("price")
	private BigDecimal price;
	
	@Column("quantity")
	private int quantity;
	

	/* Getters and Setters */
	public Invoice getParentInvoice() {
		return Invoice.findById(parentInvoice.id);
	}

	public void setParentInvoice(Invoice parentInvoice) {
		this.parentInvoice = parentInvoice;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
}

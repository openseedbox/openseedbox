package com.openseedbox.models;

import java.math.BigDecimal;

import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
public class InvoiceLine extends ModelBase {

	//needed?
	//@Column("invoice_id")
	//@Index("invoice_line_invoice_IDX")
	@Required
	@NotNull
	@ManyToOne
	private Invoice parentInvoice;
	
	private String name;
	
	private String description;
	
	private BigDecimal price;
	
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

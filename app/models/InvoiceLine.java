package models;

import java.math.BigDecimal;
import play.modules.siena.EnhancedModel;
import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Table;

@Table("invoice_line")
public class InvoiceLine extends EnhancedModel {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Column("invoice_id")
	public Invoice parentInvoice;
	
	@Column("name")
	public String name;
	
	@Column("description")
	public String description;
	
	@Column("price")
	public BigDecimal price;
	
	@Column("quantity")
	public int quantity;
	
}

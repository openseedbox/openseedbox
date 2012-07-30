package models;

import code.MessageException;
import code.Util;
import com.google.checkout.sdk.commands.ApiContext;
import com.google.checkout.sdk.commands.CartPoster;
import com.google.checkout.sdk.domain.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import play.modules.siena.EnhancedModel;
import siena.Column;
import siena.DateTime;
import siena.Generator;
import siena.Id;
import siena.Table;
import siena.Unique;

@Table("invoice")
public class Invoice extends EnhancedModel {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Column("account_id")
	public Account account;
	
	@DateTime
	@Column("invoice_date_utc")
	public Date invoiceDateUtc;
	
	@DateTime
	@Column("payment_date_utc")
	public Date paymentDateUtc;
	
	@Unique("google_order_number_unique")
	@Column("google_order_number")
	public String googleOrderNumber;
	
	public boolean hasBeenPaid() {
		return paymentDateUtc != null;
	}
	
	public Account getAccount() {
		return Account.all().filter("id", account.id).get();
	}
	
	private transient List<InvoiceLine> _items;
	public List<InvoiceLine> getItems() {
		if (_items == null) {
			_items = InvoiceLine.all().filter("parentInvoice", this).fetch();
		}
		return _items;
	}
	
	public BigDecimal getTotalAmount() {
		List<InvoiceLine> items = getItems();
		if (items != null) {
			BigDecimal total = BigDecimal.ZERO;
			for (InvoiceLine line : items) {
				total = total.add(line.price);
			}
			return total;
		}
		return BigDecimal.ZERO;
	}
	
	public String getGoogleCheckoutUrl() throws MessageException {
		ApiContext apic = Util.getGoogleApiContext();
		
		CartPoster.CheckoutShoppingCartBuilder cart = apic.cartPoster().makeCart();
		List<InvoiceLine> items = getItems();
		if (items == null) {
			throw new MessageException("Invoice has no items in it! No point in going to Google Checkout.");
		}
		for (InvoiceLine item : items) {
			Item i = new Item();
			i.setItemName(item.name);
			i.setItemDescription(item.description);
			i.setUnitPrice(apic.makeMoney(item.price));
			i.setQuantity(item.quantity);
			DigitalContent dc = new DigitalContent();
			dc.setEmailDelivery(false);
			dc.setDescription("You will have the option to migrate to your new plan the next time you access your account.");
			i.setDigitalContent(dc);
			cart.addItem(i);
		}
		CheckoutShoppingCart csc = cart.build();
		ShoppingCart sc = csc.getShoppingCart();
		AnyMultiple am = new AnyMultiple();
		am.getContent().add(this.id.toString());
		sc.setMerchantPrivateData(am);
		CheckoutRedirect redir = apic.cartPoster().postCart(csc);
		
		return redir.getRedirectUrl();		
	}
	
	public static Invoice createInvoice(Account a, Plan p) {
		Invoice i = new Invoice();
		i.account = a;
		i.invoiceDateUtc = new Date();
		i.save();
		
		InvoiceLine line = new InvoiceLine();
		line.name = p.getInvoiceLineName();
		line.description = p.getInvoiceLineDescription();
		line.quantity = 1;
		line.price = p.monthlyCost;
		line.parentInvoice = i;
		line.save();
		
		return i;
	}
	
	public static Invoice getByGoogleOrderNumber(String googleOrderNumber) {
		return Invoice.all().filter("googleOrderNumber", googleOrderNumber).get();
	}
	
	
	
}

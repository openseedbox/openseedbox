package models;

import code.MessageException;
import code.Util;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.modules.siena.EnhancedModel;
import play.mvc.Router;
import siena.Column;
import siena.DateTime;
import siena.Generator;
import siena.Id;
import siena.Index;
import siena.Table;

@Table("invoice")
public class Invoice extends EnhancedModel {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Column("account_id")
	@Index("account_payment_index")
	public Account account;
	
	@DateTime
	@Column("invoice_date_utc")
	public Date invoiceDateUtc;
	
	@DateTime
	@Column("payment_date_utc")
	@Index("account_payment_index")
	public Date paymentDateUtc;
	
	@Column("paypal_token")
	public String paypalToken;
	
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
	
	public String getPaymentUrl() throws MessageException {
		Properties c = Play.configuration;
		
		WSRequest req = WS.url(c.getProperty("paypal.api.url"));
		req.setParameter("USER", c.getProperty("paypal.api.username"));
		req.setParameter("PWD", c.getProperty("paypal.api.password"));
		req.setParameter("SIGNATURE", c.getProperty("paypal.api.signature"));
		req.setParameter("VERSION", "92.0");
		req.setParameter("METHOD", "SetExpressCheckout");
		req.setParameter("RETURNURL", Router.reverse("PaymentController.paymentReturn").secure().toString());
		req.setParameter("CANCELURL", Router.reverse("PaymentController.paymentCancel").secure().toString());
		req.setParameter("NOSHIPPING", "1");
		req.setParameter("REQNOSHIPPING", "0");		
		req.setParameter("EMAIL", this.getAccount().getPrimaryUser().emailAddress);
		req.setParameter("BRANDNAME", "OpenSeedbox");
		req.setParameter("PAYMENTREQUEST_0_INVNUM", this.id);
		req.setParameter("PAYMENTREQUEST_0_AMT", Util.formatMoney(this.getTotalAmount()));
		req.setParameter("PAYMENTREQUEST_0_CURRENCYCODE", "NZD");
		req.setParameter("PAYMENTREQUEST_0_PAYMENTACTION", "Sale");
			
		List<InvoiceLine> items = getItems();
		for (int x = 0; x < items.size(); x++) {
			InvoiceLine item = items.get(x);
			req.setParameter("L_PAYMENTREQUEST_0_AMT" + x, Util.formatMoney(item.price));
			req.setParameter("L_PAYMENTREQUEST_0_NAME" + x, item.name);
			req.setParameter("L_PAYMENTREQUEST_0_DESC" + x, item.description);	
			req.setParameter("L_PAYMENTREQUEST_0_QTY" + x, item.quantity);
			req.setParameter("L_PAYMENTREQUEST_0_ITEMCATEGORY" + x, "Digital");
		}
		
		HttpResponse res = req.get();
		Map<String, String> params = Util.getUrlParameters(res.getString());
		String token = params.get("TOKEN");
		String ack = params.get("ACK");
		if (ack != null && ack.contains("Success")) {
			String contextUrl = c.getProperty("paypal.api.contexturl");
			this.paypalToken = token;
			this.save();
			return String.format("%s?token=%s", contextUrl, token);
		}
		throw new MessageException("ACK: " + ack + " Bad PayPal response: " + res.getString());
	}
	
	public static Invoice getByPayPalToken(String token) {
		return Invoice.all().filter("paypalToken", token).get();
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
	
}

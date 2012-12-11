package models;

import com.openseedbox.code.MessageException;
import com.openseedbox.code.Util;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import play.Play;
import play.db.jpa.Model;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.Router;

@Table(name="invoice")
public class Invoice extends Model {
	
	@Column(name="account_id")
	private User user;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="invoice_date_utc")
	private Date invoiceDateUtc;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="payment_date_utc")
	private Date paymentDateUtc;
	
	@Column(name="paypal_token")
	private String paypalToken;
	
	@OneToMany
	private List<InvoiceLine> invoiceLines;
	
	public boolean hasBeenPaid() {
		return paymentDateUtc != null;
	}
	
	public List<InvoiceLine> getInvoiceLines() {
		return invoiceLines;
	}
	
	public BigDecimal getTotalAmount() {
		BigDecimal total = BigDecimal.ZERO;
		for (InvoiceLine line : invoiceLines) {
//			total = total.add(line.getPrice());
		}
		return total;
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
//		req.setParameter("EMAIL", this.getAccount().getPrimaryUser().getEmailAddress());
		req.setParameter("BRANDNAME", "OpenSeedbox");
		req.setParameter("PAYMENTREQUEST_0_INVNUM", this.id);
		req.setParameter("PAYMENTREQUEST_0_AMT", Util.formatMoney(this.getTotalAmount()));
		req.setParameter("PAYMENTREQUEST_0_CURRENCYCODE", "NZD");
		req.setParameter("PAYMENTREQUEST_0_PAYMENTACTION", "Sale");
			
		List<InvoiceLine> items = getInvoiceLines();
		for (int x = 0; x < items.size(); x++) {
			InvoiceLine item = items.get(x);
			req.setParameter("L_PAYMENTREQUEST_0_AMT" + x, Util.formatMoney(item.getPrice()));
			req.setParameter("L_PAYMENTREQUEST_0_NAME" + x, item.getName());
			req.setParameter("L_PAYMENTREQUEST_0_DESC" + x, item.getDescription());	
			req.setParameter("L_PAYMENTREQUEST_0_QTY" + x, item.getQuantity());
			req.setParameter("L_PAYMENTREQUEST_0_ITEMCATEGORY" + x, "Digital");
		}
		
		HttpResponse res = req.get();
		Map<String, String> params = Util.getUrlParameters(res.getString());
		String token = params.get("TOKEN");
		String ack = params.get("ACK");
		if (ack != null && ack.contains("Success")) {
			String contextUrl = c.getProperty("paypal.api.contexturl");
			this.paypalToken = token;
			//this.save();
			return String.format("%s?token=%s", contextUrl, token);
		}
		throw new MessageException("ACK: " + ack + " Bad PayPal response: " + res.getString());
	}
	
	public static Invoice getByPayPalToken(String token) {
		return Invoice.find("paypalToken = ?", token).first();
	}
	
	public static Invoice createInvoice(/*Account a, */Plan p) {
		Invoice i = new Invoice();
		//i.account = a;
		i.invoiceDateUtc = new Date();
		//i.save();
		
		InvoiceLine line = new InvoiceLine();
		line.setName(p.getInvoiceLineName());
		line.setDescription(p.getInvoiceLineDescription());
		line.setQuantity(1);
		line.setPrice(p.getMonthlyCost());
		line.setParentInvoice(i);
		//line.save();
		
		return i;
	}
	
}

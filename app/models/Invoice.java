package models;

import com.openseedbox.code.MessageException;
import com.openseedbox.code.Util;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.Router;
import siena.Column;
import siena.DateTime;
import siena.Table;

@Table("invoice")
public class Invoice extends ModelBase {
	
	@Column("user_id")
	private User user;
		
	@DateTime
	@Column("invoice_date")
	private Date invoiceDate;
	
	@DateTime
	@Column("payment_date")
	private Date paymentDate;
	
	@Column("paypal_token")
	private String paypalToken;
	
	public static Invoice getByPayPalToken(String token) {
		return Invoice.all().filter("paypalToken", token).get();
	}
	
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
		req.setParameter("EMAIL", getUser().getEmailAddress());
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
			this.save();
			return String.format("%s?token=%s", contextUrl, token);
		}
		throw new MessageException("ACK: " + ack + " Bad PayPal response: " + res.getString());
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

	public String getPaypalToken() {
		return paypalToken;
	}

	public void setPaypalToken(String paypalToken) {
		this.paypalToken = paypalToken;
	}
	
	
}

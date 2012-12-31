package com.openseedbox.bitpay;

import com.google.gson.Gson;
import com.openseedbox.Config;
import com.openseedbox.bitpay.BitPayInvoice.BitPayCurrency;
import com.openseedbox.code.MessageException;
import com.openseedbox.code.Util;
import com.openseedbox.models.Invoice;
import com.openseedbox.models.User;
import org.bouncycastle.jce.provider.JDKKeyFactory;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.Router;

public class BitPay {
	
	public static BitPayResponse createInvoice(Invoice i) {
		BitPayInvoice bpi = new BitPayInvoice();
		bpi.setNotificationURL(getNotificationUrl());
		User u = i.getUser();
		bpi.setBuyerEmail(u.getEmailAddress());
		bpi.setBuyerName(u.getDisplayName());
		bpi.setNotificationEmail(u.getEmailAddress());
		bpi.setPrice(i.getTotalAmount().toString());
		bpi.setCurrency(BitPayCurrency.USD);
		bpi.setItemDesc("Payment for OpenSeedbox Invoice #" + i.getId());
		bpi.setRedirectURL(getRedirectUrl("" + i.getId()));
		bpi.setOrderID("" + i.getId());
		HttpResponse res = getRequest("https://bitpay.com/api/invoice", bpi).post();
		if (res.success()) {
			return new Gson().fromJson(res.getString(), BitPayResponse.class);
		} else {
			throw new MessageException("Unable to create BitPay invoice: " + res.getStatus());
		}
	}
	
	private static String getRedirectUrl(String invoiceId) {
		return Router.getFullUrl("Account.invoiceDetails", Util.convertToMap(new Object[] { "id", invoiceId }));
	}
	
	private static String getNotificationUrl() {
		return Router.getFullUrl("Payment.notify");
	}
	
	private static WSRequest getRequest(String url, Object body) {
		WSRequest r = WS.url(url);
		r.authenticate(Config.getBitPayAPIKey(), "");
		r.setHeader("Content-Type", "application/json");
		r.body = new Gson().toJson(body);
		return r;
	}
	
}

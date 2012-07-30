package controllers;

import code.BigDecimalUtils;
import code.MessageException;
import code.Util;
import code.checkout.PlayHttpServletRequest;
import code.checkout.PlayHttpServletResponse;
import com.google.checkout.sdk.commands.ApiContext;
import com.google.checkout.sdk.commands.CheckoutException;
import com.google.checkout.sdk.commands.OrderCommands;
import com.google.checkout.sdk.domain.*;
import com.google.checkout.sdk.notifications.BaseNotificationDispatcher;
import com.google.checkout.sdk.notifications.Notification;
import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import models.Invoice;
import models.PlanSwitch;
import models.User;
import models.checkout.GoogleNotification;
import play.Logger;


public class PaymentController extends BaseController {
	
	public static void callback() throws MessageException {
		HttpServletRequest req = new PlayHttpServletRequest(request);
		HttpServletResponse res = new PlayHttpServletResponse(response);
		ApiContext api = Util.getGoogleApiContext();
		try {
			api.handleNotification(new MyNotificationDispatcher(req, res));
		} catch (CheckoutException ex) {
			error(ex);
		}
	}
	
	public static class MyNotificationDispatcher extends BaseNotificationDispatcher {

		public MyNotificationDispatcher(HttpServletRequest req, HttpServletResponse res) {
			super(req, res);
		}

		protected boolean hasAlreadyHandled(String serialNumber, OrderSummary orderSummary, Notification notification) throws Exception {
			return (GoogleNotification.all().filter("serialNumber", serialNumber).count() > 0);
		}	

		public void rememberSerialNumber(String serialNumber, OrderSummary orderSummary, Notification notification) {
			GoogleNotification gn = new GoogleNotification();
			gn.serialNumber = serialNumber;
			gn.insert();
		}

		@Override
		protected void onNewOrderNotification(OrderSummary orderSummary, NewOrderNotification notification) throws Exception {
			String id = getInvoiceId(notification.getShoppingCart());
			String order_id = notification.getGoogleOrderNumber();
			Logger.debug("New order for invoice: %s", id);
			Invoice i = Invoice.getByKey(id);
			i.googleOrderNumber = order_id;
			i.save();
			OrderCommands oc = Util.getGoogleApiContext().orderCommands(order_id);
			oc.chargeAndShipOrder();
		}

		@Override
		protected void onChargeAmountNotification(OrderSummary orderSummary, ChargeAmountNotification notification) throws Exception {
			String orderNumber = notification.getGoogleOrderNumber();
			Invoice i = Invoice.getByGoogleOrderNumber(orderNumber);
			if (i == null) {
				throw new Exception("No invoice associated with google order number: " + orderNumber);
			}
			BigDecimal paymentAmount = notification.getTotalChargeAmount().getValue();
			BigDecimal invoiceAmount = i.getTotalAmount();
			if (BigDecimalUtils.GreaterThanOrEqual(paymentAmount, invoiceAmount)) {
				//set the payment date on the invoice
				GregorianCalendar gc =  notification.getTimestamp().toGregorianCalendar();
				gc.setTimeZone(TimeZone.getTimeZone("GMT"));
				i.paymentDateUtc = gc.getTime();
				i.save();
				
				//update user details, if they just switched from a free plan to a paid one then mark it as paid
				//so they can be allowed access
				User u = i.getAccount().getPrimaryUser();
				if (u != null) {
					u.paidForPlan = true;
					u.save();
				}	
				
				PlanSwitch.notifyUser(u, true);
			}
			Logger.debug("User paid amount for invoice: %s ($%s)", i.id, paymentAmount);
		}

		private String getInvoiceId(ShoppingCart sc) {
			if (sc != null) {
				AnyMultiple am = sc.getMerchantPrivateData();
				if (am != null) {
					List<Object> content = am.getContent();
					if (content != null) {
						Object item = content.get(0);
						if (item != null) {
							return item.toString();
						}
					}
				}
			}
			return null;
		}

	}	
	
}

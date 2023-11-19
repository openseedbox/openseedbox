package controllers;

import com.openseedbox.models.Plan;
import com.openseedbox.models.Invoice;
import com.openseedbox.models.User;
import com.openseedbox.code.MessageException;
import java.util.List;
import play.cache.Cache;
import play.data.validation.Email;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Before;

public class Account extends Base {
	
	@Before
	public static void checkLoggedIn() {
		User u = getCurrentUser();
		if (u == null) {
			Auth.logout();
		}
	}
	
	@Before
	public static void uncacheUser() {
		//When a users account settings are updated, the user should be removed from
		//the cache so that the settings actually take effect, since basically
		//every operation involving a User calls getCurrentUser() which will retrieve
		//the user from the cache if its in there.
		Cache.delete(getCurrentUserCacheKey());
	}
	
	public static void index() {
		plans();
	}
	
	public static void plans() {
		String active = "plans";
		List<Plan> plans = Plan.all().filter("visible", true).order("monthlyCost").fetch();
		render("account/plans.html", active, plans);
	}
	
	public static void buyPlan(long id) throws MessageException {
		User user = getCurrentUser();		
		Plan newPlan = Plan.getByKey(id);
				
		if (user.getUnpaidInvoices().size() > 0) {
			setGeneralErrorMessage("You cannot change your plan if you have unpaid invoices!");
			plans();
		}
		
		//if the new plan is free, switch to it immediately
		if (newPlan.isFree()) {
			invoicePlan(newPlan.getId());
		}
		
		Plan oldPlan = user.getPlan();
		String active = "plans";
		render("account/buyplan.html", active, user, newPlan, oldPlan);
	}
	
	public static void invoicePlan(long id) {
		//check to see if there is an outstanding invoice for this plan.
		//if there is, use that one instead of creating a new one
		
		Plan newPlan = Plan.getByKey(id);
		User u = getCurrentUser();
		
		//if an invoice doesnt need to be created because the plan is free, switch immediately
		if (newPlan.isFree()) {
			//switch to plan
			u.setPlan(newPlan);
			u.save();
			setGeneralMessage("Plan successfully switched to '" + newPlan.getName() + "'");
			Client.index(null, null);
		}
		
		//check to see if theres already an active invoice for this plan
		//if there is, dont create another one
		List<Invoice> unpaid = u.getUnpaidInvoices();		
		Invoice invoice = null;
		if (unpaid.size() > 0) {
			invoice = unpaid.get(0);
		}
		
		if (invoice != null) {
			Account.invoiceDetails(invoice.getId());
		} else {		
			//no active invoices for this plan; create one
			u.setPlan(newPlan);			
			u.save();
			Invoice i = Invoice.createInvoice(u, newPlan);			
			Account.invoiceDetails(i.getId());
		}
	}
	
	public static void payInvoice(long id) {		
		Invoice i = Invoice.findById(id);
		redirect(i.getPaymentUrl());
	}
	
	public static void invoices() {
		String active = "invoices";
		User u = getCurrentUser();
		List<Invoice> unpaid_invoices = u.getUnpaidInvoices();
		List<Invoice> paid_invoices = u.getPaidInvoices();	
		render("account/invoices.html", active, unpaid_invoices, paid_invoices);
	}
	
	public static void invoiceDetails(long id) {		
		String active = "invoices";
		Invoice invoice = Invoice.findById(id);
		if (invoice == null) {
			setGeneralErrorMessage("No such invoice with id: " + id);
			invoices();
		}
		if (!invoice.getUser().getEmailAddress().equals(getCurrentUser().getEmailAddress())) {
			setGeneralErrorMessage("The invoice isnt yours!");
			invoices();
		}
		render("account/invoice-details.html", invoice, active);
	}
	
	public static void details() {		
		String active = "details";		
		render("account/details.html", active);
	}
	
	public static void updateDetails(@Valid User user) {		
		if (!Validation.hasErrors()) {
			user.save();
			setGeneralMessage("Account details updated successfully");
			details();
		}
		Validation.keep();
		params.flash();
		details();		
	}	
	
	public static void settings() {
		String active = "settings";
		render("account/settings.html", active);
	}
	
	public static void api() {
		String active = "api";
		render("account/api.html", active);
	}
	
	public static void apiPost() {
		getCurrentUser().generateApiKey();
		api();
	}
	
}

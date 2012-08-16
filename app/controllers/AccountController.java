package controllers;

import code.MessageException;
import code.jobs.PlanSwitcherJob;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.*;
import notifiers.Mails;
import org.joda.time.DateTimeZone;
import play.cache.Cache;
import play.data.validation.Email;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Before;

public class AccountController extends BaseController {
	
	@Before
	public static void checkLoggedIn() {
		User u = getCurrentUser();
		if (u == null) {
			AuthController.logout();
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
	
	public static void buyPlan(Long newPlanID) throws MessageException {
		User user = getCurrentUser();
		Plan newPlan = Plan.getByKey(newPlanID);
		
		List<Invoice> unpaid = user.getUnpaidInvoices();
		if (unpaid.size() > 0) {
			setGeneralErrorMessage("You cannot change your plan if you have unpaid invoices!");
			plans();
		}
		
		//if the new plan is free, switch to it immediately
		if (newPlan.isFree()) {
			invoicePlan(newPlan.id);
		}
		
		Plan oldPlan = user.getPlan();
		String active = "plans";
		render("account/buyplan.html", active, user, newPlan, oldPlan);
	}
	
	public static void invoicePlan(Long newPlanID) throws MessageException {
		//check to see if there is an outstanding invoice for this plan.
		//if there is, use that one instead of creating a new one
		Plan newPlan = Plan.getByKey(newPlanID);
		User u = getCurrentUser();
		
		//if an invoice doesnt need to be created because the plan is free, switch immediately
		if (newPlan.isFree()) {
			//switch to plan
			FreeSlot fs = consumeSlot(u, newPlan);
			PlanSwitch.create(u.getPrimaryAccount(), fs, null);
			PlanSwitch.notifyUser(u, false);
			ClientController.index();
		}
		
		//check to see if theres already an active invoice for this plan
		//if there is, dont create another one
		Invoice invoice = Invoice.all()
				.filter("account", u.getPrimaryAccount())
				.filter("paymentDateUtc", null)
				.get();
		
		if (invoice != null) {
			AccountController.invoiceDetails(invoice.id);
		} else {		
			//no active invoices for this plan; create one
			FreeSlot fs = consumeSlot(u, newPlan);
			u.paidForPlan = false;
			u.save();
			Invoice i = Invoice.createInvoice(u.getPrimaryAccount(), newPlan);
			PlanSwitch.create(u.getPrimaryAccount(), fs, i);
			AccountController.invoiceDetails(i.id);
		}
	}
	
	public static void paypalInvoice(Long id) throws MessageException {
		Invoice i = Invoice.findById(id);
		redirect(i.getPaymentUrl());
	}
	
	public static void switchPlans() {
		User user = getCurrentUser();
		PlanSwitch ps = PlanSwitch.forUser(user);
		if (ps != null) {
			if (!ps.inProgress) {
				new PlanSwitcherJob(ps).now();
			}
		}
		render("client/switchplans.html", user, ps);
	}		
	
	public static void invoices() {
		String active = "invoices";
		User u = getCurrentUser();
		List<Invoice> unpaid_invoices = u.getUnpaidInvoices();
		List<Invoice> paid_invoices = u.getPaidInvoices();	
		render("account/invoices.html", active, unpaid_invoices, paid_invoices);
	}
	
	public static void invoiceDetails(Long invoiceId) {
		String active = "invoices";
		Invoice invoice = Invoice.findById(invoiceId);
		if (invoice == null) {
			setGeneralMessage("No such invoice with id: " + invoiceId);
		}
		if (invoice.getAccount().id != getCurrentUser().getPrimaryAccount().id) {
			setGeneralMessage("This invoice isnt yours!");
		}
		render("account/invoice-details.html", invoice, active);
	}
	
	public static void details(@Valid User user) {
		String active = "details";
		boolean success = false;
		if (user != null && !Validation.hasErrors()) {
			user.save();
			success = true;
		}
		if (user == null) { user = getCurrentUser(); }	
		List<String> timeZones = new ArrayList<String>(DateTimeZone.getAvailableIDs());
		//List<SelectItem> timeZones = Util.toSelectItems(tz);
		render("account/details.html", active, user, success, timeZones);
	}
	
	public static void invite() {
		String active = "invite";
		List<Invitation> invitations = getCurrentUser().getInvitations();
		render("account/invite.html", active, invitations);
	}
	
	public static void removeInvitedUser(Long id) {
		Invitation.getByKey(id).delete();
		invite();
	}
	
	public static void resendInvitationEmail(Long id) {
		Invitation i = Invitation.getByKey(id);
		Mails.inviteUser(i);
		flash.put("sucess", "Email successfully sent to " + i.emailAddress + ".");
		invite();
	}
	
	public static void inviteUser(@Email String emailAddress) {
		if (!Validation.hasErrors()) {
			User u = getCurrentUser();
			//check email address isnt already in list
			boolean found = false;
			for (Invitation i : u.getInvitations()) {
				if (i.emailAddress.equals(emailAddress)) {
					found = true;
					break;
				}
			}
			if (!found) {
				Invitation i = new Invitation();
				i.invitingUser = u;
				i.emailAddress = emailAddress;
				i.invitationDate = new Date();
				i.insert();
				
				Mails.inviteUser(i);
				
			} else {
				Validation.addError("emailAddress", "You have already invited this user!");
			}
		}
		Validation.keep();
		invite();
	}
	
	public static void api() {
		String active = "api";
		render("account/api.html", active);
	}
	
	public static void apiPost() {
		getCurrentUser().generateApiKey();
		api();
	}
	
	protected static FreeSlot consumeSlot(User u, Plan p) {
		List<FreeSlot> eligibleSlots = p.getFreeSlots();
		FreeSlot fs = eligibleSlots.get(0);
		
		//take a slot
		fs.freeSlots -= 1;
		fs.save();
		
		return fs;
	}
	
}

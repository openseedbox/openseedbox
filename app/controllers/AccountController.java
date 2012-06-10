package controllers;

import code.MessageException;
import code.Util;
import code.Util.SelectItem;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.*;
import notifiers.Mails;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
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
	
	public static void setPlan(String id) {
		User u = getCurrentUser();
		Plan p = Plan.getByKey(id);
		List<FreeSlot> fs = FreeSlot.all().filter("plan", p).filter("freeSlots >", 0).fetch();
		try {
			if (fs.isEmpty()) {
				throw new MessageException(
						String.format("No free slots left for plan %s!", p.name));
			}
			FreeSlot f = fs.get(0);
			f.freeSlots -= 1; //take a slot
			f.save(); //save ASAP so site gets updated
			
			//figure out transmission port and save to account
			int port = getTransmissionPort(f.getNode());
			Account a = u.getPrimaryAccount();
			a.transmissionPort = port;
			a.node = f.node;
			a.save();
			u.setPlan(p);
			u.save();			
		
			//start transmission on node for user. This will create all the needed files
			u.getTransmission().start();	
			
		} catch (MessageException ex) {
			Validation.addError("general", ex.getMessage());
		}
		Validation.keep(); 
		plans();
	}
	
	public static void invoices() {
		String active = "invoices";
		render("account/invoices.html", active);
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
	
	private static int getTransmissionPort(Node n) throws MessageException {
		Account biggestPort =
				Account.all()
				.filter("node", n)
				.filter("transmissionPort >", 0)
				.order("-transmissionPort").limit(1).get();
		int p;
		if (biggestPort != null) {
			p = biggestPort.getTransmissionPort();
			if (p > 0) {
				p++;
			} else {
				p = 3000; //start at 3000
			}
		} else {
			p = 3000;
		}
		return p;		
	}
	
}

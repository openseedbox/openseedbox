package controllers;

import code.MessageException;
import java.util.List;
import models.Account;
import models.FreeSlot;
import models.Node;
import models.Plan;
import models.User;
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
		render("account/details.html", active, user, success);
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

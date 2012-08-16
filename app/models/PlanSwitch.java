package models;

import code.BigDecimalUtils;
import java.math.BigDecimal;
import java.util.Date;
import play.modules.siena.EnhancedModel;
import play.mvc.Router;
import siena.*;

@Table("plan_switch")
public class PlanSwitch extends EnhancedModel {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@DateTime
	@Column("create_date_utc")
	public Date createDateUtc;
	
	@Column("account_id")
	public Account account;
	
	@Column("free_slot_id")
	public FreeSlot freeSlot;
	
	@Column("invoice_id")
	public Invoice invoice;
	
	@Column("in_progress")
	public boolean inProgress;
	
	@DateTime
	@Column("switch_date_utc")
	public Date switchDateUtc;
	
	@Text
	@Column("error")
	public String error;
	
	public Account getAccount() {
		return Account.getByKey(account.id);
	}
	
	public FreeSlot getFreeSlot() {
		return FreeSlot.getByKey(freeSlot.id);
	}
	
	public Invoice getInvoice() {
		if (invoice != null) {
			return Invoice.getByKey(invoice.id);
		}
		return null;
	}
	
	public boolean canSwitch() {
		Invoice i = getInvoice();
		if (i != null) {
			return i.hasBeenPaid();
		}
		//just incase there was no invoice and the user is switching to a free plan
		FreeSlot fs = getFreeSlot();
		return (BigDecimalUtils.LessThanOrEqual(fs.getPlan().monthlyCost, BigDecimal.ZERO));
	}
	
	public static PlanSwitch create(Account a, FreeSlot fs, Invoice i) {
		PlanSwitch ps = new PlanSwitch();
		ps.createDateUtc = new Date();
		ps.account = a;
		ps.freeSlot = fs;
		ps.invoice = i;
		ps.insert();
		return ps;
	}
	
	public static PlanSwitch forUser(User u) {
		return PlanSwitch.all()
				.filter("account", u.getPrimaryAccount())
				.filter("switchDateUtc", null).limit(1).get();
	}
	
	public static void notifyUser(User u, boolean wasPaid) {
		String url = Router.reverse("AccountController.switchPlans").url;
		String message = "Please click <a href='" + url + "'>here</a> to activate your new plan.";
		if (wasPaid) {
			message = "Your invoice has been paid. " + message;
		}
		u.addUserMessage("Ready to switch plans", message, UserMessage.Type.SWITCHPLAN);
	}
	
}

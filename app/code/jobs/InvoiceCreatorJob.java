package code.jobs;

import play.jobs.Every;
import play.jobs.Job;

@Every("1h")
public class InvoiceCreatorJob extends Job {

	@Override
	public void doJob() throws Exception {
		//get accounts where last invoice > 1 month
		//check to see if plan is scheduled to change
		//change plan
		//generate new invoice
		//send notification email for payment
		
		//get accounts where there are unpaid invoices > 5 days
		//send warning
		
		//get accounts where there are unpaid invoices > 5 days
		//cancel account
	}
	
}

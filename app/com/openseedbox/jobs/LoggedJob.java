package com.openseedbox.jobs;

import com.openseedbox.code.Util;
import com.openseedbox.models.EventBase;
import com.openseedbox.models.JobEvent;
import java.util.Date;
import play.Logger;
import play.Play;
import play.Play.Mode;

public abstract class LoggedJob<T extends EventBase> extends GenericJob {

	@Override
	protected GenericJobResult runJob() throws Exception {				
		long startMillis = System.currentTimeMillis();
		GenericJobResult res = super.runJob();
		long endMillis = System.currentTimeMillis();
		EventBase eb = getJobEvent();
		eb.setDurationMilliseconds(endMillis - startMillis);
		eb.setCompletionDate(new Date());
		if (res.hasError()) {
			eb.setSuccessful(false);			
			eb.setStackTrace(Util.getStackTrace(res.getError()));
			if (Play.mode == Mode.DEV) {
				Logger.error(res.getError(), "Error executing job");
			}			
		} else {
			eb.setSuccessful(true);
		}		
		logResult(res, (T) eb);
		return res;
	}	
	
	protected T getJobEvent() {
		return (T) new JobEvent(this);
	}
	
	protected void logResult(GenericJobResult res, T event) {		
		event.update();		
	}
	
	protected void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (Exception ex) {
			//fuck off java
		}
	}
	
}

package com.openseedbox.models;

import java.util.Date;
import org.apache.commons.lang.time.DurationFormatUtils;
import siena.Column;
import siena.Text;

public abstract class EventBase extends ModelBase {
	
	protected boolean successful;
	@Column("stack_trace") @Text private String stackTrace;
	@Column("start_date") protected Date startDate;
	@Column("end_date") protected Date completionDate;
	@Column("duration_milliseconds") protected long durationMilliseconds;
	
	public String getLastRan() {
		long now = new Date().getTime();
		long then = getStartDate().getTime();
		long distance = now - then;
		return DurationFormatUtils.formatDurationWords(distance, true, true)
				  .replace("hour","h")
				  .replace("hs", "h")
				  .replace("minute", "m")
				  .replace("ms", "m")
				  .replace("second", "s")
				  .replace("ss", "s");
	}
	
	/* Getters and Setters */
	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getCompletionDate() {
		return completionDate;
	}

	public void setCompletionDate(Date completionDate) {
		this.completionDate = completionDate;
	}		

	public long getDurationMilliseconds() {
		return durationMilliseconds;
	}

	public void setDurationMilliseconds(long durationMilliseconds) {
		this.durationMilliseconds = durationMilliseconds;
	}	
	
}

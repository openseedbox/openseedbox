package com.openseedbox.models;

import java.util.Date;
import org.apache.commons.lang.time.DurationFormatUtils;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class EventBase extends ModelBase {
	
	protected boolean successful;
	@Lob
	private String stackTrace;
	protected Date startDate;
	@Column( name = "end_date") protected Date completionDate;
	protected long durationMilliseconds;
	
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

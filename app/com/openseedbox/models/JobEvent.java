package com.openseedbox.models;

import com.openseedbox.jobs.JobName;
import com.openseedbox.jobs.LoggedJob;
import java.util.Date;
import java.util.List;
import siena.Column;
import siena.Table;

@Table("job_event")
public class JobEvent extends EventBase {

	@Column("job_class") private String jobClass;
	@Column("job_title") private String jobTitle;
	
	public JobEvent(LoggedJob job) {
		startDate = new Date();		
		jobClass = job.getClass().getName();
		JobName n = job.getClass().getAnnotation(JobName.class);
		jobTitle = (n != null) ? n.value() : jobClass;
		this.insert();
	}	
	
	public static List<JobEvent> getLast30() {
		return JobEvent.all().order("-completionDate").limit(30).fetch();
	}
	
	public static List<JobEvent> getLast(Class jobClass, int limit) {
		return JobEvent.all().filter("jobClass", jobClass.getName())
				  .order("-completionDate").limit(limit).fetch();
	}
	
	/* Getters and Setters */
	public String getJobClass() {
		return jobClass;
	}

	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}
}

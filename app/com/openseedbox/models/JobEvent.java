package com.openseedbox.models;

import com.openseedbox.jobs.JobName;

import java.util.*;

import com.openseedbox.jobs.admin.LoggedAdminJob;
import siena.Column;
import siena.Table;

@Table("job_event")
public class JobEvent extends EventBase {

	@Column("job_class") private String jobClass;
	@Column("job_title") private String jobTitle;
	
	public JobEvent(LoggedAdminJob job) {
		startDate = new Date();		
		jobClass = job.getClass().getName();
		JobName n = job.getClass().getAnnotation(JobName.class);
		jobTitle = (n != null) ? n.value() : jobClass;
		this.insert();
	}	
	
	public static List<JobEvent> getLast30() {
		return JobEvent.all().order("-startDate").limit(30).fetch();
	}
	
	public static List<JobEvent> getLast(Class<? extends LoggedAdminJob> jobClass, int limit) {
		List<Class<? extends LoggedAdminJob>> oneList = new ArrayList<Class<? extends LoggedAdminJob>>();
		oneList.add(jobClass);
		return getLastList(oneList, limit);
	}

	public static List<JobEvent> getLastList(List<Class<? extends LoggedAdminJob>> jobClasses, int limit) {
		List<String> jobClassNames = new ArrayList<String>();
		for (Class<? extends LoggedAdminJob> jobClass: jobClasses) {
			jobClassNames.add(jobClass.getName());
		}
		return JobEvent.all().filter("jobClass IN", jobClassNames)
				.order("-startDate").limit(limit).fetch();
	}

	public static void deleteOlderThan(Date date) {
		JobEvent.all().filter("startDate <", date).delete();
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

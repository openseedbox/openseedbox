package com.openseedbox.models;

import com.openseedbox.jobs.JobName;

import javax.persistence.Entity;
import java.util.*;

import com.openseedbox.jobs.admin.LoggedAdminJob;

@Entity
public class JobEvent extends EventBase {

	private String jobClass;
	private String jobTitle;
	
	public JobEvent(LoggedAdminJob job) {
		startDate = new Date();		
		jobClass = job.getClass().getName();
		JobName n = job.getClass().getAnnotation(JobName.class);
		jobTitle = (n != null) ? n.value() : jobClass;
		this.save();
	}	
	
	public static List<JobEvent> getLast30() {
		return JobEvent.<JobEvent>all().orderBy("startDate desc").setMaxRows(30).findList();
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
		return JobEvent.<JobEvent>all().where().in("jobClass", jobClassNames)
				.orderBy("startDate desc").setMaxRows(limit).findList();
	}

	public static int deleteOlderThan(Date date) {
		//JobEvent.all().where().lt("startDate", date).delete();
		return createDeleteQuery(JobEvent.class,"startDate < ?", new Object[] { date } ).execute();
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

package com.openseedbox.jobs.node;

import com.openseedbox.jobs.admin.LoggedAdminJob;
import com.openseedbox.models.JobEvent;
import com.openseedbox.models.Node;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

public abstract class LoggedNodeJob extends LoggedAdminJob {
	protected final Node n;

	public LoggedNodeJob(Node n) {
		this.n = Objects.requireNonNull(n, "Node  for " + this + " shouldn't be null!");
	}

	@Override
	protected JobEvent getEvent() {
		JobEvent event = Objects.requireNonNull(super.getEvent(), "Event for " + this + " shouldn't be null!");
		event.setJobTitle(appendNodeName(event));
		event.update();
		return event;
	}

	private String appendNodeName(JobEvent event) {
		String jobName = event.getJobTitle();
		String name = !StringUtils.isEmpty(jobName) ? jobName + " - " : "";
		return name + n.getName();
	}
}

package squashtm.testautomation.jenkins.internal.beans;

import java.util.ArrayList;
import java.util.Collection;

public class JobList {

	private Collection<Job> jobs = new ArrayList<Job>();

	public Collection<Job> getJobs() {
		return jobs;
	}

	public void setJobs(Collection<Job> jobs) {
		this.jobs = jobs;
	}
	
	
}

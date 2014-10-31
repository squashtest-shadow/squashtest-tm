package org.squashtest.tm.domain.milestone;

import org.squashtest.tm.domain.milestone.Milestone;

public class ExpandedMilestone {

	private Milestone milestone;
	private String translatedStatus;
	private String translatedEndDate;
	
	
	public String getTranslatedEndDate() {
		return translatedEndDate;
	}
	public void setTranslatedEndDate(String translatedEndDate) {
		this.translatedEndDate = translatedEndDate;
	}
	public Milestone getMilestone() {
		return milestone;
	}
	public void setMilestone(Milestone milestone) {
		this.milestone = milestone;
	}
	public String getTranslatedStatus() {
		return translatedStatus;
	}
	public void setTranslatedStatus(String translatedStatus) {
		this.translatedStatus = translatedStatus;
	}
	
	 
}

/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.domain.bugtracker;

import java.util.Date;

import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.Category;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.domain.Status;
import org.squashtest.csp.core.bugtracker.domain.User;
import org.squashtest.csp.core.bugtracker.domain.Version;

public class BTIssueDecorator extends BTIssue {

	protected BTIssue issue;
	private long issueId;
	
	public BTIssueDecorator(BTIssue issue) {
		this.issue = issue;
	}

	public long getIssueId() {
		return issueId;
	}
	
	public void setIssueId(long issueId) {
		this.issueId = issueId;
	}
	
	public String getId(){
		return this.issue.getId();
	}
	
	public void setId(String id){
		this.issue.setId(id);
	}
	

	public Category getCategory() {
		return this.issue.getCategory();
	}

	public void setCategory(Category category) {
		this.issue.setCategory(category);
	}


	public String getSummary() {
		return this.issue.getSummary();
	}

	public void setSummary(String summary) {
		this.issue.setSummary(summary);
	}

	public BTProject getProject() {
		return this.issue.getProject();
	}

	public void setProject(BTProject project) {
		this.issue.setProject(project);
	}

	public Priority getPriority() {
		return this.issue.getPriority();
	}

	public void setPriority(Priority priority) {
		this.issue.setPriority(priority);
	}

	public Version getVersion() {
		return this.issue.getVersion();
	}

	public void setVersion(Version version) {
		this.issue.setVersion(version);
	}

	public User getReporter() {
		return this.issue.getReporter();
	}

	public void setReporter(User reporter) {
		this.issue.setReporter(reporter);
	}

	public User getAssignee() {
		return this.issue.getAssignee();
	}

	public void setAssignee(User assignee) {
		this.issue.setAssignee(assignee);
	}

	public String getDescription() {
		return this.issue.getDescription();
	}

	public void setDescription(String description) {
		this.issue.setDescription(description);
	}

	public String getComment() {
		return this.issue.getComment();
	}

	public void setComment(String comment) {
		this.issue.setComment(comment);
	}


	public Date getCreatedOn() {
		return this.issue.getCreatedOn();
	}


	public void setCreatedOn(Date createdOn) {
		this.issue.setCreatedOn(createdOn);
	}
	
	public Status getStatus(){
		return this.issue.getStatus();
	}
	
	public void setStatus(Status status){
		this.issue.setStatus(status);
	}
	

	public void setBugtracker(String btName){
		this.issue.setBugtracker(btName);
	}
	
	public String getBugtracker(){
		return this.issue.getBugtracker();
	}
	
	
	public void setDummy(Boolean dummy){
		this.issue.setDummy(dummy);
	}

	public boolean hasBlankId(){
		return this.issue.hasBlankId();
	}
}

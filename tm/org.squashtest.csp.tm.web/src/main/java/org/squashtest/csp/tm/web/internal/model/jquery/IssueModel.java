/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.web.internal.model.jquery;

import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.Category;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.domain.User;
import org.squashtest.csp.core.bugtracker.domain.Version;

/*
 * Note : we set the NOSONAR flag on the setters for Array-type properties otherwise it rings because we don't clone them.
 * We can reasonably ignore those warnings because that class is meant to be serialized from/to json. Of course, that assumption holds
 * as long as no one uses that class for another purpose.
 * 
 * 
 * @author bsiri
 */

public class IssueModel {
	
	private static final String LABEL_UNUSED = "unused";
	/* ** data sent from server to client ** */
	private Object[] priorities;
	private Object[] users;
	private String defaultDescription;
	private Object[] versions;
	private Object[] categories;
	private String projectId;
	

	/* ** data received from client ** */
	private String assigneeId;
	private String priorityId;
	private String versionId;
	private String categoryId;
	private String summary;
	private String description;
	private String comment;
	

	
	public Object[] getPriorities() {
		return priorities;
	}


	public void setPriorities(Object[] priorities) {
		this.priorities = priorities; //NOSONAR that class is used in one specific case and could barely be used elsewhere. In this sole usecase the arrays will never be modified.
	}


	public Object[] getUsers() {
		return users;
	}


	public void setUsers(Object[] users) {
		this.users = users; //NOSONAR that class is used in one specific case and could barely be used elsewhere. In this sole usecase the arrays will never be modified.
		}


	public String getDefaultDescription() {
		return defaultDescription;
	}

	public void setDefaultDescription(String defaultDescription) {
		this.defaultDescription = defaultDescription;
	}

	public Object[] getVersions() {
		return versions;
	}

	public void setVersions(Object[] versions) {
		this.versions = versions; //NOSONAR that class is used in one specific case and could barely be used elsewhere. In this sole usecase the arrays will never be modified.
		}

	public Object[] getCategories() {
		return categories;
	}

	public void setCategories(Object[] categories) {
		this.categories = categories; //NOSONAR that class is used in one specific case and could barely be used elsewhere. In this sole usecase the arrays will never be modified.
		}


	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getAssigneeId() {
		return assigneeId;
	}

	public void setAssigneeId(String assigneeId) {
		this.assigneeId = assigneeId;
	}

	public String getPriorityId() {
		return priorityId;
	}

	public void setPriorityId(String priorityId) {
		this.priorityId = priorityId;
	}

	public String getVersionId() {
		return versionId;
	}

	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}



	public BTIssue toIssue(){
		
		BTIssue issue = new BTIssue();
		
		BTProject project = new BTProject(getProjectId(), LABEL_UNUSED);
		User assignee = new User(getAssigneeId(), LABEL_UNUSED);
		Priority priority = new Priority(getPriorityId(), LABEL_UNUSED);
		Version version = new Version(getVersionId(),LABEL_UNUSED);
		Category category = new Category(getCategoryId(),LABEL_UNUSED);
		
		

		issue.setProject(project);
		issue.setAssignee(assignee);
		issue.setPriority(priority);
		issue.setVersion(version);
		issue.setComment(getComment());
		issue.setSummary(getSummary());
		issue.setDescription(getDescription());
		issue.setCategory(category);
		
		return issue;
		
	}
	
	
}

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


/*
 * Note : we set the NOSONAR flag on the setters for Array-type properties otherwise it rings because we don't clone them.
 * We can reasonably ignore those warnings because that class is meant to be serialized from/to json. Of course, that assumption holds
 * as long as no one uses that class for another purpose.
 * 
 * 
 * @author bsiri
 */

public class IssueModel {
	
	/* ** data sent from server to client ** */
	private Object[] priorities;
	private Object[] users;
	private String defaultDescription;
	private Object[] versions;
	private Object[] categories;
	private Object project;
	

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


	public Object getProject() {
		return project;
	}

	public void setProject(Object project) {
		this.project = project;
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
	
	
	
	
	
	public static class ProjectModel{
		private String id;
		private String name;
		
		public String getId(){
			return id;
		}
		
		public String getName(){
			return name;
		}
		
		public ProjectModel(String id, String name){
			this.id=id;
			this.name=name;
		}
		
	}
	
}

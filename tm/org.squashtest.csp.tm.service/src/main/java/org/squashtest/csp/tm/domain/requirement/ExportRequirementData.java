/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.domain.requirement;

import java.util.Date;

import org.squashtest.csp.tm.domain.audit.AuditableMixin;
import org.squashtest.csp.tm.domain.library.Folder;
import org.squashtest.csp.tm.domain.library.LibraryNode;

public class ExportRequirementData {

	private Long id;
	private String reference;
	private String folderName;
	private RequirementCriticality criticality;
	private RequirementCategory category;
	private String project;
	private String name;
	private String description;
	private Integer currentVersion;
	private RequirementStatus status;
	private Date createdOn;
    private String createdBy;
    private Long folderId;
    public static final Long NO_FOLDER = -1l;
	
	public ExportRequirementData() {
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public RequirementCriticality getCriticality() {
		return criticality;
	}

	public void setCriticality(RequirementCriticality criticality) {
		this.criticality = criticality;
	}
	
	public RequirementCategory getCategory() {
		return category;
	}

	public void setCategory(RequirementCategory category) {
		this.category = category;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFolderName() {
		return folderName;
	}
	
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	
	public Integer getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(Integer currentVersion) {
		this.currentVersion = currentVersion;
	}

	public RequirementStatus getStatus() {
		return status;
	}

	public void setStatus(RequirementStatus status) {
		this.status = status;
	}
	
	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	

	public Long getFolderId() {
		return folderId;
	}

	public void setFolderId(Long folderId) {
		this.folderId = folderId;
	}
	
	public ExportRequirementData(Requirement requirement, RequirementFolder folder) {
		this.id = requirement.getId();
		this.name = requirement.getName();
		this.criticality = requirement.getCriticality();
		this.category = requirement.getCategory();
		this.description = requirement.getDescription();
		this.project = requirement.getProject().getName();
		this.reference = requirement.getReference();
		this.currentVersion = requirement.getCurrentVersion().getVersionNumber();
		this.status = requirement.getStatus();	
		AuditableMixin audit = ((AuditableMixin) requirement);	
		this.createdOn = audit.getCreatedOn();
		this.createdBy = audit.getCreatedBy();
		//folder is null if the requirement is located directly under the project root.
		if(folder == null) {
			this.folderId = NO_FOLDER;
			this.folderName = "";
		}
		else {
			this.folderId = folder.getId();
			this.folderName = folder.getName();
		}
	}	
}

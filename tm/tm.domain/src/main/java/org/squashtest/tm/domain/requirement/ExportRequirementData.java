/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.domain.requirement;

import org.squashtest.tm.domain.library.ExportData;

/**
 * 
 * Data support for jasper Requirement Export
 * 
 */
public class ExportRequirementData extends ExportData {

	private RequirementCriticality criticality;
	private RequirementCategory category;
	private Integer currentVersion;
	private RequirementStatus status;
	private String reference = "";

	public ExportRequirementData() {
		super();
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

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		doSetReference(reference);
	}
	
	private void doSetReference(String reference){
		if(reference != null){
			this.reference = reference;
		}
	}

	public ExportRequirementData(Requirement requirement, RequirementFolder folder) {
		super(requirement, folder);
		doSetReference(requirement.getReference());
		this.criticality = requirement.getCriticality();
		this.category = requirement.getCategory();
		this.currentVersion = requirement.getCurrentVersion().getVersionNumber();
		this.status = requirement.getStatus();

	}
}

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

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Data holder for requirement version creation. We cannot use a requirement version because of its contrained
 * relationship with a requirement.
 * 
 * @author Gregory Fouquet
 * 
 */
public class NewRequirementVersionDto {
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

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	@NotBlank
	private String name;
	private String description;
	@NotNull
	private RequirementCriticality criticality = RequirementCriticality.UNDEFINED;
	@NotNull
	private RequirementCategory category = RequirementCategory.UNDEFINED;
	
	private String reference;

	public RequirementVersion toRequirementVersion() {
		RequirementVersion version = new RequirementVersion();
		version.setName(name);
		if (criticality != null) {
			version.setCriticality(criticality);
		}
		if(category != null){
			version.setCategory(category);
		}
		version.setReference(reference);
		version.setDescription(description);

		return version;
	}
}

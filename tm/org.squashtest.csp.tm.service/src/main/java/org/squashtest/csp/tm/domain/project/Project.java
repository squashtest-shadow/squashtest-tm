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
package org.squashtest.csp.tm.domain.project;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.tm.domain.audit.Auditable;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;

@Auditable
@Entity
public class Project {
	@Id
	@GeneratedValue
	@Column(name = "PROJECT_ID")
	private Long id;

	@Lob
	private String description;

	private String label;

	@NotBlank
	@Size(min = 0, max = 255)
	private String name;

	private Boolean active = Boolean.TRUE;

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "TCL_ID")
	private TestCaseLibrary testCaseLibrary;

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "RL_ID")
	private RequirementLibrary requirementLibrary;

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "CL_ID")
	private CampaignLibrary campaignLibrary;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Project() {
	}

	public Long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@NotBlank
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setActive(boolean isActive) {
		this.active = isActive;
	}

	public boolean isActive() {
		return this.active;
	}

	public TestCaseLibrary getTestCaseLibrary() {
		return testCaseLibrary;
	}

	public void setTestCaseLibrary(TestCaseLibrary testCaseLibrary) {
		this.testCaseLibrary = testCaseLibrary;
		notifyLibraryAssociation(testCaseLibrary);

	}

	public RequirementLibrary getRequirementLibrary() {
		return requirementLibrary;
	}

	public void setRequirementLibrary(RequirementLibrary requirementLibrary) {
		this.requirementLibrary = requirementLibrary;
		notifyLibraryAssociation(requirementLibrary);
	}

	public CampaignLibrary getCampaignLibrary() {
		return campaignLibrary;
	}

	public void setCampaignLibrary(CampaignLibrary campaignLibrary) {
		this.campaignLibrary = campaignLibrary;
		notifyLibraryAssociation(campaignLibrary);
	}

	/**
	 * Notifies a library it was associated with this project.
	 * 
	 * @param library
	 */
	private void notifyLibraryAssociation(GenericLibrary<?> library) {
		if (library != null) {
			library.notifyAssociatedWithProject(this);
		}
	}

}

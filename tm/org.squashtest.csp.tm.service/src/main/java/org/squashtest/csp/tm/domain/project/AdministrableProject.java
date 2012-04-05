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

import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;

public class AdministrableProject {
	private final Project project;
	private boolean deletable = false;

	public boolean isDeletable() {
		return deletable;
	}

	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}

	public String getLabel() {
		return project.getLabel();
	}

	public Long getId() {
		return project.getId();
	}

	public String getDescription() {
		return project.getDescription();
	}

	public String getName() {
		return project.getName();
	}

	public boolean isActive() {
		return project.isActive();
	}

	public TestCaseLibrary getTestCaseLibrary() {
		return project.getTestCaseLibrary();
	}

	public RequirementLibrary getRequirementLibrary() {
		return project.getRequirementLibrary();
	}

	public CampaignLibrary getCampaignLibrary() {
		return project.getCampaignLibrary();
	}

	public Project getProject() {
		return project;
	}

	public AdministrableProject(Project project) {
		this.project = project;
	}
}

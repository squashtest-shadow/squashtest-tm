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
package org.squashtest.tm.service.requirement;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementCategory;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;

/**
 * Partial view of a {@link RequirementVersionCoverage} verified by some test case.
 *
 * @author Gregory Fouquet, mpagnon
 *
 */
public class VerifiedRequirement {
	private final RequirementVersionCoverage requirementVersionCoverage;
	/**
	 * In the context of a given test case, the test case directly verifies this requirement (ie not through a test case
	 * call).
	 */
	private final boolean directVerification;

	public VerifiedRequirement(@NotNull RequirementVersionCoverage requirementVersionCoverage, boolean directVerification) {
		super();
		this.requirementVersionCoverage = requirementVersionCoverage;
		this.directVerification = directVerification;
	}
	public VerifiedRequirement(@NotNull RequirementVersion version, boolean directlyVerified) {
		super();
		this.requirementVersionCoverage = new RequirementVersionCoverage(version);
		this.directVerification = directlyVerified;
	}
	private RequirementVersion getVerifiedRequirementVersion(){
		return this.requirementVersionCoverage.getVerifiedRequirementVersion();
	}
	public Project getProject() {
		return getVerifiedRequirementVersion().getRequirement().getProject();
	}
	public RequirementStatus getStatus(){
		return getVerifiedRequirementVersion().getStatus();
	}
	public String getName() {
		return getVerifiedRequirementVersion().getName();
	}
	
	public int getVersionNumber(){
		return getVerifiedRequirementVersion().getVersionNumber();
	}

	public String getDescription() {
		return getVerifiedRequirementVersion().getDescription();
	}

	public String getReference() {
		return getVerifiedRequirementVersion().getReference();
	}

	public RequirementCriticality getCriticality() {
		return getVerifiedRequirementVersion().getCriticality();
	}
	
	public RequirementCategory getCategory() {
		return getVerifiedRequirementVersion().getCategory();
	}

	public boolean isDirectVerification() {
		return directVerification;
	}

	public Long getId() {
		return getVerifiedRequirementVersion().getId();
	}
	public List<ActionTestStep> getVerifyingSteps(){
		return requirementVersionCoverage.getVerifyingSteps();
	}
	public boolean hasStepAsVerifying(long stepId) {
		return requirementVersionCoverage.hasStepAsVerifying(stepId);
	}
	
	
}

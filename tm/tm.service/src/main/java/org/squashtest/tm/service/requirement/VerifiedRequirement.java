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

import javax.validation.constraints.NotNull;

import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCategory;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementVersion;

/**
 * Partial view of a {@link Requirement} verified by some test case.
 *
 * @author Gregory Fouquet
 *
 */
public class VerifiedRequirement {
	private final RequirementVersion decoratedRequirement;
	/**
	 * In the context of a given test case, the test case directly verifies this requirement (ie not through a test case
	 * call).
	 */
	private final boolean directVerification;

	public VerifiedRequirement(@NotNull RequirementVersion decoratedRequirement, boolean directVerification) {
		super();
		this.decoratedRequirement = decoratedRequirement;
		this.directVerification = directVerification;
	}

	public Project getProject() {
		return decoratedRequirement.getRequirement().getProject();
	}

	public String getName() {
		return decoratedRequirement.getName();
	}

	public String getDescription() {
		return decoratedRequirement.getDescription();
	}

	public String getReference() {
		return decoratedRequirement.getReference();
	}

	public RequirementCriticality getCriticality() {
		return decoratedRequirement.getCriticality();
	}
	
	public RequirementCategory getCategory() {
		return decoratedRequirement.getCategory();
	}

	public boolean isDirectVerification() {
		return directVerification;
	}

	public Long getId() {
		return decoratedRequirement.getId();
	}
	
	public RequirementVersion getDecoratedRequirement() {
		return decoratedRequirement;
	}
}

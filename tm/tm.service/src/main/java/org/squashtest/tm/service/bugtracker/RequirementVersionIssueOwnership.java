/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.bugtracker;

import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.requirement.RequirementVersion;

public class RequirementVersionIssueOwnership<ISSUE> extends IssueOwnership<ISSUE> {
	private RequirementVersion requirementVersion;
	private String nodePosition;

	public RequirementVersionIssueOwnership(ISSUE issue, IssueDetector owner, RequirementVersion requirementVersion, String nodePosition) {
		super(issue, owner);
		this.requirementVersion = requirementVersion;
		this.nodePosition = nodePosition;
	}

	public RequirementVersion getRequirementVersion() {
		return requirementVersion;
	}

	public void setRequirementVersion(RequirementVersion requirementVersion) {
		this.requirementVersion = requirementVersion;
	}

	public String getNodePosition() {
		return nodePosition;
	}

	public void setNodePosition(String nodePosition) {
		this.nodePosition = nodePosition;
	}
}
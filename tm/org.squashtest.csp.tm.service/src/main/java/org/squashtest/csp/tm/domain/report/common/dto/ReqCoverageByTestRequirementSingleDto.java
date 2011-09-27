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
package org.squashtest.csp.tm.domain.report.common.dto;

import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;

public class ReqCoverageByTestRequirementSingleDto {

	/***
	 * The name of the folder which contains the requirement. Default value is " - "
	 */
	private String folder = " - ";

	/***
	 * The requirement reference and name
	 */
	private String reference, label;

	/***
	 * Requirement criticality
	 */
	private RequirementCriticality criticality;

	/***
	 * Total number of Test case which verify this requirement
	 */
	private int associatedTestCaseNumber = 0;

	// ACCESSORS

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public RequirementCriticality getCriticality() {
		return criticality;
	}

	public void setCriticality(RequirementCriticality criticality) {
		this.criticality = criticality;
	}

	public int getAssociatedTestCaseNumber() {
		return associatedTestCaseNumber;
	}

	public void setAssociatedTestCaseNumber(int associatedTestCaseNumber) {
		this.associatedTestCaseNumber = associatedTestCaseNumber;
	}

}

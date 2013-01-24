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
package org.squashtest.tm.service.internal.importer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.requirement.RequirementFolder;

/**
 * 
 * @author mpagnon
 *
 */
/* package-private */class PseudoRequirement {
	private RequirementFolder folder = null;
	private Double id = null;
	private List<PseudoRequirementVersion> pseudoRequirementVersions; 

	private static final Logger LOGGER = LoggerFactory.getLogger(PseudoRequirement.class);
	
	public PseudoRequirement(String label2, int rowNumber) {
		PseudoRequirementVersion pseudoRequirementVersion = new PseudoRequirementVersion(label2, rowNumber, this);
		pseudoRequirementVersions = new ArrayList<PseudoRequirementVersion>();
		pseudoRequirementVersions.add(pseudoRequirementVersion);
	}
	
	public void addVersion(PseudoRequirement newReq) {
		List<PseudoRequirementVersion> newReqVersions = newReq.getPseudoRequirementVersions();
		PseudoRequirementVersion newReqVersion = newReqVersions.get(0);
		newReqVersion.setPseudoRequirement(this);
		this.pseudoRequirementVersions.add(newReqVersion);
	}

	/* ***************************** getter and setters *********************************** */

	public RequirementFolder getFolder() {
		return folder;
	}
	
	public void setFolder(RequirementFolder folder) {
		this.folder = folder;
	}

	public Double getId() {
		return id;
	}

	public void setId(Double id) {
		this.id = id;
	}

	public List<PseudoRequirementVersion> getPseudoRequirementVersions() {
		return pseudoRequirementVersions;
	}

	public void setPseudoRequirementVersions(List<PseudoRequirementVersion> pseudoRequirementVersions) {
		this.pseudoRequirementVersions = pseudoRequirementVersions;
	}

	


}

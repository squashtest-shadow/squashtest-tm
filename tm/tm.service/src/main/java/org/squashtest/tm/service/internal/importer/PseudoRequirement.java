/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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

import org.squashtest.tm.domain.requirement.RequirementFolder;

/**
 * Holds :
 * <ul>
 * <li>
 * it's folder owner (or null)</li>
 * <li>the id information (used to group the requirement version together in the excel file)</li>
 * <li>a list of it's versions as {@link PseudoRequirementVersion}</li>
 * </ul>
 * 
 * @author mpagnon
 * 
 */
/* package-private */class PseudoRequirement {
	private RequirementFolder folder = null;
	private Double id = null;
	private String reqPath = "";
	private List<PseudoRequirementVersion> pseudoRequirementVersions;

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

	public void setReqPath(String reqPath) {
		if (reqPath != null) {
			this.reqPath = reqPath;
		}
	}

	public String getReqPath() {
		return reqPath;
	}

	public boolean hasSameIdAndReqPathThan(PseudoRequirement pseudoRequirement) {
		return hasSameIdThan(pseudoRequirement) && hasSameReqPathThan(pseudoRequirement);
	}

	private boolean hasSameReqPathThan(PseudoRequirement pseudoRequirement) {
		return this.reqPath.compareTo(pseudoRequirement.getReqPath()) == 0;
	}

	public boolean hasSameIdThan(PseudoRequirement pseudoRequirement) {
		if (this.getId() == null || pseudoRequirement.getId() == null) {
			return false;
		} else {
			return this.getId().compareTo(pseudoRequirement.getId()) == 0;
		}
	}

}

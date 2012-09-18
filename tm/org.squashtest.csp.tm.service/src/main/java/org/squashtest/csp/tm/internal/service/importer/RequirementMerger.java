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

package org.squashtest.csp.tm.internal.service.importer;

import java.util.Collections;
import java.util.List;

import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.internal.service.importer.RequirementLibraryMerger.DestinationManager;

class RequirementMerger extends DestinationManager {

	public void merge(List<PseudoRequirement> pseudoRequirements, RequirementLibrary library) {
		setDestination(library);
		merge(pseudoRequirements);
	}

	public void merge(List<PseudoRequirement> pseudoRequirements, RequirementFolder folder) {
		setDestination(folder);
		merge(pseudoRequirements);
	}

	public void merge(List<PseudoRequirement> pseudoRequirements) {
		for (PseudoRequirement pseudoRequirement : pseudoRequirements) {
			List<PseudoRequirementVersion> pseudoRequirementVersions = pseudoRequirement
					.getPseudoRequirementVersions();
			if (pseudoRequirementVersions.size() > 1) {
				Collections.sort(pseudoRequirementVersions);
				PseudoRequirementVersion pseudoRequirementVersion = pseudoRequirementVersions.get(0);
				Requirement requirement = addRequirement(pseudoRequirementVersion);
				for (int i = 1; i < pseudoRequirementVersions.size(); i++) {
					RequirementVersion newVersion = createVersion(pseudoRequirementVersions.get(i));
					addVersion(requirement, newVersion);
				}

			} else {
				PseudoRequirementVersion pseudoRequirementVersion = pseudoRequirement
						.getPseudoRequirementVersions().get(0);
				addRequirement(pseudoRequirementVersion);
			}
		}
	}

	private Requirement addRequirement(PseudoRequirementVersion pseudoRequirementVersion) {
		RequirementVersion firstVersion = createVersion(pseudoRequirementVersion);
		Requirement requirement = new Requirement(firstVersion);
		persistRequirement(requirement);
		return requirement;
	}

	private RequirementVersion createVersion(PseudoRequirementVersion pseudoRequirementVersion) {
		RequirementVersion req = new RequirementVersion(pseudoRequirementVersion.getCreatedOnDate(),
				pseudoRequirementVersion.getCreatedBy());
		req.setCriticality(pseudoRequirementVersion.getCriticality());
		req.setCategory(pseudoRequirementVersion.getCategory());
		req.setDescription(pseudoRequirementVersion.getDescription());
		req.setName(pseudoRequirementVersion.getLabel());
		req.setReference(pseudoRequirementVersion.getReference());
		// STATUS ??

		return req;
	}

}
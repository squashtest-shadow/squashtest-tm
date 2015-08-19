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

import java.util.Collections;
import java.util.List;

import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.internal.importer.RequirementLibraryMerger.DestinationManager;

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
			//order version and rename last one
			List<PseudoRequirementVersion> pseudoRequirementVersions = pseudoRequirement.getPseudoRequirementVersions();
			Collections.sort(pseudoRequirementVersions);
			renameLastVersion(pseudoRequirementVersions);
			//create requirement with first version
			PseudoRequirementVersion pseudoRequirementVersion = pseudoRequirementVersions.get(0);
			RequirementVersion firstVersion = RequirementVersion.createFromMemento(pseudoRequirementVersion);
			Requirement requirement = new Requirement(firstVersion);
			//add remaining versions
			for (int i = 1; i < pseudoRequirementVersions.size(); i++) {
				addVersion(requirement, pseudoRequirementVersions.get(i));
			}
			//persist
			persistRequirement(requirement);
		}
	}
	
	private void addVersion(Requirement requirement, PseudoRequirementVersion pseudoVersion) {
		requirement.increaseVersion(RequirementVersion.createFromMemento(pseudoVersion));
	}


	

}
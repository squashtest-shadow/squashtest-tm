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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
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

	private static final class PseudoRequirementPathComparator implements Comparator<PseudoRequirement> {
		@Override
		public int compare(PseudoRequirement o1, PseudoRequirement o2) {
			return o1.getReqPath().compareTo(o2.getReqPath());
		}

	}

	public void merge(List<PseudoRequirement> pseudoRequirements) {
		// sort requirements to persist root requirements first
		Collections.sort(pseudoRequirements, new PseudoRequirementPathComparator());
		// remember renamed requirements to import their requirement children in the renamed entity
		Map<Identified, Map<String, Long>> renamedRequirements = new HashMap<Identified, Map<String, Long>>();
		// process requirements one after the other

		for (PseudoRequirement pseudoRequirement : pseudoRequirements) {
			// merge requirement hierarchy
			String reqPath = pseudoRequirement.getReqPath();
			if (!reqPath.isEmpty()) {
				mergeRequirementHierarchy(renamedRequirements, reqPath);
			}

			// order version and rename last one
			List<PseudoRequirementVersion> pseudoRequirementVersions = pseudoRequirement.getPseudoRequirementVersions();
			Collections.sort(pseudoRequirementVersions);
			PseudoRequirementVersion lastVersion = pseudoRequirementVersions.get(pseudoRequirementVersions.size() - 1);
			String originalName = lastVersion.getName();
			boolean renamed = renameLastVersion(lastVersion);

			// create requirement with first version
			PseudoRequirementVersion pseudoRequirementVersion = pseudoRequirementVersions.get(0);
			RequirementVersion firstVersion = RequirementVersion.createFromMemento(pseudoRequirementVersion);
			Requirement requirement = new Requirement(firstVersion);
			// add remaining versions
			for (int i = 1; i < pseudoRequirementVersions.size(); i++) {
				addVersion(requirement, pseudoRequirementVersions.get(i));
			}

			persistRequirement(requirement);

			if (renamed) {
				NodeContainer<? extends RequirementLibraryNode> destination = getDestination();
				Map<String, Long> renamedIdByOriginalName = renamedRequirements.get(destination);
				if (renamedIdByOriginalName == null) {
					renamedIdByOriginalName = new HashMap<String, Long>();
				}
				renamedIdByOriginalName.put(originalName, requirement.getId());
				renamedRequirements.put(destination, renamedIdByOriginalName);
			}
			setRequirementDestination(null);
		}

	}

	public void mergeRequirementHierarchy(Map<Identified, Map<String, Long>> renamedRequirements, String reqPath) {
		List<String> requirementParentNames = UrlParser.extractFoldersNames(reqPath);
		for (String requirementParentName : requirementParentNames) {
			// find if node has been renamed
			Long renamedId = renamed(requirementParentName, renamedRequirements);
			Requirement newReqDestination = null;
			if (renamedId != null) {
				newReqDestination = findContentRequirementOfId(renamedId);
			} else {
				// find parent in destination matching path name
				newReqDestination = findContentRequirementOfName(requirementParentName);
				if (newReqDestination == null) {
					// create dummy requirement to recreate hierarchy
					RequirementVersion pathFillingVersion = new RequirementVersion();
					pathFillingVersion.setName(requirementParentName);
					newReqDestination = new Requirement(pathFillingVersion);
					AuditableMixin audit = ((AuditableMixin) newReqDestination);
					audit.setCreatedBy(RequirementImporter.DEFAULT_CREATED_BY);
					audit.setCreatedOn(new Date());
					persistRequirement(newReqDestination);
				}
			}
			setRequirementDestination(newReqDestination);
		}
	}

	private Long renamed(String requirementParentName, Map<Identified, Map<String, Long>> renamedRequirements) {
		Map<String, Long> renamed = renamedRequirements.get(getDestination());
		if (renamed != null) {
			return renamed.get(requirementParentName);
		} else {
			return null;
		}

	}

	private void addVersion(Requirement requirement, PseudoRequirementVersion pseudoVersion) {
		requirement.increaseVersion(RequirementVersion.createFromMemento(pseudoVersion));
	}

}
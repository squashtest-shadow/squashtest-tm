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
package org.squashtest.tm.service.internal.repository;

import java.util.List;
import java.util.Set;

import org.squashtest.tm.domain.requirement.ExportRequirementData;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.tm.domain.requirement.RequirementVersion;

public interface RequirementDao extends EntityDao<Requirement> {

	@SuppressWarnings("rawtypes")
	List<RequirementLibraryNode> findAllBySearchCriteria(RequirementSearchCriteria criteria);

	@SuppressWarnings("rawtypes")
	List<RequirementLibraryNode> findAllBySearchCriteriaOrderByProject(RequirementSearchCriteria criteria);
	
	List<Requirement> findChildrenRequirements(long requirementId);

	List<ExportRequirementData> findRequirementToExportFromNodes(List<Long> folderIds);

	List<ExportRequirementData> findRequirementToExportFromLibrary(List<Long> projectIds);

	/**
	 * Returns collection of distinct requirements criticalities verified by any of the given verifiers.
	 * 
	 * @param verifiersIds
	 * @return unsorted collection of distinct requirements criticalities verified by any of the given verifiers.
	 */
	List<RequirementCriticality> findDistinctRequirementsCriticalitiesVerifiedByTestCases(Set<Long> testCasesIds);

	/**
	 * returns the list of distinct requirementCriticalities found for all requirementsIds
	 * 
	 * @param requirementVersionsIds
	 * @return
	 */
	List<RequirementCriticality> findDistinctRequirementsCriticalities(List<Long> requirementVersionsIds);

	/**
	 * returns the list of all requirement versions
	 * 
	 * @param requirementId
	 * @return
	 */
	List<RequirementVersion> findVersions(Long requirementId);

	/**
	 * returns the list of all requirement versions for all the specified requirements
	 * 
	 * @param requirementIds the lists of requirement ids
	 * @return
	 */
	List<RequirementVersion> findVersionsForAll(List<Long> requirementIds);
	
	/**
	 * Will find all Requirements ids contained in library (not only root ones)
	 * @param libraryId
	 * @return
	 */
	List<Long> findAllRequirementsIdsByLibrary(long libraryId);
	
	/**
	 * returns a requirement that contains the given child requirement
	 */
	Requirement findByContent(Requirement childRequirement);
	
	
	/**
	 * Given a list of requirementIds, returns the corresponding list of [parent, requirement].
	 * the 'parent' is of type NodeContainer&lt;Requirement&gt;.
	 * 
	 * @return what I just said.
	 * 
	 */
	List<Object[]> findAllParentsOf(List<Long> requirementIds);
}

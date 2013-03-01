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
	/**
	 * returns all the requirements matching the given ids, ordered by name
	 * 
	 * @param requirementsIds
	 * @return
	 * @deprecated not used
	 */
	@Deprecated
	List<Requirement> findAllByIdListOrderedByName(List<Long> requirementsIds);

	/**
	 * return all the test case directly verifying the requirement <br>
	 * 
	 * @param requirementId
	 * @return list of directly associated test-cases
	 */
	List<String> findNamesInFolderStartingWith(long folderId, String nameStart);
	
	List<String> findNamesInLibraryStartingWith(long libraryId, String nameStart);

	@SuppressWarnings("rawtypes")
	List<RequirementLibraryNode> findAllBySearchCriteria(RequirementSearchCriteria criteria);

	@SuppressWarnings("rawtypes")
	List<RequirementLibraryNode> findAllBySearchCriteriaOrderByProject(RequirementSearchCriteria criteria);

	List<ExportRequirementData> findRequirementToExportFromNodes(List<Long> folderIds);

	List<ExportRequirementData> findRequirementToExportFromProject(List<Long> projectIds);

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
	
	 	
}

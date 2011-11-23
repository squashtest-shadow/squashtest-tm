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

package org.squashtest.csp.tm.domain.event;

import java.util.Collection;
import java.util.List;

import org.squashtest.csp.tm.domain.requirement.ExportRequirementData;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.internal.repository.RequirementDao;

/**
 * @author Gregory Fouquet
 *
 */
public class StubRequirementDao implements RequirementDao {

	/**
	 * @see org.squashtest.csp.tm.internal.repository.EntityDao#findById(long)
	 */
	@Override
	public Requirement findById(long id) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.EntityDao#findAllById(java.util.List)
	 */
	@Override
	public List<Requirement> findAllById(List<Long> id) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.EntityDao#persist(java.lang.Object)
	 */
	@Override
	public void persist(Requirement transientEntity) {
		// NOOP

	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.EntityDao#remove(java.lang.Object)
	 */
	@Override
	public void remove(Requirement entity) {
		// NOOP

	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.EntityDao#flush()
	 */
	@Override
	public void flush() {
		// NOOP
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findAllByIdList(java.util.List)
	 */
	@Override
	public List<Requirement> findAllByIdList(List<Long> requirementsIds) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findAllVerifyingTestCasesById(long)
	 */
	@Override
	public List<TestCase> findAllVerifyingTestCasesById(long requirementId) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findAllVerifyingTestCasesByIdFiltered(long, org.squashtest.csp.tm.infrastructure.filter.CollectionSorting)
	 */
	@Override
	public List<TestCase> findAllVerifyingTestCasesByIdFiltered(long requirementId, CollectionSorting filter) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#countVerifyingTestCasesById(long)
	 */
	@Override
	public long countVerifyingTestCasesById(long requirementId) {
		return 0;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findNamesInFolderStartingWith(long, java.lang.String)
	 */
	@Override
	public List<String> findNamesInFolderStartingWith(long folderId, String nameStart) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findNamesInLibraryStartingWith(long, java.lang.String)
	 */
	@Override
	public List<String> findNamesInLibraryStartingWith(long libraryId, String nameStart) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findAllBySearchCriteria(org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria)
	 */
	@Override
	public List<RequirementLibraryNode> findAllBySearchCriteria(RequirementSearchCriteria criteria) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findAllBySearchCriteriaOrderByProject(org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria)
	 */
	@Override
	public List<RequirementLibraryNode> findAllBySearchCriteriaOrderByProject(RequirementSearchCriteria criteria) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findRequirementToExportFromFolder(java.util.List)
	 */
	@Override
	public List<ExportRequirementData> findRequirementToExportFromFolder(List<Long> folderIds) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findRequirementToExportFromLibrary(java.util.List)
	 */
	@Override
	public List<ExportRequirementData> findRequirementToExportFromLibrary(List<Long> folderIds) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findAllRequirementsVerifiedByTestCases(java.util.Collection, org.squashtest.csp.tm.infrastructure.filter.CollectionSorting)
	 */
	@Override
	public List<Requirement> findAllRequirementsVerifiedByTestCases(Collection<Long> verifiersIds,
			CollectionSorting sorting) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#countRequirementsVerifiedByTestCases(java.util.Collection)
	 */
	@Override
	public long countRequirementsVerifiedByTestCases(Collection<Long> verifiersIds) {
		return 0;
	}

}

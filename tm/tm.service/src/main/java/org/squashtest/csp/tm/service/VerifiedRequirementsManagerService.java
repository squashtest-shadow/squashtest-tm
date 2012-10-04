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
package org.squashtest.csp.tm.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.VerifiedRequirementException;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;

/**
 * Service for management of Requirements verified by a {@link TestCase}
 * 
 * @author Gregory Fouquet
 * 
 */
public interface VerifiedRequirementsManagerService {
	/**
	 * Adds a list of requirements to the ones verified by a test case. If a requirement is already verified, nothing
	 * special happens.
	 * 
	 * @param requirementsIds
	 * @param testCaseId
	 * @return
	 */
	Collection<VerifiedRequirementException> addVerifiedRequirementsToTestCase(List<Long> requirementsIds,
			long testCaseId);
	
	/**
	 * Adds a list of requirement-versions to the ones verified by a test case. If the version or a sister is already verified, nothing
	 * special happens.
	 * 
	 * @param requirementVersionsByTestCase : list of requirementVersions mapped by test-case
	 * @return 
	 */
	Collection<VerifiedRequirementException> addVerifyingRequirementVersionsToTestCase(Map<TestCase, List<RequirementVersion>> requirementVersionsByTestCase);

	/**
	 * Removes a list of requirements from the ones verified by a test case. If a requirement is not verified by the
	 * test case, nothing special happens.
	 * 
	 * @param testCaseId
	 * @param requirementVersionsIds
	 */
	void removeVerifiedRequirementVersionsFromTestCase(List<Long> requirementVersionsIds, long testCaseId);

	/**
	 * Removes a requirement from the ones verified by a test case. If the requirement was not previously verified by
	 * the test case, nothing special happens.
	 * 
	 * @param testCaseId
	 * @param requirementsIds
	 */
	void removeVerifiedRequirementVersionFromTestCase(long requirementVersionId, long testCaseId);
	
	/**
	 * Removes a requirement version from the given test case and replaces it with the wanted version
	 * if the same version is selected as was previously attached, nothing happens
	 * 
	 * @param testCaseId
	 * @param oldVerifiedRequirementVersionId
	 * @param newVerifiedRequirementVersionId
	 */
	int changeVerifiedRequirementVersionOnTestCase(long oldVerifiedRequirementVersionId, long newVerifiedRequirementVersionId, long testCaseId);

	/**
	 * Returns the filtered list of {@link RequirementVersion}s directly verified by a test case.
	 * 
	 * @param testCaseId
	 * @param filter
	 * @return
	 */
	@Transactional(readOnly = true)
	PagedCollectionHolder<List<RequirementVersion>> findAllDirectlyVerifiedRequirementsByTestCaseId(long testCaseId,
			PagingAndSorting pas);

	
}

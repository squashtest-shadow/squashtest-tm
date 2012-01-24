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
package org.squashtest.csp.tm.service;

import java.util.Collection;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;
import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.tm.domain.RequirementAlreadyVerifiedException;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;

/**
 * Service for management of Requirements verified by a {@link TestCase}
 * 
 * @author Gregory Fouquet
 * 
 */
public interface VerifyingTestCaseManagerService {

	/**
	 * Returns the collection of {@link RequirementLibrary}s which Requirements can be linked by a {@link TestCase}
	 * 
	 * @return
	 */
	List<TestCaseLibrary> findLinkableTestCaseLibraries();

	/**
	 * Adds a list of requirements to the ones verified by a test case. If a requirement is already verified, nothing
	 * special happens.
	 * 
	 * @param requirementsIds
	 * @param testCaseId
	 * @return test cases which could not be added.
	 */
	Collection<RequirementAlreadyVerifiedException> addVerifyingTestCasesToRequirementVersion(List<Long> testCaseIds,
			long requirementVersionId);

	/**
	 * Removes a list of requirements from the ones verified by a test case. If a requirement is not verified by the
	 * test case, nothing special happens.
	 * 
	 * @param testCaseId
	 * @param requirementsIds
	 */
	void removeVerifyingTestCasesFromRequirementVersion(List<Long> testCaseIds, long requirementVersionId);

	/**
	 * Removes a requirement from the ones verified by a test case. If the requirement was not previously verified by
	 * the test case, nothing special happens.
	 * 
	 * @param testCaseId
	 * @param requirementsIds
	 */
	void removeVerifyingTestCaseFromRequirementVersion(long testCaseId, long requirementVersionId);

	/**
	 * @param requirementId
	 * @param pagingAndSorting
	 * @return
	 */
	@Transactional(readOnly = true)
	PagedCollectionHolder<List<TestCase>> findAllByRequirementVersion(long requirementId,
			PagingAndSorting pagingAndSorting);

}

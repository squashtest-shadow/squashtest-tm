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
package org.squashtest.tm.service.testcase;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.foundation.collection.CollectionSorting;
import org.squashtest.tm.service.foundation.collection.FilteredCollectionHolder;
import org.squashtest.tm.service.requirement.VerifiedRequirement;

/**
 * @author Gregory Fouquet
 * 
 */
public interface TestCaseFinder {
	@Transactional(readOnly = true)
	TestCase findById(long testCaseId);

	@Transactional(readOnly = true)
	TestCase findTestCaseWithSteps(long testCaseId);

	@Transactional(readOnly = true)
	List<TestStep> findStepsByTestCaseId(long testCaseId);

	@Transactional(readOnly = true)
	FilteredCollectionHolder<List<TestStep>> findStepsByTestCaseIdFiltered(long testCaseId, Paging filter);

	
	/**
	 * @param testCaseId
	 * @param pas
	 * @return
	 */
	@Deprecated
	@Transactional(readOnly = true)
	PagedCollectionHolder<List<VerifiedRequirement>> findAllVerifiedRequirementsByTestCaseId(long testCaseId,
			PagingAndSorting pas);

	/**
	 * That method returns the list of test cases having at least one CallTestStep directly calling the test case
	 * identified by testCaseId. The list is wrapped in a FilteredCollectionHolder, that contains meta informations
	 * regarding the filtering, as usual.
	 * 
	 * @param testCaseId
	 *            the Id of the called test case.
	 * @param sorting
	 *            the sorting parameters.
	 * @return a non null but possibly empty FilteredCollectionHolder wrapping the list of first-level calling test
	 *         cases.
	 */
	@Transactional(readOnly = true)
	FilteredCollectionHolder<List<TestCase>> findCallingTestCases(long testCaseId, CollectionSorting sorting);

	
}
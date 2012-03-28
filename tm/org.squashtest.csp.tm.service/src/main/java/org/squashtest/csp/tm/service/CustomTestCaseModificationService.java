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

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;
import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionFilter;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;

/**
 * @author Gregory Fouquet
 * 
 */
@Transactional
public interface CustomTestCaseModificationService {
	@Transactional(readOnly = true)
	TestCase findById(long testCaseId);

	@Transactional(readOnly = true)
	TestCase findTestCaseWithSteps(long testCaseId);

	@Transactional(readOnly = true)
	List<TestStep> findStepsByTestCaseId(long testCaseId);

	@Transactional(readOnly = true)
	FilteredCollectionHolder<List<TestStep>> findStepsByTestCaseIdFiltered(long testCaseId, CollectionFilter filter);

	void rename(long testCaseId, String newName);

	TestStep addActionTestStep(long parentTestCaseId, ActionTestStep newTestStep);

	void updateTestStepAction(long testStepId, String newAction);

	void updateTestStepExpectedResult(long testStepId, String newExpectedResult);

	@Deprecated
	void changeTestStepPosition(long testCaseId, long testStepId, int newStepPosition);

	/**
	 * Will move a list of steps to a new position.
	 * 
	 * @param testCaseId
	 *            the id of the test case
	 * @param newPosition
	 *            the position we want the first element of movedSteps to be once the operation is complete
	 * @param movedSteps
	 *            the list of steps to move, sorted by rank among each others.
	 */
	void changeTestStepsPosition(long testCaseId, int newPosition, List<Long> stepIds);

	void removeStepFromTestCase(long testCaseId, long testStepId);

	void removeListOfSteps(long testCaseId, List<Long> testStepIds);

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

	/**
	 * will insert a test step into a test case script, possibly after a step (the position), given their Ids.
	 * 
	 * @param testCaseId
	 *            the id of the test case.
	 * @param idToCopyAfter
	 *            the id of the step after which we'll insert the copy of a step, may be null.
	 * @param copiedTestStepId
	 *            the id of the testStep to copy.
	 * 
	 */
	void pasteCopiedTestStep(long testCaseId, long idToCopyAfter, long copiedTestStepId);

	/**
	 * will insert a test step into a test case script at the last position
	 * 
	 * @param testCaseId
	 *            the id of the test case.
	 * @param copiedTestStepId
	 *            the id of the testStep to copy.
	 * 
	 */
	void pasteCopiedTestStepToLastIndex(long testCaseId, long copiedTestStepId);
	
	/**
	 * will change the test case importance too if auto is true.
	 * 
	 * @param testCaseId
	 * @param auto
	 */
	void changeImportanceAuto(long testCaseId, boolean auto);
}
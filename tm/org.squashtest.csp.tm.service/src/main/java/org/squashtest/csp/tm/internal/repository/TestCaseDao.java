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
package org.squashtest.csp.tm.internal.repository;

import java.util.Collection;
import java.util.List;

import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionFilter;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;

public interface TestCaseDao extends EntityDao<TestCase> {

	TestCase findAndInit(Long testCaseId);

	List<ActionTestStep> getTestCaseSteps(Long testCaseId);

	/**
	 * Returns the initialized list of requirements verified by a test case. The list is filtered accorfing to the
	 * {@link CollectionSorting} object.
	 *
	 * @param testCaseId
	 * @param filter
	 * @return
	 */
	List<Requirement> findAllDirectlyVerifiedRequirementsByIdFiltered(long testCaseId, CollectionSorting filter);

	/**
	 * Returns the number of Requirements verified by a test case.
	 *
	 * @param testCaseId
	 * @return
	 */
	long countVerifiedRequirementsById(long testCaseId);

	List<TestCase> findAllByIdList(final List<Long> testCaseIds);

	List<TestCase> findAllByIdListNonOrdered(final List<Long> testCaseIds);

	List<String> findNamesInFolderStartingWith(long folderId, String nameStart);

	List<String> findNamesInLibraryStartingWith(long libraryId, String nameStart);

	TestCase findByIdWithInitializedSteps(long testCaseId);

	List<TestStep> findAllStepsByIdFiltered(long testCaseId, CollectionFilter filter);

	/**
	 * Finds all {@link TestCaseLibraryNode} which name contains the given token.
	 *
	 * @param tokenInName
	 * @param groupByProject
	 * @return
	 */
	List<TestCaseLibraryNode> findAllByNameContaining(String tokenInName, boolean groupByProject);

	/***
	 * This method returns the test step's associated TestCase
	 *
	 * @param testStepId
	 *            the test step id
	 * @return the associated test Case
	 */
	TestCase findTestCaseByTestStepId(long testStepId);

	/**
	 * Counts the calling test steps of a test case
	 *
	 * @param testCaseId
	 * @return
	 */
	long countCallingTestSteps(long testCaseId);


	/**
	 * Given a list of test case ids, returns a sublist of the test case ids. An id will be included in the output
	 * list if at least one test case calls the given test case.
	 *
	 * @param  testCaseIds the list of test case ids under inquiry.
	 * @return a sublist of the input list, with test cases never called filtered out.
	 */
	List<Long> findTestCasesHavingCaller(Collection<Long> testCasesIds);
	
	
	/**
	 * Given a list of test case ids, returns a list of the following structure :
	 * 
	 *  	- caller id (null if no match),
	 *  	- caller name (null if no match), 
	 *  	- called id,
	 *  	- called name
	 * 
	 * 
	 * 
	 * Note that only first-level callers will be included if found, additional invokations will be needed to fetch all
	 * the hierarchy.
	 * 
	 * @param  testCaseIds the list of test case ids under inquiry.
	 * @return a structure described just like above. 
	 */
	List<Object[]> findTestCasesHavingCallerDetails(Collection<Long> testCaseIds);

	/**
	 * Finds all the ids of the test cases called by a given test case.
	 *
	 * @param testCaseId
	 * @return
	 */
	List<Long> findAllTestCasesIdsCalledByTestCase(long testCaseId);

	/**
	 * Finds all the ids of the test cases called by a given list of test cases.
	 *
	 * @param testCaseId
	 * @return
	 */
	List<Long> findAllTestCasesIdsCalledByTestCases(List<Long> testCasesIds);


	/**
	 * returns the ids of all the test cases having at least one call test step referencing the given test case.
	 *
	 * @param testCaseId the id of the test case.
	 * @param sorting the sorting attributes and the like.
	 * @return the list of test cases having at least one call step calling the input test case.
	 */
	List<TestCase> findAllCallingTestCases(long testCaseId, CollectionSorting sorting);

	/***
	 * Returns the test cases associated with at least a requirement that meets the criteria
	 *
	 * @param criteria
	 *            the requirement search criteria
	 * @param isProjectOrdered
	 *            if set to true, the list of test case is ordered by project
	 * @return the list of test case (order by project if specified)
	 */
	List<TestCase> findAllByRequirement(RequirementSearchCriteria criteria, boolean isProjectOrdered);

}

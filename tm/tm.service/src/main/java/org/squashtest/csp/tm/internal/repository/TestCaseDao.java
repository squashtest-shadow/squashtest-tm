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
package org.squashtest.csp.tm.internal.repository;

import java.util.Collection;
import java.util.List;

import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.library.Library;
import org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.ExportTestCaseData;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestCaseSearchCriteria;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.service.CallStepManagerService;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;

public interface TestCaseDao extends EntityDao<TestCase> {

	/**
	 * That method exists because hibernate doesn't quite respect the 
	 * cascade persist for a set of test case and steps 
	 * 
	 * @param testCase
	 */
	void persistTestCaseAndSteps(TestCase testCase);
	
	/**
	 * if the transient test case has steps, will invoke {@link #persistTestCaseAndSteps(TestCase)}
	 * else, will just save it.
	 * 
	 */
	public void safePersist(TestCase testCase);
	
	TestCase findAndInit(Long testCaseId);

	List<ActionTestStep> getTestCaseSteps(Long testCaseId);

	List<TestCase> findAllByIdListOrderedByName(final List<Long> testCaseIds);
	
	/**
	 * Will find all names of folderes content starting with the input string.
	 * Will not return sub-folder's content names.
	 * 
	 * @param folderId the id of a {@link TestCaseFolder}
	 * @param nameStart the search param
	 * @return
	 */
	List<String> findNamesInFolderStartingWith(long folderId, String nameStart);
	/**
	 * Will find all names of library root content starting with the input string.
	 * Will not return library folders and sub-folder's content names.
	 * 
	 * @param libraryId the id of a {@link TestCaseLibrary}
	 * @param nameStart the search param
	 * @return
	 */
	List<String> findNamesInLibraryStartingWith(long libraryId, String nameStart);

	TestCase findByIdWithInitializedSteps(long testCaseId);

	List<TestStep> findAllStepsByIdFiltered(long testCaseId, Paging filter);

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
	 * Given a list of test case ids, returns a sublist of the test case ids. An id will be included in the output list
	 * if at least one test case calls the given test case.
	 * 
	 * @param testCaseIds
	 *            the list of test case ids under inquiry.
	 * @return a sublist of the input list, with test cases never called filtered out.
	 */
	List<Long> findTestCasesHavingCaller(Collection<Long> testCasesIds);

	/**
	 * Given a list of test case ids, returns a list of the following structure :
	 * 
	 * - caller id (null if no match), - caller name (null if no match), - called id, - called name
	 * 
	 * 
	 * 
	 * Note that only first-level callers will be included if found, additional invokations will be needed to fetch all
	 * the hierarchy.
	 * 
	 * @param testCaseIds
	 *            the list of test case ids under inquiry.
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

	List<Long> findDistinctTestCasesIdsCalledByTestCase(Long testCaseId);

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
	 * @param testCaseId
	 *            the id of the test case.
	 * @param sorting
	 *            the sorting attributes and the like.
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

	/**
	 * Returns the test cases ids first called by the call steps found in the list of given test steps ids. Note: only
	 * first level called test case are returned. To get the whole test case tree you should use
	 * {@linkplain CallStepManagerService#getTestCaseCallTree(Long)}.
	 * 
	 * @param testStepsIds
	 * @return the list of test case ids called by test steps
	 */
	List<Long> findCalledTestCaseOfCallSteps(List<Long> testStepsIds);

	/**
	 * Returns paged and sorted collection of test cases verifying the requirement version of given id.
	 * 
	 * @param verifierId
	 * @param sorting
	 * @return
	 */
	List<TestCase> findAllByVerifiedRequirementVersion(long verifiedId, PagingAndSorting sorting);

	/**
	 * @param id
	 * @return
	 */
	long countByVerifiedRequirementVersion(long verifiedId);

	/**
	 * Returns unsorted collection of test cases verifying the requirement version of given id.
	 * 
	 * @param requirementId
	 * @return
	 */
	List<TestCase> findUnsortedAllByVerifiedRequirementVersion(long requirementId);
	
	List<TestCaseLibraryNode> findBySearchCriteria(TestCaseSearchCriteria criteria);
	
	/**
	 * Returns all the execution associated to this test-case
	 * @param tcId
	 * @return
	 */
	List<Execution> findAllExecutionByTestCase(Long tcId);
	
	List<ExportTestCaseData> findTestCaseToExportFromProject(List<Long> projectIds);

	List<ExportTestCaseData> findTestCaseToExportFromNodes(List<Long> nodesIds);

}

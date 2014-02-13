/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.testcase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.CyclicStepCallException;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestStepDao;
import org.squashtest.tm.service.testcase.CallStepManagerService;
import org.squashtest.tm.service.testcase.DatasetModificationService;
import org.squashtest.tm.service.testcase.TestCaseCyclicCallChecker;
import org.squashtest.tm.service.testcase.TestCaseImportanceManagerService;

@Service("squashtest.tm.service.CallStepManagerService")
@Transactional
public class CallStepManagerServiceImpl implements CallStepManagerService, TestCaseCyclicCallChecker {
	
	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private TestStepDao testStepDao;

	@Inject
	private TestCaseCallTreeFinder callTreeFinder;

	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;

	@Inject
	private DatasetModificationService datasetModificationService;
	
	@Override
	@PreAuthorize("(hasPermission(#parentTestCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'WRITE') "
			+ "and hasPermission(#calledTestCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ')) "
			+ "or hasRole('ROLE_ADMIN')")
	public void addCallTestStep(long parentTestCaseId, long calledTestCaseId) {

		if (parentTestCaseId == calledTestCaseId) {
			throw new CyclicStepCallException();
		}

		Set<Long> callTree = callTreeFinder.getTestCaseCallTree(calledTestCaseId);

		if (callTree.contains(parentTestCaseId)) {
			throw new CyclicStepCallException();
		}

		TestCase parentTestCase = testCaseDao.findById(parentTestCaseId);
		TestCase calledTestCase = testCaseDao.findById(calledTestCaseId);

		CallTestStep newStep = new CallTestStep();
		newStep.setCalledTestCase(calledTestCase);

		testStepDao.persist(newStep);

		parentTestCase.addStep(newStep);

		datasetModificationService.updateDatasetParameters(parentTestCaseId);
		testCaseImportanceManagerService.changeImportanceIfCallStepAddedToTestCases(calledTestCase, parentTestCase);
	}


	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ')"
			+ " or hasRole('ROLE_ADMIN')	")
	public TestCase findTestCase(long testCaseId) {
		return testCaseDao.findById(testCaseId);
	}
	
	
	@Override
	@PreAuthorize("hasPermission(#destinationTestCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ') or hasRole('ROLE_ADMIN')")
	public void checkForCyclicStepCallBeforePaste(long destinationTestCaseId, String[] pastedStepId) {
		List<Long> idsAsList = parseLong(pastedStepId);
		checkForCyclicStepCallBeforePaste(destinationTestCaseId, idsAsList);
	}

	@Override
	@PreAuthorize("hasPermission(#destinationTestCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ') or hasRole('ROLE_ADMIN')")
	public void checkForCyclicStepCallBeforePaste(long destinationTestCaseId, List<Long> pastedStepId) {
		List<Long> firstCalledTestCasesIds = testCaseDao.findCalledTestCaseOfCallSteps(pastedStepId);
		
		// 1> check that first called test cases are not the destination one.
		if (firstCalledTestCasesIds.contains(destinationTestCaseId)) {
			throw new CyclicStepCallException();
		}
		
		// 2> check that each first called test case doesn't have the destination one in it's callTree
		for (Long testCaseId : firstCalledTestCasesIds) {
			Set<Long> callTree = callTreeFinder.getTestCaseCallTree(testCaseId);
			if (callTree.contains(destinationTestCaseId)) {
				throw new CyclicStepCallException();
			}
		}

	}
	

	private List<Long> parseLong(String[] stringArray) {
		List<Long> longList = new ArrayList<Long>();
		for (int i = 0; i < stringArray.length; i++) {
			longList.add(Long.parseLong(stringArray[i]));
		}
		return longList;
	}

	@Override
	@Transactional(readOnly = true)
	public void checkNoCyclicCall(TestCase testCase) throws CyclicStepCallException {
		long rootTestCaseId = testCase.getId();

		List<Long> firstCalledTestCasesIds = testCaseDao.findAllDistinctTestCasesIdsCalledByTestCase(rootTestCaseId);
		// 1> find first called test cases and check they are not the parent one
		if (firstCalledTestCasesIds.contains(rootTestCaseId)) {
			throw new CyclicStepCallException();
		}
		// 2> check that each first called test case doesn't have the destination one in it's callTree
		for (Long testCaseId : firstCalledTestCasesIds) {
			Set<Long> callTree = callTreeFinder.getTestCaseCallTree(testCaseId);
			if (callTree.contains(rootTestCaseId)) {
				throw new CyclicStepCallException();
			}
		}
	}

}

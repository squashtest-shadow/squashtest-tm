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
package org.squashtest.csp.tm.internal.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.CyclicStepCallException;
import org.squashtest.csp.tm.domain.testcase.CallTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.internal.repository.TestStepDao;
import org.squashtest.csp.tm.service.CallStepManagerService;
import org.squashtest.csp.tm.service.TestCaseImportanceManagerService;

@Service("squashtest.tm.service.CallStepManagerService")
@Transactional
public class CallStepManagerServiceImpl implements CallStepManagerService, TestCaseCyclicCallChecker {
	private static final Logger LOGGER = LoggerFactory.getLogger(CallStepManagerServiceImpl.class);

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private TestStepDao testStepDao;

	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;

	@Override
	@PreAuthorize("(hasPermission(#parentTestCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') "
			+ "and hasPermission(#calledTestCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'READ')) "
			+ "or hasRole('ROLE_ADMIN')")
	public void addCallTestStep(long parentTestCaseId, long calledTestCaseId) {

		if (parentTestCaseId == calledTestCaseId) {
			throw new CyclicStepCallException();
		}

		Set<Long> callTree = getTestCaseCallTree(calledTestCaseId);
		
		if (callTree.contains(parentTestCaseId)) {
			throw new CyclicStepCallException();
		}

		TestCase parentTestCase = testCaseDao.findById(parentTestCaseId);
		TestCase calledTestCase = testCaseDao.findById(calledTestCaseId);

		CallTestStep newStep = new CallTestStep();
		newStep.setCalledTestCase(calledTestCase);

		testStepDao.persist(newStep);

		parentTestCase.addStep(newStep);

		testCaseImportanceManagerService.changeImportanceIfCallStepAddedToTestCases(calledTestCase, parentTestCase);
	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'READ')"
			+ " or hasRole('ROLE_ADMIN')	")
	public TestCase findTestCase(long testCaseId) {
		return testCaseDao.findById(testCaseId);
	}

	@Override
	@PreAuthorize("hasPermission(#rootTcId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'READ')"
			+ " or hasRole('ROLE_ADMIN')	")
	public Set<Long> getTestCaseCallTree(Long rootTcId) {

		Set<Long> calleesIds = new HashSet<Long>();
		List<Long> prevCalleesIds = testCaseDao.findDistinctTestCasesIdsCalledByTestCase(rootTcId);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("TestCase #" + rootTcId + " directly calls " + prevCalleesIds);
		}

		prevCalleesIds.remove(rootTcId);// added to prevent infinite cycle in case of inconsistent data

		while (!prevCalleesIds.isEmpty()) {
			// FIXME a tester avant correction : boucle infinie quand il y a un cycle dans les appels de cas de test
			calleesIds.addAll(prevCalleesIds);
			prevCalleesIds = testCaseDao.findAllTestCasesIdsCalledByTestCases(prevCalleesIds);

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("TestCase #" + rootTcId + " indirectly calls " + prevCalleesIds);
			}
			prevCalleesIds.remove(rootTcId);// added to prevent infinite cycle in case of inconsistent data
		}

		return calleesIds;

	}

	@Override
	public List<TestCase> findCallingTestCases(long testCaseId, CollectionSorting sorting) {
		return testCaseDao.findAllCallingTestCases(testCaseId, sorting);
	}

	@Override
	@PreAuthorize("hasPermission(#destinationTestCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'READ') or hasRole('ROLE_ADMIN')")
	public void checkForCyclicStepCallBeforePaste(long destinationTestCaseId, String[] pastedStepId) {
		List<Long> firstCalledTestCasesIds = findFirstCalledTestCasesIds(pastedStepId);
		// 1> check that first called test cases are not the destination one.
		if (firstCalledTestCasesIds.contains(destinationTestCaseId)) {
			throw new CyclicStepCallException();
		}
		// 2> check that each first called test case doesn't have the destination one in it's callTree
		for (Long testCaseId : firstCalledTestCasesIds) {
			Set<Long> callTree = getTestCaseCallTree(testCaseId);
			if (callTree.contains(destinationTestCaseId)) {
				throw new CyclicStepCallException();
			}
		}

	}

	private List<Long> findFirstCalledTestCasesIds(String[] copiedStepId) {
		List<Long> copiedStepIds = parseLong(copiedStepId);
		return testCaseDao.findCalledTestCaseOfCallSteps(copiedStepIds);
	}

	private List<Long> parseLong(String[] stringArray) {
		List<Long> longList = new ArrayList<Long>();
		for (int i = 0; i < stringArray.length; i++) {
			longList.add(Long.parseLong(stringArray[i]));
		}
		return longList;
	}

	@Override
	public void checkNoCyclicCall(TestCase testCase) throws CyclicStepCallException {
		long rootTestCaseId = testCase.getId();
		
		List<Long> firstCalledTestCasesIds = testCaseDao.findDistinctTestCasesIdsCalledByTestCase(rootTestCaseId);
		// 1> find first called test cases and check they are not the parent one
		if (firstCalledTestCasesIds.contains(rootTestCaseId)) {
			throw new CyclicStepCallException();
		}
		// 2> check that each first called test case doesn't have the destination one in it's callTree
		for (Long testCaseId : firstCalledTestCasesIds) {
			Set<Long> callTree = getTestCaseCallTree(testCaseId);
			if (callTree.contains(rootTestCaseId)) {
				throw new CyclicStepCallException();
			}
		}
	}

}

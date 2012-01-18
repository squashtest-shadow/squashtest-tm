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
package org.squashtest.csp.tm.internal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;
import org.squashtest.csp.tm.internal.repository.RequirementDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.service.CallStepManagerService;
import org.squashtest.csp.tm.service.TestCaseImportanceManagerService;

@Service
public class TestCaseImportanceManagerServiceImpl implements TestCaseImportanceManagerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseImportanceManagerServiceImpl.class);

	@Inject
	private RequirementDao requirementDao;

	@Inject
	private CallStepManagerService callStepManagerService;

	@Inject
	private TestCaseDao testCaseDao;

	/**
	 * 
	 * @param testCaseId
	 * @return distinct criticalities found for all verified requirements
	 *         (including through call steps)
	 */
	private List<RequirementCriticality> findAllDistinctRequirementsCriticalityByTestCaseId(long testCaseId) {

		Set<Long> calleesIds = callStepManagerService.getTestCaseCallTree(testCaseId);
		calleesIds.add(testCaseId);
		return requirementDao.findDistinctRequirementsCriticalitiesVerifiedByTestCases(calleesIds);

	}

	/**
	 * <p>
	 * will deduce the importance of the given test case with the list of it's
	 * associated requirement taking into account the requirement associated
	 * through call steps.
	 * </p>
	 * <p>
	 * <i>NB: this can't be done in the setter of "importanceAuto" because of
	 * the call-step associated requirements that is an info handled by the
	 * "service" package . </i>
	 * </p>
	 * 
	 * @param testCaseId
	 * @return the test case autoCalculated importance
	 */
	private TestCaseImportance deduceImportanceAuto(long testCaseId) {
		List<RequirementCriticality> rCriticalities = findAllDistinctRequirementsCriticalityByTestCaseId(testCaseId);
		TestCaseImportance importance = TestCaseImportance.deduceTestCaseImportance(rCriticalities);
		return importance;
	}

	@Override
	public void changeImportanceIfIsAuto(long testCaseId) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		if (testCase.isImportanceAuto()) {
			TestCaseImportance importance = deduceImportanceAuto(testCaseId);
			testCase.setImportance(importance);
		}
	}

	@Override
	public void changeImportanceIfRelationsAddedToReq(List<TestCase> testCases, Requirement requirement) {
		
		RequirementCriticality requirementCriticality = requirement.getCriticality();
		for (TestCase testCase : testCases) {
			changeImportanceIfRelationAdded(testCase, requirementCriticality);
		}

	}

	@Override
	public void changeImportanceIfRelationsAddedToTestCases(List<Requirement> requirements, TestCase testCase) {
		// find the strongest requirement criticalitiy level
		List<RequirementCriticality> requirementCriticalities = extractCriticalities(requirements);
		RequirementCriticality strongestRequirementCriticality = RequirementCriticality.findStrongestCriticality(requirementCriticalities);
		changeImportanceIfRelationAdded(testCase, strongestRequirementCriticality);
	}

	

	private List<RequirementCriticality> extractCriticalities(List<Requirement> requirements) {
		List<RequirementCriticality> requirementCriticalities = new ArrayList<RequirementCriticality>(requirements.size());
		for(Requirement requirement : requirements){
			requirementCriticalities.add(requirement.getCriticality());
		}
		return requirementCriticalities;
	}

	private void changeImportanceIfRelationAdded(TestCase testCase, RequirementCriticality requirementCriticality) {

		if (testCase.isImportanceAuto()) {
			TestCaseImportance importance = testCase.getImportance();
			TestCaseImportance newImportance = importance.deduceNewImporanceWhenAddCriticality(requirementCriticality);
			if (!newImportance.equals(importance)) {
				testCase.setImportance(newImportance);
				List<TestCase> callingTestCases = testCaseDao.findAllCallingTestCases(testCase.getId(), null);
				for (TestCase callingTestCase : callingTestCases) {
					changeImportanceIfRelationAdded(callingTestCase, requirementCriticality);
				}
			}
		} else {
			List<TestCase> callingTestCases = testCaseDao.findAllCallingTestCases(testCase.getId(), null);
			for (TestCase callingTestCase : callingTestCases) {
				changeImportanceIfRelationAdded(callingTestCase, requirementCriticality);
			}
		}

	}

	@Override
	public void changeImportanceIfRelationsRemovedFromReq(List<Long> testCasesIds, long requirementId) {
		// TODO Auto-generated method stub
		// for each test case call method 1-1 changeImportanceIfRelationAdded

	}

	@Override
	public void changeImportanceIfRelationsRemovedFromTestCase(List<Long> requirementsIds, long testCaseId) {
		// TODO Auto-generated method stub
		// find all reqCriticalities
		// if all reqCriticalities are lower than the importanceAuto of the
		// testCase it changes nothing
		// otherwise recompute the importance of the test-case
		// -if it changed
		// --if test case is auto => change it
		// --and look for any calling test case and recompute it's importance if
		// auto

	}

	@Override
	public void changeImportanceIfRequirementCriticalityChanged(long requirementId, RequirementCriticality oldRequirementCriticality) {
		Requirement requirement = requirementDao.findById(requirementId);
		List<TestCase> testCases = requirementDao.findAllVerifyingTestCasesById(requirementId);
		for (TestCase testCase : testCases) {
			changeImportanceIfRequirementCriticalityChanged(oldRequirementCriticality, requirement, testCase);
		}
	}

	private void changeImportanceIfRequirementCriticalityChanged(RequirementCriticality oldRequirementCriticality, Requirement requirement,
			TestCase testCase) {
		// if test-case is auto
		if (testCase.isImportanceAuto()) {
			TestCaseImportance importanceAuto = testCase.getImportance();
			// if change of criticality can change importanceAuto
			boolean importanceAutoCanChange = importanceAuto.changeOfCriticalityCanChangeImportanceAuto(oldRequirementCriticality,
					requirement.getCriticality());
			if (importanceAutoCanChange) {
				// -if it changes
				TestCaseImportance newImportanceAuto = deduceImportanceAuto(testCase.getId());

				if (!importanceAuto.equals(newImportanceAuto)) {
					// -- => change importance
					testCase.setImportance(newImportanceAuto);
					// -- look for any calling test case and call the method on
					// them

					List<TestCase> callingTestCases = testCaseDao.findAllCallingTestCases(testCase.getId(), null);
					for (TestCase callingTestCase : callingTestCases) {
						changeImportanceIfRequirementCriticalityChanged(oldRequirementCriticality, requirement, callingTestCase);
					}
				}

			}
		} else {
			// call the method in callers
			List<TestCase> callingTestCases = testCaseDao.findAllCallingTestCases(testCase.getId(), null);
			for (TestCase callingTestCase : callingTestCases) {
				changeImportanceIfRequirementCriticalityChanged(oldRequirementCriticality, requirement, callingTestCase);
			}
		}
	}

}

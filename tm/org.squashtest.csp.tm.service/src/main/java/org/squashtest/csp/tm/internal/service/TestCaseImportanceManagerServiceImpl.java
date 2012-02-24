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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;
import org.squashtest.csp.tm.internal.repository.RequirementDao;
import org.squashtest.csp.tm.internal.repository.RequirementVersionDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.service.CallStepManagerService;
import org.squashtest.csp.tm.service.TestCaseImportanceManagerService;

@Service
public class TestCaseImportanceManagerServiceImpl implements TestCaseImportanceManagerService {

	@Inject
	private RequirementDao requirementDao;
	@Inject
	private RequirementVersionDao requirementVersionDao;

	@Inject
	private CallStepManagerService callStepManagerService;

	@Inject
	private TestCaseDao testCaseDao;

	private Map<TestCase, List<RequirementCriticality>> requirementDeletionConcernedTestCases;

	/**
	 * 
	 * @param testCaseId
	 * @return distinct criticalities found for all verified requirementVersions (including through call steps)
	 */
	private List<RequirementCriticality> findAllDistinctRequirementsCriticalityByTestCaseId(long testCaseId) {
		Set<Long> calleesIds = callStepManagerService.getTestCaseCallTree(testCaseId);
		calleesIds.add(testCaseId);
		return requirementDao.findDistinctRequirementsCriticalitiesVerifiedByTestCases(calleesIds);

	}

	/**
	 * <p>
	 * will deduce the importance of the given test case with the list of it's associated requirementVersions taking
	 * into account the requirementVersions associated through call steps.
	 * </p>
	 * <p>
	 * <i>NB: this can't be done in the setter of "importanceAuto" because of the call-step associated
	 * requirementVersions that is an info handled by the "service" package . </i>
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

	/**
	 * @see org.squashtest.csp.tm.service.TestCaseImportanceManagerService#changeImportanceIfIsAuto(long)
	 */
	@Override
	public void changeImportanceIfIsAuto(long testCaseId) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		if (testCase.isImportanceAuto()) {
			TestCaseImportance importance = deduceImportanceAuto(testCaseId);
			testCase.setImportance(importance);
		}
	}

	/**
	 * @see org.squashtest.csp.tm.service.TestCaseImportanceManagerService#changeImportanceIfRelationsAddedToReq(List,
	 *      RequirementVersion)
	 */
	@Override
	public void changeImportanceIfRelationsAddedToReq(List<TestCase> testCases, RequirementVersion requirementVersion) {
		RequirementCriticality requirementCriticality = requirementVersion.getCriticality();
		for (TestCase testCase : testCases) {
			changeImportanceIfRelationAdded(testCase, requirementCriticality);
		}
	}

	/**
	 * @see org.squashtest.csp.tm.service.TestCaseImportanceManagerService#changeImportanceIfRelationsAddedToTestCases(List,
	 *      TestCase)
	 */
	@Override
	public void changeImportanceIfRelationsAddedToTestCase(List<RequirementVersion> requirementVersions,
			TestCase testCase) {
		if (!requirementVersions.isEmpty()) {
			List<RequirementCriticality> requirementCriticalities = extractCriticalities(requirementVersions);
			RequirementCriticality strongestRequirementCriticality = RequirementCriticality
					.findStrongestCriticality(requirementCriticalities);
			changeImportanceIfRelationAdded(testCase, strongestRequirementCriticality);
		}
	}

	private List<RequirementCriticality> extractCriticalities(List<RequirementVersion> requirementVersions) {
		List<RequirementCriticality> requirementCriticalities = new ArrayList<RequirementCriticality>(
				requirementVersions.size());
		for (RequirementVersion requirementVersion : requirementVersions) {
			requirementCriticalities.add(requirementVersion.getCriticality());
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

	/**
	 * @see org.squashtest.csp.tm.service.TestCaseImportanceManagerService#changeImportanceIfRelationsRemovedFromReq(List,
	 *      long)
	 */
	@Override
	public void changeImportanceIfRelationsRemovedFromReq(List<Long> testCasesIds, long requirementVersionId) {
		RequirementVersion requirementVersion = requirementVersionDao.findById(requirementVersionId);
		RequirementCriticality requirementCriticality = requirementVersion.getCriticality();
		TestCaseImportance reqCritImportance = TestCaseImportance.deduceTestCaseImportance(Arrays
				.asList(requirementCriticality));
		List<TestCase> testCases = extractTestCases(testCasesIds);
		for (TestCase testCase : testCases) {
			changeImportanceIfRelationRemoved(reqCritImportance, testCase);
		}
	}

	private void changeImportanceIfRelationRemoved(TestCaseImportance maxReqCritImportance, TestCase testCase) {
		if (testCase.isImportanceAuto()) {
			TestCaseImportance actualImportance = testCase.getImportance();
			if (maxReqCritImportance.getLevel() <= actualImportance.getLevel()) {
				TestCaseImportance newImportance = deduceImportanceAuto(testCase.getId());
				if (!newImportance.equals(actualImportance)) {
					testCase.setImportance(newImportance);
					List<TestCase> callingTestCases = testCaseDao.findAllCallingTestCases(testCase.getId(), null);
					for (TestCase callingTestCase : callingTestCases) {
						changeImportanceIfRelationRemoved(maxReqCritImportance, callingTestCase);
					}
				}
			}
		} else {
			List<TestCase> callingTestCases = testCaseDao.findAllCallingTestCases(testCase.getId(), null);
			for (TestCase callingTestCase : callingTestCases) {
				changeImportanceIfRelationRemoved(maxReqCritImportance, callingTestCase);
			}
		}
	}

	private List<TestCase> extractTestCases(List<Long> testCasesIds) {
		List<TestCase> testCases = new ArrayList<TestCase>(testCasesIds.size());
		for (long testCaseId : testCasesIds) {
			testCases.add(testCaseDao.findById(testCaseId));

		}
		return testCases;
	}

	/**
	 * @see org.squashtest.csp.tm.service.TestCaseImportanceManagerService#changeImportanceIfRelationsRemovedFromTestCase(List,
	 *      long)
	 */
	@Override
	public void changeImportanceIfRelationsRemovedFromTestCase(List<Long> requirementsVersionIds, long testCaseId) {
		if (!requirementsVersionIds.isEmpty()) {
			TestCase testCase = testCaseDao.findById(testCaseId);
			List<RequirementCriticality> reqCriticalities = requirementDao
					.findDistinctRequirementsCriticalities(requirementsVersionIds);
			TestCaseImportance maxReqCritImportance = TestCaseImportance.deduceTestCaseImportance(reqCriticalities);
			changeImportanceIfRelationRemoved(maxReqCritImportance, testCase);
		}
	}

	/**
	 * @see org.squashtest.csp.tm.service.TestCaseImportanceManagerService#changeImportanceIfRequirementCriticalityChanged(long,
	 *      RequirementCriticality)
	 */
	@Override
	public void changeImportanceIfRequirementCriticalityChanged(long requirementVersionId,
			RequirementCriticality oldRequirementCriticality) {
		RequirementVersion requirementVersion = requirementVersionDao.findById(requirementVersionId);
		List<TestCase> testCases = testCaseDao.findUnsortedAllByVerifiedRequirementVersion(requirementVersionId);
		for (TestCase testCase : testCases) {
			changeImportanceIfRequirementCriticalityChanged(oldRequirementCriticality, requirementVersion, testCase);
		}
	}

	private void changeImportanceIfRequirementCriticalityChanged(RequirementCriticality oldRequirementCriticality,
			RequirementVersion requirementVersion, TestCase testCase) {
		// if test-case is auto
		if (testCase.isImportanceAuto()) {
			TestCaseImportance importanceAuto = testCase.getImportance();
			// if change of criticality can change importanceAuto
			boolean importanceAutoCanChange = importanceAuto.changeOfCriticalityCanChangeImportanceAuto(
					oldRequirementCriticality, requirementVersion.getCriticality());
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
						changeImportanceIfRequirementCriticalityChanged(oldRequirementCriticality, requirementVersion,
								callingTestCase);
					}
				}

			}
		} else {
			// call the method in callers
			List<TestCase> callingTestCases = testCaseDao.findAllCallingTestCases(testCase.getId(), null);
			for (TestCase callingTestCase : callingTestCases) {
				changeImportanceIfRequirementCriticalityChanged(oldRequirementCriticality, requirementVersion,
						callingTestCase);
			}
		}
	}

	/**
	 * @see org.squashtest.csp.tm.service.TestCaseImportanceManagerService#changeImportanceIfCallStepAddedToTestCases(TestCase,
	 *      TestCase)
	 */
	@Override
	public void changeImportanceIfCallStepAddedToTestCases(TestCase calledTestCase, TestCase parentTestCase) {
		List<RequirementCriticality> rCriticalities = findAllDistinctRequirementsCriticalityByTestCaseId(calledTestCase
				.getId());
		if (!rCriticalities.isEmpty()) {
			RequirementCriticality strongestRequirementCriticality = RequirementCriticality
					.findStrongestCriticality(rCriticalities);
			changeImportanceIfRelationAdded(parentTestCase, strongestRequirementCriticality);
		}
	}

	/**
	 * @see org.squashtest.csp.tm.service.TestCaseImportanceManagerService#changeImportanceIfCallStepRemoved(TestCase,
	 *      TestCase)
	 */
	@Override
	public void changeImportanceIfCallStepRemoved(TestCase calledTestCase, TestCase parentTestCase) {
		List<RequirementCriticality> rCriticalities = findAllDistinctRequirementsCriticalityByTestCaseId(calledTestCase
				.getId());
		if (!rCriticalities.isEmpty()) {
			TestCaseImportance maxReqCritImportance = TestCaseImportance.deduceTestCaseImportance(rCriticalities);
			changeImportanceIfRelationRemoved(maxReqCritImportance, parentTestCase);
		}

	}

	/**
	 * @see org.squashtest.csp.tm.service.TestCaseImportanceManagerService#prepareRequirementDeletion(List)
	 */
	@Override
	public void prepareRequirementDeletion(List<Long> requirementIds) {
		this.requirementDeletionConcernedTestCases = new HashMap<TestCase, List<RequirementCriticality>>();
		List<RequirementVersion> versions = requirementDao.findVersionsForAll(requirementIds);
		storeReqVersionConcernedTestCases(versions);

	}

	private void storeReqVersionConcernedTestCases(List<RequirementVersion> requirementVersions) {
		for (RequirementVersion reqVersion : requirementVersions) {
			Set<TestCase> concernedTestCases = reqVersion.getVerifyingTestCases();
			RequirementCriticality nextCriticality = reqVersion.getCriticality();
			storeConcernedTestCases(concernedTestCases, nextCriticality);
		}
	}

	private void storeConcernedTestCases(Set<TestCase> concernedTestCases, RequirementCriticality nextCriticality) {
		for (TestCase testCase : concernedTestCases) {
			storeConcernedTestCase(nextCriticality, testCase);
		}
	}

	private void storeConcernedTestCase(RequirementCriticality nextCriticality, TestCase testCase) {
		if (this.requirementDeletionConcernedTestCases.containsKey(testCase)) {
			List<RequirementCriticality> storedCriticalities = this.requirementDeletionConcernedTestCases.get(testCase);
			storedCriticalities.add(nextCriticality);

		} else {
			List<RequirementCriticality> reqCriticalities = new ArrayList<RequirementCriticality>();
			reqCriticalities.add(nextCriticality);
			this.requirementDeletionConcernedTestCases.put(testCase, reqCriticalities);

		}
	}

	/**
	 * @see org.squashtest.csp.tm.service.TestCaseImportanceManagerService#changeImportanceAfterRequirementDeletion()
	 */
	@Override
	public void changeImportanceAfterRequirementDeletion() {
		if (this.requirementDeletionConcernedTestCases != null && !this.requirementDeletionConcernedTestCases.isEmpty()) {
			for (Entry<TestCase, List<RequirementCriticality>> testCaseAndCriticalities : this.requirementDeletionConcernedTestCases
					.entrySet()) {
				TestCase testCase = testCaseAndCriticalities.getKey();
				List<RequirementCriticality> requirementCriticalities = testCaseAndCriticalities.getValue();
				TestCaseImportance maxReqCritImportance = TestCaseImportance
						.deduceTestCaseImportance(requirementCriticalities);
				changeImportanceIfRelationRemoved(maxReqCritImportance, testCase);
			}
		}
		this.requirementDeletionConcernedTestCases = null;
	}
}

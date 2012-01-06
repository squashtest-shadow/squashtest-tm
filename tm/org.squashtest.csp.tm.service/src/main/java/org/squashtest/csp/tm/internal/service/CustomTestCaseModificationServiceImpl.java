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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;
import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.core.infrastructure.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionFilter;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.internal.repository.RequirementDao;
import org.squashtest.csp.tm.internal.repository.RequirementVersionDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.internal.repository.TestStepDao;
import org.squashtest.csp.tm.service.CallStepManagerService;
import org.squashtest.csp.tm.service.CustomTestCaseModificationService;
import org.squashtest.csp.tm.service.VerifiedRequirement;

/**
 * @author Gregory Fouquet
 * 
 */
@Service("CustomTestCaseModificationService")
public class CustomTestCaseModificationServiceImpl implements CustomTestCaseModificationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomTestCaseModificationServiceImpl.class);

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private TestStepDao testStepDao;

	@Inject
	@Named("squashtest.tm.service.internal.TestCaseManagementService")
	private NodeManagementService<TestCase, TestCaseLibraryNode, TestCaseFolder> testCaseManagementService;

	@Inject
	private RequirementVersionDao requirementVersionDao; 

	@Inject
	private CallStepManagerService callStepManagerService;

	@Inject
	private TestCaseNodeDeletionHandler deletionHandler;

	/* *************** TestCase section ***************************** */

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void rename(long testCaseId, String newName) throws DuplicateNameException {
		testCaseManagementService.renameNode(testCaseId, newName);
	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'READ') or hasRole('ROLE_ADMIN')")
	public TestCase findById(long testCaseId) {
		return testCaseDao.findById(testCaseId);
	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'READ') or hasRole('ROLE_ADMIN')")
	public List<TestStep> findStepsByTestCaseId(long testCaseId) {

		TestCase testCase = testCaseDao.findByIdWithInitializedSteps(testCaseId);
		return testCase.getSteps();

	}

	/* *************** TestStep section ***************************** */

	@Override
	@PreAuthorize("hasPermission(#parentTestCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public TestStep addActionTestStep(long parentTestCaseId, ActionTestStep newTestStep) {
		TestCase parentTestCase = testCaseDao.findById(parentTestCaseId);

		// will throw a nasty NullPointerException if the parent test case can't be found
		parentTestCase.addStep(newTestStep);
		testStepDao.persist(newTestStep);

		return newTestStep;
	}

	@Override
	@PreAuthorize("hasPermission(#testStepId, 'org.squashtest.csp.tm.domain.testcase.TestStep' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void updateTestStepAction(long testStepId, String newAction) {
		ActionTestStep testStep = (ActionTestStep) testStepDao.findById(testStepId);
		testStep.setAction(newAction);
	}

	@Override
	@PreAuthorize("hasPermission(#testStepId, 'org.squashtest.csp.tm.domain.testcase.TestStep' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void updateTestStepExpectedResult(long testStepId, String newExpectedResult) {
		ActionTestStep testStep = (ActionTestStep) testStepDao.findById(testStepId);
		testStep.setExpectedResult(newExpectedResult);
	}

	@Override
	@Deprecated
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void changeTestStepPosition(long testCaseId, long testStepId, int newStepPosition) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		int index = findTestStepInTestCase(testCase, testStepId);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("**************** change step order : old index = " + index + ",new index : "
					+ newStepPosition);
		}

		testCase.moveStep(index, newStepPosition);
	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void changeTestStepsPosition(long testCaseId, int newPosition, List<Long> stepIds) {

		TestCase testCase = testCaseDao.findById(testCaseId);
		List<TestStep> steps = testStepDao.findListById(stepIds);

		testCase.moveSteps(newPosition, steps);

	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void removeStepFromTestCase(long testCaseId, long testStepId) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		TestStep testStep = testStepDao.findById(testStepId);
		deletionHandler.deleteStep(testCase, testStep);
	}

	/*
	 * given a TestCase, will search for a TestStep in the steps list (identified with its testStepId)
	 * 
	 * returns : the index if found, -1 if not found or if the provided TestCase is null
	 */
	private int findTestStepInTestCase(TestCase testCase, long testStepId) {
		return testCase.getPositionOfStep(testStepId);
	}

	@Override
	@PostAuthorize("hasPermission(returnObject, 'READ') or hasRole('ROLE_ADMIN')")
	public TestCase findTestCaseWithSteps(long testCaseId) {
		return testCaseDao.findAndInit(testCaseId);
	}

	/*
	 * regarding the @PreAuthorize for the verified requirements :
	 * 
	 * I prefer to show all the requirements that the test case refers to even if some of those requirements belongs to
	 * a project the current user cannot "read", rather post filtering it.
	 * 
	 * The reason for that is that such policy is impractical for the same problem in the context of Iteration-TestCase
	 * associations : filtering the test cases wouldn't make much sense and would lead to partial executions of a
	 * campaign.
	 * 
	 * Henceforth the same policy applies to other cases of possible inter-project associations (like
	 * TestCase-Requirement associations in the present case), for the sake of coherence.
	 * 
	 * @author bsiri
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.csp.tm.service.TestCaseModificationService#findVerifiedRequirementsByTestCaseId(long,
	 * org.squashtest.csp.tm.infrastructure.filter.CollectionSorting)
	 */
	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<Requirement>> findAllDirectlyVerifiedRequirementsByTestCaseId(long testCaseId,
			CollectionSorting filter) {
		List<Requirement> reqs = testCaseDao.findAllDirectlyVerifiedRequirementsByIdFiltered(testCaseId, filter);
		long count = testCaseDao.countVerifiedRequirementsById(testCaseId);
		return new FilteredCollectionHolder<List<Requirement>>(count, reqs);
	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void removeListOfSteps(long testCaseId, List<Long> testStepIds) {
		TestCase testCase = testCaseDao.findById(testCaseId);

		for (Long id : testStepIds) {
			TestStep step = testStepDao.findById(id);
			deletionHandler.deleteStep(testCase, step);
		}

	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<TestStep>> findStepsByTestCaseIdFiltered(long testCaseId,
			CollectionFilter filter) {
		List<TestStep> list = testCaseDao.findAllStepsByIdFiltered(testCaseId, filter);
		long count = findStepsByTestCaseId(testCaseId).size();
		return new FilteredCollectionHolder<List<TestStep>>(count, list);
	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void pasteCopiedTestStep(Long testCaseId, Long idToCopyAfter, Long copiedTestStepId) {
		TestStep original = testStepDao.findById(copiedTestStepId);
		// FIXME il faut vérifier un éventuel cycle !
		TestStep copyStep = original.createCopy();

		testStepDao.persist(copyStep);

		TestCase testCase = testCaseDao.findAndInit(testCaseId);
		int index;

		if (idToCopyAfter != null) {
			TestStep stepToCopyAfter = testStepDao.findById(idToCopyAfter);
			index = testCase.getSteps().indexOf(stepToCopyAfter) + 1;
		} else {
			index = 0;
		}

		testCase.addStep(index, copyStep);
	}

	@Override
	public PagedCollectionHolder<List<VerifiedRequirement>> findAllVerifiedRequirementsByTestCaseId(long testCaseId,
			PagingAndSorting pas) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Looking for verified requirements of TestCase[id: " + testCaseId + ']');
		}

		Set<Long> calleesIds = callStepManagerService.getTestCaseCallTree(testCaseId);

		calleesIds.add(testCaseId);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Fetching Requirements verified by TestCases " + calleesIds);
		}
		List<RequirementVersion> verified = requirementVersionDao.findAllVerifiedByTestCases(calleesIds, pas);

		TestCase mainTestCase = testCaseDao.findById(testCaseId);

		 List<VerifiedRequirement> verifiedReqs = buildVerifiedRequirementList(mainTestCase.getVerifiedRequirements(),
		 verified );

		long verifiedCount = requirementVersionDao.countVerifiedByTestCases(calleesIds);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Total count of verified requirements : " + verifiedCount);
		}

		return new PagingBackedPagedCollectionHolder<List<VerifiedRequirement>>(pas, verifiedCount,
				verifiedReqs);
	}

	/*
	 * 
	 */
	private List<VerifiedRequirement> buildVerifiedRequirementList(final Collection<RequirementVersion> directlyVerifiedList,
			List<RequirementVersion> verified) {

		List<VerifiedRequirement> toReturn = new ArrayList<VerifiedRequirement>(verified.size());

		for (RequirementVersion req : verified) {
			boolean directlyVerified = directlyVerifiedList.contains(req);

			 toReturn.add(new VerifiedRequirement(req, directlyVerified));
		}

		return toReturn;
	}

	@Override
	public FilteredCollectionHolder<List<TestCase>> findCallingTestCases(long testCaseId, CollectionSorting sorting) {

		List<TestCase> callers = callStepManagerService.findCallingTestCases(testCaseId, sorting);
		Long countCallers = testCaseDao.countCallingTestSteps(testCaseId);
		return new FilteredCollectionHolder<List<TestCase>>(countCallers, callers);

	}

}

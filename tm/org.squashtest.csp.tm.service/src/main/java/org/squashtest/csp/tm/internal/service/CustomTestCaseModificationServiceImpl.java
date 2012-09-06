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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.domain.testautomation.AutomatedTest;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.CallTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.internal.repository.RequirementVersionDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.internal.repository.TestStepDao;
import org.squashtest.csp.tm.internal.testautomation.service.InsecureTestAutomationManagementService;
import org.squashtest.csp.tm.service.CallStepManagerService;
import org.squashtest.csp.tm.service.CustomTestCaseModificationService;
import org.squashtest.csp.tm.service.TestCaseImportanceManagerService;
import org.squashtest.csp.tm.service.VerifiedRequirement;
import org.squashtest.csp.tm.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;


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
	private TestCaseImportanceManagerService testCaseImportanceManagerService;

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
	
	@Inject
	private InsecureTestAutomationManagementService taService;

	/* *************** TestCase section ***************************** */

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'SMALL_EDIT') or hasRole('ROLE_ADMIN')")
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
			Paging filter) {
		List<TestStep> list = testCaseDao.findAllStepsByIdFiltered(testCaseId, filter);
		long count = findStepsByTestCaseId(testCaseId).size();
		return new FilteredCollectionHolder<List<TestStep>>(count, list);
	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void pasteCopiedTestStep(long testCaseId, long idToCopyAfter, long copiedTestStepId) {
		TestStep original = testStepDao.findById(copiedTestStepId);
		// FIXME il faut vÃ©rifier un Ã©ventuel cycle ! // pour l'instant vérifié au niveau du controller
		TestStep copyStep = original.createCopy();

		testStepDao.persist(copyStep);

		TestCase testCase = testCaseDao.findAndInit(testCaseId);
		int index;

		TestStep stepToCopyAfter = testStepDao.findById(idToCopyAfter);
		index = testCase.getSteps().indexOf(stepToCopyAfter) + 1;

		testCase.addStep(index, copyStep);

		if (!testCase.getSteps().contains(original)) {
			updateImportanceIfCallStep(testCase, copyStep);
		}

	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void pasteCopiedTestStepToLastIndex(long testCaseId, long copiedTestStepId) {
		TestStep original = testStepDao.findById(copiedTestStepId);
		// FIXME il faut vÃ©rifier un Ã©ventuel cycle ! // pour l'instant vérifié au niveau du controller
		TestStep copyStep = original.createCopy();

		testStepDao.persist(copyStep);

		TestCase testCase = testCaseDao.findAndInit(testCaseId);

		testCase.addStep(copyStep);

		if (!testCase.getSteps().contains(original)) {
			updateImportanceIfCallStep(testCase, copyStep);
		}

	}

	private void updateImportanceIfCallStep(TestCase parentTestCase, TestStep copyStep) {
		if (copyStep instanceof CallTestStep) {
			TestCase called = ((CallTestStep) copyStep).getCalledTestCase();
			testCaseImportanceManagerService.changeImportanceIfCallStepAddedToTestCases(called, parentTestCase);
		}
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

		List<VerifiedRequirement> verifiedReqs = buildVerifiedRequirementList(
				mainTestCase.getVerifiedRequirementVersions(), verified);

		long verifiedCount = requirementVersionDao.countVerifiedByTestCases(calleesIds);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Total count of verified requirements : " + verifiedCount);
		}

		return new PagingBackedPagedCollectionHolder<List<VerifiedRequirement>>(pas, verifiedCount, verifiedReqs);
	}

	/*
	 * 
	 */
	private List<VerifiedRequirement> buildVerifiedRequirementList(
			final Collection<RequirementVersion> directlyVerifiedList, List<RequirementVersion> verified) {

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

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void changeImportanceAuto(long testCaseId, boolean auto) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		testCase.setImportanceAuto(auto);
		testCaseImportanceManagerService.changeImportanceIfIsAuto(testCaseId);
	}

	
	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public Collection<TestAutomationProjectContent> findAssignableAutomationTests(
			long testCaseId) {
		
		TestCase testCase = testCaseDao.findById(testCaseId);
		
		return taService.listTestsInProjects(testCase.getProject().getTestAutomationProjects());
	}
	
	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public AutomatedTest bindAutomatedTest(Long testCaseId, Long taProjectId, String testName) {
		
		TestAutomationProject project = taService.findProjectById(taProjectId); 
		
		AutomatedTest newTest = new AutomatedTest(testName, project);
		
		AutomatedTest persistedTest =  taService.persistOrAttach(newTest);
		
		TestCase testCase = testCaseDao.findById(testCaseId);
		
		testCase.setTestAutomationTest(persistedTest);
		
		return persistedTest;
	}
}

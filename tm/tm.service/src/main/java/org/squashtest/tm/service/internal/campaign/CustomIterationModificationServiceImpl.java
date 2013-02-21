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
package org.squashtest.tm.service.internal.campaign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.exception.TestPlanItemNotExecutableException;
import org.squashtest.tm.service.campaign.CustomIterationModificationService;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.denormalizedField.PrivateDenormalizedFieldValueService;
import org.squashtest.tm.service.internal.library.PasteStrategy;
import org.squashtest.tm.service.internal.library.TreeNodeCopier;
import org.squashtest.tm.service.internal.repository.AutomatedSuiteDao;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.squashtest.tm.service.internal.repository.ItemTestPlanDao;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.project.ProjectsPermissionFinder;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;
import org.squashtest.tm.service.testcase.TestCaseCyclicCallChecker;
import org.squashtest.tm.service.user.UserAccountService;

@Service("CustomIterationModificationService")
@Transactional
public class CustomIterationModificationServiceImpl implements CustomIterationModificationService,
		IterationTestPlanManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomIterationModificationServiceImpl.class);
	private static final String OR_HAS_ROLE_ADMIN = "or hasRole('ROLE_ADMIN')";
	private static final String PERMISSION_EXECUTE_ITERATION = "hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'EXECUTE') ";
	@Inject
	private CampaignDao campaignDao;
	@Inject
	private IterationDao iterationDao;
	@Inject
	private TestSuiteDao suiteDao;
	@Inject
	private ItemTestPlanDao testPlanDao;
	@Inject
	private AutomatedSuiteDao autoSuiteDao;
	@Inject
	private ExecutionDao executionDao;
	@Inject
	private TestCaseCyclicCallChecker testCaseCyclicCallChecker;
	@Inject
	private CampaignNodeDeletionHandler deletionHandler;
	@Inject
	private PermissionEvaluationService permissionService;
	@Inject
	private PrivateCustomFieldValueService customFieldValueService;
	@Inject
	private PrivateDenormalizedFieldValueService denormalizedFieldValueService;
	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToIterationStrategy")
	private Provider<PasteStrategy<Iteration, TestSuite>> pasteToIterationStrategyProvider;
	@Inject
	private ProjectsPermissionFinder projectsPermissionFinder;
	@Inject
	private UserAccountService userService;
	@Inject
	private ObjectFactory<TreeNodeCopier> treeNodeCopierFactory;

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public int addIterationToCampaign(Iteration iteration, long campaignId, boolean copyTestPlan) {
		Campaign campaign = campaignDao.findById(campaignId);

		// copy the campaign test plan in the iteration

		List<CampaignTestPlanItem> campaignTestPlan = campaign.getTestPlan();

		if (copyTestPlan) {
			for (CampaignTestPlanItem campaignItem : campaignTestPlan) {
				IterationTestPlanItem iterationItem = new IterationTestPlanItem(campaignItem.getReferencedTestCase());
				iterationItem.setUser(campaignItem.getUser());
				iteration.addTestPlan(iterationItem);
			}
		} else {
			for (CampaignTestPlanItem campaignItem : campaignTestPlan) {
				IterationTestPlanItem iterationItem = new IterationTestPlanItem(campaignItem.getReferencedTestCase());
				iterationItem.setUser(campaignItem.getUser());
			}
		}
		iterationDao.persistIterationAndTestPlan(iteration);
		campaign.addIteration(iteration);
		customFieldValueService.createAllCustomFieldValues(iteration);
		return campaign.getIterations().size() - 1;
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'READ') "
			+ OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<Iteration> findIterationsByCampaignId(long campaignId) {
		return campaignDao.findByIdWithInitializedIterations(campaignId).getIterations();
	}

	@Override
	@PostAuthorize("hasPermission(returnObject, 'READ') " + OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public Iteration findById(long iterationId) {
		return iterationDao.findById(iterationId);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'DELETE') "
			+ OR_HAS_ROLE_ADMIN)
	public String delete(long iterationId) {
		Iteration iteration = iterationDao.findById(iterationId);
		if (iteration == null) {
			return "ko";
		}

		iterationDao.removeFromCampaign(iteration);
		iterationDao.remove(iteration);

		return "ok";

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'SMALL_EDIT') "
			+ OR_HAS_ROLE_ADMIN)
	public void rename(long iterationId, String newName) {
		Iteration iteration = iterationDao.findById(iterationId);

		iteration.setName(newName);
	}

	@Override
	@PreAuthorize(PERMISSION_EXECUTE_ITERATION + OR_HAS_ROLE_ADMIN)
	public Execution addExecution(long iterationId, long testPlanId) {

		Iteration iteration = iterationDao.findAndInit(iterationId);
		IterationTestPlanItem item = iteration.getTestPlan(testPlanId);

		return addExecution(item);
	}

	@Override
	@PreAuthorize(PERMISSION_EXECUTE_ITERATION + OR_HAS_ROLE_ADMIN)
	public Execution addAutomatedExecution(long iterationId, long testPlanId) {

		Iteration iteration = iterationDao.findAndInit(iterationId);
		IterationTestPlanItem item = iteration.getTestPlan(testPlanId);

		return addAutomatedExecution(item);
	}

	@Override
	@PreAuthorize(PERMISSION_EXECUTE_ITERATION + OR_HAS_ROLE_ADMIN)
	public AutomatedSuite createAutomatedSuite(long iterationId) {

		AutomatedSuite newSuite = autoSuiteDao.createNewSuite();

		Iteration iteration = iterationDao.findById(iterationId);

		for (IterationTestPlanItem item : iteration.getTestPlans()) {
			if (item.isAutomated()) {
				Execution exec = addAutomatedExecution(item);
				newSuite.addExtender(exec.getAutomatedExecutionExtender());
			}
		}

		return newSuite;
	}

	@Override
	@PreAuthorize(PERMISSION_EXECUTE_ITERATION + OR_HAS_ROLE_ADMIN)
	public AutomatedSuite createAutomatedSuite(long iterationId, Collection<Long> testPlanIds) {

		AutomatedSuite newSuite = autoSuiteDao.createNewSuite();

		List<IterationTestPlanItem> items = testPlanDao.findAllByIds(testPlanIds);

		for (IterationTestPlanItem item : items) {
			if (item.isAutomated()) {
				Execution exec = addAutomatedExecution(item);
				newSuite.addExtender(exec.getAutomatedExecutionExtender());
			}

		}

		return newSuite;

	}

	/**
	 * @see CustomIterationModificationService#changeTestPlanPosition(long, int, List)
	 */
	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'LINK') "
			+ OR_HAS_ROLE_ADMIN)
	public void changeTestPlanPosition(long iterationId, int newPosition, List<Long> itemIds) {
		Iteration iteration = iterationDao.findById(iterationId);
		List<IterationTestPlanItem> items = testPlanDao.findAllByIds(itemIds);

		iteration.moveTestPlans(newPosition, items);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') "
			+ OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<Execution> findAllExecutions(long iterationId) {
		return iterationDao.findOrderedExecutionsByIterationId(iterationId);

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') "
			+ OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<Execution> findExecutionsByTestPlan(long iterationId, long testPlanId) {
		return iterationDao.findOrderedExecutionsByIterationAndTestPlan(iterationId, testPlanId);

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') "
			+ OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<TestCase> findPlannedTestCases(long iterationId) {
		Iteration iteration = iterationDao.findById(iterationId);
		return iteration.getPlannedTestCase();
	}

	@Override
	public List<SuppressionPreviewReport> simulateDeletion(List<Long> targetIds) {
		return deletionHandler.simulateIterationDeletion(targetIds);
	}

	@Override
	public List<Long> deleteNodes(List<Long> targetIds) {
		return deletionHandler.deleteIterations(targetIds);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public void addTestSuite(long iterationId, TestSuite suite) {
		Iteration iteration = iterationDao.findById(iterationId);
		addTestSuite(iteration, suite);
	}

	@Override
	public void addTestSuite(Iteration iteration, TestSuite suite) {
		suiteDao.persist(suite);
		iteration.addTestSuite(suite);
		customFieldValueService.createAllCustomFieldValues(suite);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = true)
	public List<TestSuite> findAllTestSuites(long iterationId) {
		return iterationDao.findAllTestSuites(iterationId);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public TestSuite copyPasteTestSuiteToIteration(long testSuiteId, long iterationId) {
		return createCopyToIterationStrategy().pasteNodes(iterationId, Arrays.asList(testSuiteId)).get(0);
	}

	private PasteStrategy<Iteration, TestSuite> createCopyToIterationStrategy() {
		PasteStrategy<Iteration, TestSuite> pasteStrategy = pasteToIterationStrategyProvider.get();
		pasteStrategy.setFirstLayerOperationFactory(treeNodeCopierFactory);
		pasteStrategy.setNextLayersOperationFactory(treeNodeCopierFactory);
		return pasteStrategy;
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public List<TestSuite> copyPasteTestSuitesToIteration(Long[] testSuiteIds, long iterationId) {
		return createCopyToIterationStrategy().pasteNodes(iterationId, Arrays.asList(testSuiteIds));
	}

	@Override
	public List<Long> removeTestSuites(List<Long> suitesIds) {
		List<TestSuite> testSuites = suiteDao.findAllByIds(suitesIds);
		// check
		checkPermissionsForAll(testSuites, "DELETE");
		// proceed
		return deletionHandler.deleteSuites(suitesIds);

	}

	@Override
	public Execution addExecution(IterationTestPlanItem item) throws TestPlanItemNotExecutableException {

		Execution execution = item.createExecution(testCaseCyclicCallChecker);
		// if we don't persist before we add, add will trigger an update of item.testPlan which fail because execution
		// has no id yet. this is caused by weird mapping (https://hibernate.onjira.com/browse/HHH-5732)
		executionDao.persist(execution);
		item.addExecution(execution);
		createDenormalizedFieldsForExecutionAndExecutionSteps(execution);

		return execution;
	}

	private void createDenormalizedFieldsForExecutionAndExecutionSteps(Execution execution) {
		LOGGER.debug("Create denormalized fields for Execution {}", execution.getId());
		TestCase sourceTC = execution.getReferencedTestCase();
		denormalizedFieldValueService.createAllDenormalizedFieldValues(sourceTC, execution);
		for (ExecutionStep step : execution.getSteps()) {
			TestStep sourceStep = step.getReferencedTestStep();
			if (stepIsFromSameProjectAsTC(sourceTC, (ActionTestStep) sourceStep)) {
				denormalizedFieldValueService.createAllDenormalizedFieldValues((ActionTestStep)sourceStep, step);
			} else {
				denormalizedFieldValueService.createAllDenormalizedFieldValues((ActionTestStep)sourceStep, step, sourceTC.getProject());
			}
		}

	}

	private boolean stepIsFromSameProjectAsTC(TestCase sourceTC, ActionTestStep sourceStep) {
		return sourceStep.getProject().getId().equals(sourceTC.getProject().getId());
	}

	public Execution addAutomatedExecution(IterationTestPlanItem item) throws TestPlanItemNotExecutableException {

		Execution execution = item.createAutomatedExecution(testCaseCyclicCallChecker);

		executionDao.persist(execution);
		item.addExecution(execution);
		createDenormalizedFieldsForExecutionAndExecutionSteps(execution);

		return execution;

	}

	/* ************************* security ************************* */

	private void checkPermission(SecurityCheckableObject... checkableObjects) {
		PermissionsUtils.checkPermission(permissionService, checkableObjects);
	}

	/* ************************* private stuffs ************************* */

	private void checkPermissionsForAll(List<TestSuite> testSuites, String permission) {
		for (TestSuite testSuite : testSuites) {
			checkPermission(new SecurityCheckableObject(testSuite, permission));
		}

	}

	@Override
	public TestPlanStatistics getIterationStatistics(long iterationId) {
		return iterationDao.getIterationStatistics(iterationId);
	}

	@Override
	public List<IterationTestPlanItem> filterIterationForCurrentUser(long iterationId) {

		String userLogin = getCurrentUserLogin();
		List<IterationTestPlanItem> testPlanItemsToReturn = new ArrayList<IterationTestPlanItem>();

		Iteration iteration = iterationDao.findById(iterationId);

		List<IterationTestPlanItem> testPlanItems = iteration.getTestPlans();

		Long projectId = iteration.getProject().getId();

		if (projectsPermissionFinder.isInPermissionGroup(userLogin, projectId, "squashtest.acl.group.tm.TestRunner")) {

			for (IterationTestPlanItem testPlanItem : testPlanItems) {

				if (hasToBeReturned(testPlanItem, userLogin)) {
					testPlanItemsToReturn.add(testPlanItem);
				}
			}
		} else {
			testPlanItemsToReturn.addAll(testPlanItems);
		}

		return testPlanItemsToReturn;
	}

	private boolean hasToBeReturned(IterationTestPlanItem testPlanItem, String userLogin) {

		boolean hasToBeReturned = false;

		if (testPlanItem.getUser() != null && (testPlanItem.getUser().getLogin() == userLogin)) {

			hasToBeReturned = true;
		}

		return hasToBeReturned;
	}

	private String getCurrentUserLogin() {
		return userService.findCurrentUser().getLogin();
	}
}

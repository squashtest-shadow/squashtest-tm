/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException;
import org.squashtest.tm.service.campaign.CustomIterationModificationService;
import org.squashtest.tm.service.campaign.IterationStatisticsService;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.denormalizedField.PrivateDenormalizedFieldValueService;
import org.squashtest.tm.service.internal.library.PasteStrategy;
import org.squashtest.tm.service.internal.library.TreeNodeCopier;
import org.squashtest.tm.service.internal.repository.AutomatedSuiteDao;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.internal.testautomation.service.InsecureTestAutomationManagementService;
import org.squashtest.tm.service.library.AdvancedSearchService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;
import org.squashtest.tm.service.statistics.iteration.IterationStatisticsBundle;
import org.squashtest.tm.service.testcase.TestCaseCyclicCallChecker;

@Service("CustomIterationModificationService")
@Transactional
public class CustomIterationModificationServiceImpl implements CustomIterationModificationService,
		IterationTestPlanManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomIterationModificationServiceImpl.class);
	private static final String OR_HAS_ROLE_ADMIN = "or hasRole('ROLE_ADMIN')";
	private static final String PERMISSION_EXECUTE_ITERATION = "hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'EXECUTE') ";
	private static final String PERMISSION_EXECUTE_ITEM = "hasPermission(#testPlanItemId, 'org.squashtest.tm.domain.campaign.IterationTestPlanItem', 'EXECUTE') ";
	
	@Inject	private CampaignDao campaignDao;
	
	@Inject	private IterationDao iterationDao;
	
	@Inject	private TestSuiteDao suiteDao;
	
	@Inject	private IterationTestPlanDao testPlanDao;
	
	@Inject	private AutomatedSuiteDao autoSuiteDao;
	
	@Inject	private ExecutionDao executionDao;
	
	@Inject	private TestCaseCyclicCallChecker testCaseCyclicCallChecker;
	
	@Inject	private CampaignNodeDeletionHandler deletionHandler;
	
	@Inject	private PermissionEvaluationService permissionService;
	
	@Inject	private PrivateCustomFieldValueService customFieldValueService;
	
	@Inject	private PrivateDenormalizedFieldValueService denormalizedFieldValueService;
	
	@Inject private AdvancedSearchService advancedSearchService;

	@Inject private IterationStatisticsService statisticsService;
	
	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToIterationStrategy")
	private Provider<PasteStrategy<Iteration, TestSuite>> pasteToIterationStrategyProvider;

	@Inject	private ObjectFactory<TreeNodeCopier> treeNodeCopierFactory;
	
	@Inject	private IterationTestPlanManagerService iterationTestPlanManager;
	
	@Inject private InsecureTestAutomationManagementService testAutomationService;
	
	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public int addIterationToCampaign(Iteration iteration, long campaignId, boolean copyTestPlan) {
		Campaign campaign = campaignDao.findById(campaignId);

		// copy the campaign test plan in the iteration

		List<CampaignTestPlanItem> campaignTestPlan = campaign.getTestPlan();

		if (copyTestPlan) {
			populateTestPlan(iteration, campaignTestPlan);
		} 
		iterationDao.persistIterationAndTestPlan(iteration);
		campaign.addIteration(iteration);
		customFieldValueService.createAllCustomFieldValues(iteration);
		return campaign.getIterations().size() - 1;
	}

	/**
	 * populates an iteration's test plan from a campaign's test plan.
	 * 
	 * @param iteration
	 * @param campaignTestPlan
	 */
	private void populateTestPlan(Iteration iteration, List<CampaignTestPlanItem> campaignTestPlan) {
		for (CampaignTestPlanItem campaignItem : campaignTestPlan) {
			TestCase referenced = campaignItem.getReferencedTestCase();
			User assignee = campaignItem.getUser();

			Collection<IterationTestPlanItem> testPlanFragment = iterationTestPlanManager.createTestPlanFragment(
					referenced, assignee);

			appendFragmentToTestPlan(iteration, testPlanFragment);
		}
	}

	private void appendFragmentToTestPlan(Iteration iteration, Collection<IterationTestPlanItem> testPlanFragment) {
		for (IterationTestPlanItem item : testPlanFragment) {
			iteration.addTestPlan(item);
		}
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
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'WRITE') "
			+ OR_HAS_ROLE_ADMIN)
	public void rename(long iterationId, String newName) {
		Iteration iteration = iterationDao.findById(iterationId);

		iteration.setName(newName);
	}

	/**
	 * 
	 * @see org.squashtest.tm.service.campaign.CustomIterationModificationService#addExecution(long)
	 */
	@Override
	@PreAuthorize(PERMISSION_EXECUTE_ITEM + OR_HAS_ROLE_ADMIN)
	public Execution addExecution(long testPlanItemId) {
		IterationTestPlanItem item = testPlanDao.findTestPlanItem(testPlanItemId);

		return addExecution(item);
	}

	/**
	 * 
	 * @see org.squashtest.tm.service.campaign.CustomIterationModificationService#addAutomatedExecution(long)
	 */
	@Override
	@PreAuthorize(PERMISSION_EXECUTE_ITEM + OR_HAS_ROLE_ADMIN)
	public Execution addAutomatedExecution(long testPlanItemId) {
		IterationTestPlanItem item = testPlanDao.findTestPlanItem(testPlanItemId);

		return addAutomatedExecution(item);
	}

	@Override
	@PreAuthorize(PERMISSION_EXECUTE_ITERATION + OR_HAS_ROLE_ADMIN)
	public AutomatedSuite createAndStartAutomatedSuite(long iterationId) {
		Iteration iteration = iterationDao.findById(iterationId);
		List<IterationTestPlanItem> items =  iteration.getTestPlans();
		return createAndStartAutomatedSuite(items);
	}

	@Override
	@PreAuthorize(PERMISSION_EXECUTE_ITERATION + OR_HAS_ROLE_ADMIN)
	public AutomatedSuite createAndStartAutomatedSuite(long iterationId, Collection<Long> testPlanIds) {
		return createAndStartAutomatedSuiteByITPIsIds(testPlanIds);
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
	public OperationReport deleteNodes(List<Long> targetIds) {
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
	public OperationReport removeTestSuites(List<Long> suitesIds) {
		List<TestSuite> testSuites = suiteDao.findAllByIds(suitesIds);
		// check
		checkPermissionsForAll(testSuites, "DELETE");
		// proceed
		return deletionHandler.deleteSuites(suitesIds);

	}

	@Override
	public Execution addExecution(IterationTestPlanItem item) throws TestPlanItemNotExecutableException {

		testCaseCyclicCallChecker.checkNoCyclicCall(item.getReferencedTestCase());

		// if passes, let's move to the next step

		Execution execution = item.createExecution();

		// if we don't persist before we add, add will trigger an update of item.testPlan which fail because execution
		// has no id yet. this is caused by weird mapping (https://hibernate.onjira.com/browse/HHH-5732)
		executionDao.persist(execution);
		item.addExecution(execution);
		createDenormalizedFieldsForExecutionAndExecutionSteps(execution);
		advancedSearchService.reindexTestCase(item.getReferencedTestCase().getId());
		
		return execution;
	}

	private void createDenormalizedFieldsForExecutionAndExecutionSteps(Execution execution) {
		LOGGER.debug("Create denormalized fields for Execution {}", execution.getId());
		TestCase sourceTC = execution.getReferencedTestCase();
		denormalizedFieldValueService.createAllDenormalizedFieldValues(sourceTC, execution);
		for (ExecutionStep step : execution.getSteps()) {
			TestStep sourceStep = step.getReferencedTestStep();
			if (stepIsFromSameProjectAsTC(sourceTC, (ActionTestStep) sourceStep)) {
				denormalizedFieldValueService.createAllDenormalizedFieldValues((ActionTestStep) sourceStep, step);
			} else {
				denormalizedFieldValueService.createAllDenormalizedFieldValues((ActionTestStep) sourceStep, step,
						sourceTC.getProject());
			}
		}

	}

	private boolean stepIsFromSameProjectAsTC(TestCase sourceTC, ActionTestStep sourceStep) {
		return sourceStep.getProject().getId().equals(sourceTC.getProject().getId());
	}

	
	@Override
	public Execution addAutomatedExecution(IterationTestPlanItem item) throws TestPlanItemNotExecutableException {

		Execution execution = item.createAutomatedExecution();

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
	public AutomatedSuite createAndStartAutomatedSuite(List<IterationTestPlanItem> testPlanItems) {
		AutomatedSuite newSuite = autoSuiteDao.createNewSuite();

		for (IterationTestPlanItem item : testPlanItems) {
			if (item.isAutomated()) {
				Execution exec = addAutomatedExecution(item);
				newSuite.addExtender(exec.getAutomatedExecutionExtender());
			}
		}
		
		//See [Issue 1531]
		//We need to make sure that the AutomatedSuite has an id before we launch the execution.
		//Otherwise there is a risk that, if TA is too quick to complete execution, it will try to find and update the suite that doesn't have an id yet.
		TransactionSynchronizationManager.registerSynchronization(new AutomatedRunTransactionHandler(newSuite, testAutomationService));
		
		return newSuite;
	}
	
	@Override
	public AutomatedSuite createAndStartAutomatedSuiteByITPIsIds(Collection<Long> testPlanIds){
		List<IterationTestPlanItem> items = testPlanDao.findAllByIds(testPlanIds);
		return createAndStartAutomatedSuite(items);
	}

	@Override
	public List<Iteration> findIterationContainingTestCase(long testCaseId) {
		return iterationDao.findAllIterationContainingTestCase(testCaseId);
	}

	@Override
	public IterationStatisticsBundle gatherIterationStatisticsBundle(long iterationId) {
		return statisticsService.gatherIterationStatisticsBundle(iterationId);
	}

} 

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
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.TestPlanItemNotExecutableException;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.internal.repository.AutomatedSuiteDao;
import org.squashtest.csp.tm.internal.repository.CampaignDao;
import org.squashtest.csp.tm.internal.repository.ExecutionDao;
import org.squashtest.csp.tm.internal.repository.ItemTestPlanDao;
import org.squashtest.csp.tm.internal.repository.IterationDao;
import org.squashtest.csp.tm.internal.repository.TestSuiteDao;
import org.squashtest.csp.tm.internal.service.campaign.IterationTestPlanManager;
import org.squashtest.csp.tm.internal.testautomation.service.InsecureTestAutomationManagementService;
import org.squashtest.csp.tm.service.CustomIterationModificationService;
import org.squashtest.csp.tm.service.IterationTestPlanManagerService;
import org.squashtest.csp.tm.service.TestSuiteModificationService;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;

@Service("CustomIterationModificationService")
public class CustomIterationModificationServiceImpl implements CustomIterationModificationService,
		IterationTestPlanManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomIterationModificationServiceImpl.class);
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
	private InsecureTestAutomationManagementService automationService;
	
	
	@Inject
	private TestCaseCyclicCallChecker testCaseCyclicCallChecker;

	@Inject
	private CampaignNodeDeletionHandler deletionHandler;

	private PermissionEvaluationService permissionService;

	@Inject
	private IterationTestPlanManagerService iterationTestPlanManagerService;

	@Inject
	private TestSuiteModificationService testSuiteModificationService;

	@ServiceReference
	public void setPermissionService(PermissionEvaluationService permissionService) {
		this.permissionService = permissionService;
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign', 'CREATE') "
			+ "or hasRole('ROLE_ADMIN')")
	public int addIterationToCampaign(Iteration iteration, long campaignId) {
		Campaign campaign = campaignDao.findById(campaignId);

		// copy the campaign test plan in the iteration

		List<CampaignTestPlanItem> campaignTestPlan = campaign.getTestPlan();

		for (CampaignTestPlanItem campaignItem : campaignTestPlan) {
			IterationTestPlanItem iterationItem = new IterationTestPlanItem(campaignItem.getReferencedTestCase());
			iteration.addTestPlan(iterationItem);
		}
		iterationDao.persistIterationAndTestPlan(iteration);
		campaign.addIteration(iteration);
		return campaign.getIterations().size() - 1;
	}

	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.csp.tm.domain.campaign.Campaign', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public List<Iteration> findIterationsByCampaignId(long campaignId) {
		return campaignDao.findByIdWithInitializedIterations(campaignId).getIterations();
	}

	@Override
	@PostAuthorize("hasPermission(returnObject, 'READ') " + "or hasRole('ROLE_ADMIN')")
	public Iteration findById(long iterationId) {
		return iterationDao.findById(iterationId);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'DELETE') "
			+ "or hasRole('ROLE_ADMIN')")
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
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'SMALL_EDIT') "
			+ "or hasRole('ROLE_ADMIN')")
	public void rename(long iterationId, String newName) {
		Iteration iteration = iterationDao.findById(iterationId);

		iteration.setName(newName);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'EXECUTE') "
			+ "or hasRole('ROLE_ADMIN')")
	public Execution addExecution(long iterationId, long testPlanId) {

		Iteration iteration = iterationDao.findAndInit(iterationId);
		IterationTestPlanItem item = iteration.getTestPlan(testPlanId);

		return addExecution(item);
	}
	
	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'EXECUTE') "
			+ "or hasRole('ROLE_ADMIN')")
	public Execution addAutomatedExecution(long iterationId, long testPlanId) {
		
		Iteration iteration = iterationDao.findAndInit(iterationId);
		IterationTestPlanItem item = iteration.getTestPlan(testPlanId);
		
		return addAutomatedExecution(item);
	}
	


	@Override	
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'EXECUTE') "
			+ "or hasRole('ROLE_ADMIN')")
	public AutomatedSuite createAndExecuteAutomatedSuite(long iterationId) {
		
		AutomatedSuite newSuite = autoSuiteDao.createNewSuite();
		
		Iteration iteration = iterationDao.findById(iterationId);
		
		for (IterationTestPlanItem item : iteration.getTestPlans()){
			if (item.isAutomated()){
				Execution exec = addAutomatedExecution(item);
				newSuite.addExtender(exec.getAutomatedExecutionExtender());
			}			
		}
		
		
		automationService.startAutomatedSuite(newSuite);
		
		return newSuite;
	}

	
	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'EXECUTE') "
			+ "or hasRole('ROLE_ADMIN')")
	public AutomatedSuite createAndExecuteAutomatedSuite(long iterationId, Collection<Long> testPlanIds) {
		
		AutomatedSuite newSuite = autoSuiteDao.createNewSuite();
		
		List<IterationTestPlanItem> items = testPlanDao.findAllByIds(testPlanIds);
		
		for (IterationTestPlanItem item : items){
			if (item.isAutomated()){
				Execution exec = addAutomatedExecution(item);
				newSuite.addExtender(exec.getAutomatedExecutionExtender());
			}
			
		}

		automationService.startAutomatedSuite(newSuite);
		
		return newSuite;
		
	}


	/****
	 * Method which change the index of test case in the selected iteration
	 * 
	 * @param iterationId
	 *            the iteration at which the test case is attached
	 * @param testCaseId
	 *            the test case to move
	 * @param newTestCasePosition
	 *            the test case new position
	 */
	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'LINK') "
			+ "or hasRole('ROLE_ADMIN')")
	@Deprecated
	public void changeTestPlanPosition(long iterationId, long testPlanId, int newTestPlanPosition) {

		Iteration iteration = iterationDao.findById(iterationId);

		int currentPosition = iteration.findItemIndexInTestPlan(testPlanId);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("**************** change test case order : old index = " + currentPosition + ",new index : "
					+ newTestPlanPosition);
		}

		iteration.moveTestPlan(currentPosition, newTestPlanPosition);

	}

	/**
	 * @see CustomIterationModificationService#changeTestPlanPosition(long, int, List)
	 */
	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'LINK') "
			+ "or hasRole('ROLE_ADMIN')")
	public void changeTestPlanPosition(long iterationId, int newPosition, List<Long> itemIds) {
		Iteration iteration = iterationDao.findById(iterationId);
		List<IterationTestPlanItem> items = testPlanDao.findAllByIds(itemIds);

		iteration.moveTestPlans(newPosition, items);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public List<Execution> findAllExecutions(long iterationId) {
		return iterationDao.findOrderedExecutionsByIterationId(iterationId);

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public List<Execution> findExecutionsByTestPlan(long iterationId, long testPlanId) {
		return iterationDao.findOrderedExecutionsByIterationAndTestPlan(iterationId, testPlanId);

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
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
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'CREATE') "
			+ "or hasRole('ROLE_ADMIN')")
	public void addTestSuite(long iterationId, TestSuite suite) {
		Iteration iteration = iterationDao.findById(iterationId);
		addTestSuite(iteration, suite);
	}

	@Override
	public void addTestSuite(Iteration iteration, TestSuite suite) {
		suiteDao.persist(suite);
		iteration.addTestSuite(suite);

	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public List<TestSuite> findAllTestSuites(long iterationId) {
		return iterationDao.findAllTestSuites(iterationId);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'CREATE') "
			+ "or hasRole('ROLE_ADMIN')")
	public TestSuite copyPasteTestSuiteToIteration(Long testSuiteId, Long iterationId) {
		TestSuite testSuite = suiteDao.findById(testSuiteId);

		List<IterationTestPlanItem> copyOfTestPlan = testSuite.createPastableCopyOfTestPlan();
		TestSuite copyOfTestSuite = testSuite.createPastableCopy();

		iterationTestPlanManagerService.addTestPlanToIteration(copyOfTestPlan, iterationId);

		renameTestSuiteIfNecessary(copyOfTestSuite, iterationId);
		addTestSuite(iterationId, copyOfTestSuite);
		List<Long> itemTestPlanIds = listTestPlanIds(copyOfTestPlan);

		testSuiteModificationService.bindTestPlan(copyOfTestSuite.getId(), itemTestPlanIds);

		return copyOfTestSuite;
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'CREATE') "
			+ "or hasRole('ROLE_ADMIN')")
	public List<TestSuite> copyPasteTestSuitesToIteration(Long[] testSuiteIds, Long iterationId) {

		List<TestSuite> createdTestSuites = new ArrayList<TestSuite>();

		for (Long testSuiteId : testSuiteIds) {
			createdTestSuites.add(copyPasteTestSuiteToIteration(testSuiteId, iterationId));
		}

		return createdTestSuites;
	}

	private void renameTestSuiteIfNecessary(TestSuite copyOfTestSuite, Long iterationId) {
		Iteration iteration = iterationDao.findById(iterationId);
		String originalSuiteName = copyOfTestSuite.getName();
		String newName = originalSuiteName;
		int numberOfCopy = 0;
		while (!iteration.checkSuiteNameAvailable(newName)) {
			numberOfCopy++;
			newName = originalSuiteName + AbstractLibraryNavigationService.COPY_TOKEN + numberOfCopy;
		}
		copyOfTestSuite.setName(newName);
	}

	private List<Long> listTestPlanIds(List<IterationTestPlanItem> copyOfTestPlan) {
		List<Long> ids = new LinkedList<Long>();
		for (IterationTestPlanItem iterationTestPlanItem : copyOfTestPlan) {
			ids.add(iterationTestPlanItem.getId());
		}
		return ids;
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
		// if we dont persist before we add, add will trigger an update of item.testPlan which fail because execution
		// has no id yet. this is caused by weird mapping (https://hibernate.onjira.com/browse/HHH-5732)
		executionDao.persist(execution);
		item.addExecution(execution);

		return execution;
	}
	
	public Execution addAutomatedExecution(IterationTestPlanItem item) throws TestPlanItemNotExecutableException {
		
		Execution execution = item.createAutomatedExecution(testCaseCyclicCallChecker);
		
		executionDao.persist(execution);
		item.addExecution(execution);
		
		return execution;
		
	}
	


	/* ************************* security ************************* */

	/* **that class just performs the same, using a domainObject directly */
	private static class SecurityCheckableObject {
		private final Object domainObject;
		private final String permission;

		private SecurityCheckableObject(Object domainObject, String permission) {
			this.domainObject = domainObject;
			this.permission = permission;
		}

		public String getPermission() {
			return permission;
		}

		public Object getObject() {
			return domainObject;
		}

	}

	private void checkPermission(SecurityCheckableObject... checkableObjects) {
		for (SecurityCheckableObject object : checkableObjects) {
			if (!permissionService
					.hasRoleOrPermissionOnObject("ROLE_ADMIN", object.getPermission(), object.getObject())) {
				throw new AccessDeniedException("Access is denied");
			}
		}
	}

	/* ************************* private stuffs ************************* */

	private void checkPermissionsForAll(List<TestSuite> testSuites, String permission) {
		for (TestSuite testSuite : testSuites) {
			checkPermission(new SecurityCheckableObject(testSuite, permission));
		}

	}



}

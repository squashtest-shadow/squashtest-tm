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
package org.squashtest.tm.service.internal.library;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.library.Copiable;
import org.squashtest.tm.domain.library.Folder;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.NodeVisitor;
import org.squashtest.tm.domain.library.TreeNode;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepVisitor;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.internal.campaign.IterationTestPlanManager;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.CampaignFolderDao;
import org.squashtest.tm.service.internal.repository.EntityDao;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.internal.repository.RequirementFolderDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionCoverageDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;

@Component
@Scope("prototype")
public class TreeNodeCopier  implements NodeVisitor, PasteOperation {
	@Inject
	private RequirementDao requirementDao;
	@Inject
	private RequirementFolderDao requirementFolderDao;
	@Inject
	private TestCaseDao testCaseDao;
	@Inject
	private TestCaseFolderDao testCaseFolderDao;
	@Inject
	private CampaignDao campaignDao;
	@Inject
	private CampaignFolderDao campaignFolderDao;
	@Inject
	private IterationDao iterationDao;
	@Inject
	private TestSuiteDao testSuiteDao;
	@Inject
	private IterationTestPlanDao iterationTestPlanItemDao;
	@Inject
	private IterationTestPlanManager iterationTestPlanManager;
	@Inject
	private PrivateCustomFieldValueService customFieldValueManagerService;
	@Inject
	private TreeNodeUpdater treeNodeUpdater;
	@Inject
	private PermissionEvaluationService permissionService;
	@Inject
	private RequirementVersionCoverageDao requirementVersionCoverageDao;
	@Inject
	private IndexationService indexationService;

	private NodeContainer<? extends TreeNode> destination;
	private TreeNode copy;
	private boolean okToGoDeeper = true;
	private boolean projectChanged = true;

	public TreeNode performOperation(TreeNode source, NodeContainer<TreeNode> destination) {
		PermissionsUtils.checkPermission(permissionService, new SecurityCheckableObject(destination, "CREATE"),
				new SecurityCheckableObject(source, "READ"));
		this.okToGoDeeper = true;
		this.destination = destination;
		this.projectChanged = projectchanged(source);
		copy = null;
		source.accept(this);
		if(projectChanged){
			copy.accept(treeNodeUpdater);
		}
		return copy;
		
		
	}

	public TreeNode performOperation(TreeNode source, NodeContainer<TreeNode> destination, int position) {
		PermissionsUtils.checkPermission(permissionService, new SecurityCheckableObject(destination, "CREATE"),
				new SecurityCheckableObject(source, "READ"));
		this.okToGoDeeper = true;
		this.destination = destination;
		this.projectChanged = projectchanged(source);
		copy = null;
		source.accept(this);
		if(projectChanged){
			copy.accept(treeNodeUpdater);
		}
		return copy;
		
		
	}
	
	private boolean projectchanged(TreeNode source) {
		Project projectSource = source.getProject();
		GenericProject projectDestination = destination.getProject();
		return (projectSource !=null && projectDestination != null && !projectSource.getId().equals(projectDestination.getId()));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void visit(Folder source, FolderDao dao) {
		Folder<?> copyFolder = (Folder<?>) source.createCopy();
		persistCopy(copyFolder, dao);
		
	}

	@Override
	public void visit(Campaign source) {
		Campaign copyCampaign = source.createCopy();
		persistCopy(copyCampaign, campaignDao);
		copyCustomFields(source, copyCampaign);
		
	}

	/**
	 * Will copy paste the iteration with it's test-suites.<br>
	 * Hence it is not ok to go deeper with the paste strategy : we don't want the test-suites to be copied twice.<br>
	 * <br>
	 * <h4>Why don't we use the paste strategy to go through the test-suites ?</h4> Because of functional rules : the requirements for
	 * "copying an test-suite alone" and "copying test-suites of a copied iteration" are different.
	 * <ol>
	 * <li>When copying a test-suite alone all test-plan item of the concerned test-suite will be added to the iteration
	 * even if there were already contained by the iteration.</li>
	 * <li>When copying an interaction, it's copied test-suite should be bound to the already copied
	 * iteration-test-plan-items.</li>
	 * </ol>
	 */
	@Override
	public void visit(Iteration source) {
		Iteration copyIteration = source.createCopy();
		persitIteration(copyIteration);
		copyCustomFields(source, copyIteration);
		copyIterationTestSuites(source, copyIteration);
		this.okToGoDeeper = false;
		if(projectChanged){
			for(TestSuite suite : source.getTestSuites()){
				suite.accept(treeNodeUpdater);
			}
		}

	}

	@Override
	public void visit(TestSuite source) {
		TestSuite copyTestSuite = source.createCopy();
		persistCopy(copyTestSuite, testSuiteDao);
		copyCustomFields(source, copyTestSuite);
		copyTestSuiteTestPlanToDestinationIteration(source, copyTestSuite);
		
	}

	private void copyTestSuiteTestPlanToDestinationIteration(TestSuite source, TestSuite copy) {
		Iteration iteration = (Iteration) destination;
		List<IterationTestPlanItem> copyOfTestPlan = source.createPastableCopyOfTestPlan();
		for (IterationTestPlanItem itp : copyOfTestPlan) {
			iterationTestPlanItemDao.persist(itp);
			iteration.addTestPlan(itp);
		}
		copy.bindTestPlanItems(copyOfTestPlan);
	}

	@Override
	public void visit(Requirement source) {
		//copy simple attributes of requirement
		Requirement copyRequirement = source.createCopy();
		//create copies for requirement versions and remember version's sources
		TreeMap<RequirementVersion, RequirementVersion> previousVersionsCopiesBySources = source
				.addPreviousVersionsCopiesToCopy(copyRequirement);
		persistCopy(copyRequirement, requirementDao);
		//copy custom fields and requirement-version coverages for Current Version
		copyCustomFields(source.getCurrentVersion(), copyRequirement.getCurrentVersion());
		copyRequirementVersionCoverages(source.getCurrentVersion(), copyRequirement.getCurrentVersion());
		//copy custom fields and requirement-version coverages for older versions
		for (Entry<RequirementVersion, RequirementVersion> previousVersionCopyBySource : previousVersionsCopiesBySources
				.entrySet()) {
			//retrieve entities from entry
			RequirementVersion sourceVersion = previousVersionCopyBySource.getKey();
			RequirementVersion copyVersion = previousVersionCopyBySource.getValue();
			//copy cufs and coverages for entities
			copyRequirementVersionCoverages(sourceVersion, copyVersion);
			copyCustomFields(sourceVersion, copyVersion);
		}
	}
	
	@Override
	public void visit(TestCase source) {
		TestCase copyTestCase = source.createCopy();
		persistTestCase(copyTestCase);		
		copyCustomFields(source, copyTestCase);		
		copyRequirementVersionCoverage(source, copyTestCase);
	}
	

	@Override
	public void visit(CampaignFolder campaignFolder) {
		visit(campaignFolder, campaignFolderDao);
		
	}

	@Override
	public void visit(RequirementFolder requirementFolder) {
		visit(requirementFolder, requirementFolderDao);
		
	}

	@Override
	public void visit(TestCaseFolder testCaseFolder) {
		visit(testCaseFolder, testCaseFolderDao);
		
	}

	/**************************************************** PRIVATE **********************************************************/

	private void copyIterationTestSuites(Iteration originalIteration, Iteration iterationCopy) {
		Map<TestSuite, List<Integer>> testSuitesPastableCopiesMap = originalIteration.createTestSuitesPastableCopy();
		for (Entry<TestSuite, List<Integer>> testSuitePastableCopyEntry : testSuitesPastableCopiesMap.entrySet()) {
			TestSuite testSuiteCopy = testSuitePastableCopyEntry.getKey();
			iterationTestPlanManager.addTestSuite(iterationCopy, testSuiteCopy);
			bindTestPlanOfCopiedTestSuite(iterationCopy, testSuitePastableCopyEntry, testSuiteCopy);
		}
	}

	private void bindTestPlanOfCopiedTestSuite(Iteration iterationCopy,
			Entry<TestSuite, List<Integer>> testSuitePastableCopyEntry, TestSuite testSuiteCopy) {
		List<Integer> testSuiteTpiIndexesInIterationList = testSuitePastableCopyEntry.getValue();
		List<IterationTestPlanItem> testPlanItemsToBind = new ArrayList<IterationTestPlanItem>();
		List<IterationTestPlanItem> iterationTestPlanCopy = iterationCopy.getTestPlans();
		for (Integer testSuiteTpiIndexInIterationList : testSuiteTpiIndexesInIterationList) {
			IterationTestPlanItem testPlanItemToBind = iterationTestPlanCopy.get(testSuiteTpiIndexInIterationList);
			testPlanItemsToBind.add(testPlanItemToBind);
		}
		testSuiteCopy.bindTestPlanItems(testPlanItemsToBind);
	}

	/**
	 * @see PrivateCustomFieldValueService#copyCustomFieldValues(BoundEntity, BoundEntity)
	 */
	private void copyCustomFields(BoundEntity source, BoundEntity copy) {
		customFieldValueManagerService.copyCustomFieldValues(source, copy);
	}

	private void copyCustomFields(TestCase source, TestCase copy) {
		customFieldValueManagerService.copyCustomFieldValues(source, copy);
		// do the same for the steps if any
		int total = copy.getSteps().size();
		for (int i = 0; i < total; i++) {
			TestStep copyStep = copy.getSteps().get(i);
			TestStep sourceStep = source.getSteps().get(i);
			copyStep.accept(new TestStepCufCopier(customFieldValueManagerService, sourceStep));
		}
	}

	private static final class TestStepCufCopier implements TestStepVisitor {
		private PrivateCustomFieldValueService customFieldValueManagerService;
		private TestStep sourceStep;

		private TestStepCufCopier(PrivateCustomFieldValueService customFieldValueManagerService, TestStep sourceStep) {
			this.customFieldValueManagerService = customFieldValueManagerService;
			this.sourceStep = sourceStep;
		}

		@Override
		public void visit(ActionTestStep visited) {
			customFieldValueManagerService.copyCustomFieldValues((ActionTestStep) sourceStep, visited);

		}

		@Override
		public void visit(CallTestStep visited) {
			// do nothing

		}

	}

	@SuppressWarnings("unchecked")
	private <T extends TreeNode> void persistCopy(T copyParam, EntityDao<T> dao) {
		renameIfNeeded((Copiable) copyParam);
		dao.persist(copyParam);
		((NodeContainer<T>) destination).addContent(copyParam);
		this.copy = copyParam;
	}

	@SuppressWarnings("unchecked")
	private void persistTestCase(TestCase testCase) {
		renameIfNeeded(testCase);
		testCaseDao.persistTestCaseAndSteps(testCase);
		((NodeContainer<TestCase>) destination).addContent(testCase);
		this.copy = testCase;
	}

	@SuppressWarnings("unchecked")
	private void persitIteration(Iteration copyParam) {
		renameIfNeeded((Copiable) copyParam);
		iterationDao.persistIterationAndTestPlan(copyParam);
		((NodeContainer<Iteration>) destination).addContent(copyParam);
		this.copy = copyParam;
	}

	private <T extends Copiable> void renameIfNeeded(T copyParam) {
		if (!destination.isContentNameAvailable(copyParam.getName())) {
			String newName = LibraryUtils.generateUniqueCopyName(destination.getContentNames(), copyParam.getName());
			copyParam.setName(newName);
		}
	}

	@Override
	public boolean isOkToGoDeeper() {
		return this.okToGoDeeper;
	}
	
	private void copyRequirementVersionCoverages(RequirementVersion sourceVersion, RequirementVersion copyVersion) {
		List<RequirementVersionCoverage> copies = sourceVersion.createRequirementVersionCoveragesForCopy(copyVersion);
		indexRequirementVersionCoverageCopies(copies);
		requirementVersionCoverageDao.persist(copies);
		
	}

	private void indexRequirementVersionCoverageCopies(List<RequirementVersionCoverage> copies) {
		for(RequirementVersionCoverage copy : copies){
			indexationService.reindexTestCase(copy.getVerifyingTestCase().getId());
			indexationService.reindexRequirementVersion(copy.getVerifiedRequirementVersion().getId());
		}
	}

	private void copyRequirementVersionCoverage(TestCase source, TestCase copyTestCase) {
		List<RequirementVersionCoverage> copies = source.createRequirementVersionCoveragesForCopy(copyTestCase);
		indexRequirementVersionCoverageCopies(copies);
		requirementVersionCoverageDao.persist(copies);
	}

}

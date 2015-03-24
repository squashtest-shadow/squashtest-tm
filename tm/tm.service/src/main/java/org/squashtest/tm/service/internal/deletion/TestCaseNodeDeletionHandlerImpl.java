/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.internal.deletion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.NamedReference;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.library.structures.LibraryGraph;
import org.squashtest.tm.domain.library.structures.LibraryGraph.SimpleNode;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.deletion.BoundToLockedMilestonesReport;
import org.squashtest.tm.service.deletion.BoundToMultipleMilestonesReport;
import org.squashtest.tm.service.deletion.LinkedToIterationPreviewReport;
import org.squashtest.tm.service.deletion.MilestoneModeNoFolderDeletion;
import org.squashtest.tm.service.deletion.NotDeletablePreviewReport;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.deletion.LockedFileInferenceGraph.Node;
import org.squashtest.tm.service.internal.repository.AutomatedTestDao;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestCaseDeletionDao;
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao;
import org.squashtest.tm.service.internal.testcase.TestCaseCallTreeFinder;
import org.squashtest.tm.service.internal.testcase.TestCaseNodeDeletionHandler;
import org.squashtest.tm.service.testcase.DatasetModificationService;
import org.squashtest.tm.service.testcase.ParameterModificationService;
import org.squashtest.tm.service.testcase.TestCaseImportanceManagerService;

@Component("squashtest.tm.service.deletion.TestCaseNodeDeletionHandler")
@Transactional
public class TestCaseNodeDeletionHandlerImpl extends
AbstractNodeDeletionHandler<TestCaseLibraryNode, TestCaseFolder> implements TestCaseNodeDeletionHandler {

	@Inject
	private TestCaseFolderDao folderDao;

	@Inject
	private TestCaseDao leafDao;

	@Inject
	private TestCaseDeletionDao deletionDao;
	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;

	@Inject
	private DatasetModificationService datasetService;

	@Inject
	private ParameterModificationService parameterService;

	@Inject
	private PrivateCustomFieldValueService customValueService;

	@Inject
	private TestCaseCallTreeFinder calltreeFinder;

	@Inject
	private AutomatedTestDao autoTestDao;

	@Override
	protected FolderDao<TestCaseFolder, TestCaseLibraryNode> getFolderDao() {
		return folderDao;
	}

	/* *******************************************************************************
	 * 
	 * DELETION SIMULATION
	 * 
	 * ******************************************************************************/

	@Override
	protected List<SuppressionPreviewReport> diagnoseSuppression(List<Long> nodeIds, Long milestoneId) {

		List<SuppressionPreviewReport> preview = new LinkedList<SuppressionPreviewReport>();

		// check test cases that are called by other test cases
		NotDeletablePreviewReport report = previewLockedNodes(nodeIds, milestoneId);
		if (report != null){
			preview.add(report);
		}

		// check test cases that have been planned or executed
		LinkedToIterationPreviewReport previewAffectedNodes = previewAffectedNodes(nodeIds);
		if (previewAffectedNodes != null){
			preview.add(previewAffectedNodes);
		}

		// milestone mode only :
		if (milestoneId != null){

			// check if some elements belong to milestones which status forbids that
			if (someNodesAreLockedByMilestones(nodeIds)){
				preview.add(new BoundToLockedMilestonesReport());
			}

			// check if there are some folders in the selection
			List<Long>[] separatedIds = deletionDao.separateFolderFromTestCaseIds(nodeIds);
			if (! separatedIds[0].isEmpty()){
				preview.add(new MilestoneModeNoFolderDeletion());
			}

			// check if some elements are bound to multiple milestones
			if (someNodesHaveMultipleMilestones(nodeIds)){
				preview.add(new BoundToMultipleMilestonesReport());
			}

		}

		return preview;

	}



	/* *******************************************************************************
	 * 
	 * ACTUAL DELETION
	 * 
	 * ******************************************************************************/


	@Override
	protected List<Long> detectLockedNodes(final List<Long> nodeIds, Long milestoneId) {

		List<Long> lockedCandidateIds = new ArrayList<Long>();

		List<Long> lockedByMilestoneRule = new ArrayList<>();

		List<Long> candidateIds = new ArrayList<>(nodeIds);


		/*
		 * Find test cases that cannot be removed because of milestones. Note that
		 * the definition of "locked by milestone" may change depending on whether the
		 * user actually uses the milestone mode or not. Also the test cases are de facto
		 * removed from the test case call graph before computation.
		 */
		if (milestoneId  == null){
			lockedByMilestoneRule = lockedByMilestoneNormalMode(nodeIds);
		}
		else {
			lockedByMilestoneRule = lockedByMilestoneMilestoneMode(nodeIds, milestoneId);
		}

		lockedCandidateIds.addAll(lockedByMilestoneRule);

		candidateIds.removeAll(lockedByMilestoneRule);


		// now init the graph with test case calls
		LockedFileInferenceGraph graph = createCallTestCaseGraph(candidateIds);

		graph.setCandidatesToDeletion(candidateIds);
		graph.resolveLockedFiles();

		List<Node> lockedCandidates = graph.collectLockedCandidates();


		for (Node node : lockedCandidates) {
			lockedCandidateIds.add(node.getKey().getId());
		}


		return lockedCandidateIds;

	}

	@Override
	/*
	 * Will batch-remove some TestCaseLibraryNodes. Since we may have to delete lots of nested entities we cannot afford
	 * to use Hibernate orm abilities : you don't want one fetch-query per entity or the DB admin from hell will eat
	 * you.
	 * 
	 * Note : We only need to take care of the attachments and steps, the rest will cascade thanks to the ON CASCADE
	 * clauses in the other tables.
	 */
	protected OperationReport batchDeleteNodes(List<Long> ids, Long milestoneId) {

		OperationReport report = new OperationReport();

		if (!ids.isEmpty()) {

			DeletableIds deletableIds = findSeparateIds(ids);


			List<Long> folderIds = deletableIds.getFolderIds();
			List<Long> tcIds = deletableIds.getTestCaseIds();
			List<Long> allIds = deletableIds.getAllIds();

			List<Long> stepIds = deletionDao.findTestSteps(tcIds);

			List<Long> testCaseAttachmentIds = 	deletionDao.findTestCaseAttachmentListIds(tcIds);
			List<Long> testStepAttachmentIds = 	deletionDao.findTestStepAttachmentListIds(stepIds);
			List<Long> testCaseFolderAttachmentIds = deletionDao.findTestCaseFolderAttachmentListIds(folderIds);

			deletionDao.removeCampaignTestPlanInboundReferences(tcIds);
			deletionDao.removeOrSetIterationTestPlanInboundReferencesToNull(tcIds);

			deletionDao.setExecutionInboundReferencesToNull(tcIds);
			deletionDao.setExecStepInboundReferencesToNull(stepIds);

			deletionDao.removeFromVerifyingTestStepsList(stepIds);
			deletionDao.removeFromVerifyingTestCaseLists(tcIds);

			customValueService.deleteAllCustomFieldValues(BindableEntity.TEST_STEP, stepIds);
			deletionDao.removeAllSteps(stepIds);

			customValueService.deleteAllCustomFieldValues(BindableEntity.TEST_CASE, tcIds);

			datasetService.removeAllByTestCaseIds(tcIds);
			parameterService.removeAllByTestCaseIds(tcIds);

			deletionDao.removeEntities(allIds);

			// We merge the attachment list ids for
			// test cases, test step and folder first so that
			// we can make one only one query against the database.
			testCaseAttachmentIds.addAll(testStepAttachmentIds);
			testCaseAttachmentIds.addAll(testCaseFolderAttachmentIds);
			deletionDao.removeAttachmentsLists(testCaseAttachmentIds);

			report.addRemoved(folderIds, "folder");
			report.addRemoved(tcIds, "test-case");

			// Last, take care of the automated tests that could end up as "orphans" after the mass deletion
			autoTestDao.pruneOrphans();

		}

		return report;
	}


	protected OperationReport batchUnbindFromMilestone(List<Long> ids, Long milestoneId){

		List<Long> remainingIds = deletionDao.findRemainingTestCaseIds(ids);

		// some node should not be unbound.
		List<Long> lockedIds = deletionDao.findTestCasesWhichMilestonesForbidsDeletion(remainingIds);
		remainingIds.removeAll(lockedIds);

		OperationReport report = new OperationReport();

		deletionDao.unbindFromMilestone(remainingIds, milestoneId);

		report.addRemoved(remainingIds, "test-case");

		return report;

	}


	/* ************************ TestCaseNodeDeletionHandler impl ***************************** */

	/*
	 * deleting a test step means : - delete its attachments, - delete itself.
	 */
	@Override
	public void deleteStep(TestCase owner, TestStep step) {

		int index = owner.getPositionOfStep(step.getId());

		if (index == -1) {
			return;
		}

		owner.getSteps().remove(index);

		List<Long> stepId = new LinkedList<Long>();
		stepId.add(step.getId());
		deletionDao.setExecStepInboundReferencesToNull(stepId);

		if (step instanceof ActionTestStep) {
			customValueService.deleteAllCustomFieldValues((ActionTestStep) step);
			deleteActionStep((ActionTestStep) step);
			customValueService.deleteAllCustomFieldValues((ActionTestStep) step);
		} else if (step instanceof CallTestStep) {
			CallTestStep callTestStep = (CallTestStep) step;
			deleteCallStep(callTestStep);
			testCaseImportanceManagerService.changeImportanceIfCallStepRemoved(callTestStep.getCalledTestCase(), owner);
		}
	}


	/* ************************ privates stuffs ************************ */

	/*
	 * note : the supposedly 'private' methods are labelled as 'protected' instead, so that reflexive-based
	 * test-frameworks (such as Groovy+Spock) can access them.
	 */

	/*
	 * a node will be deletable if : - it has no deletion-related constraints, - the node has constraints but they are
	 * being deleted too.
	 */
	protected NotDeletablePreviewReport previewLockedNodes(List<Long> nodeIds, Long milestoneId) {

		NotDeletablePreviewReport report = null;


		List<Long> candidateIds = new ArrayList<>(nodeIds);

		// find the nodes locked by milestone. The rules differ depending on
		// whether the milestone mode is on or not
		List<Long> lockedByMilestoneRule;
		if (milestoneId == null){
			lockedByMilestoneRule = lockedByMilestoneNormalMode(nodeIds);
		}
		else{
			lockedByMilestoneRule = lockedByMilestoneMilestoneMode(nodeIds, milestoneId);
		}

		candidateIds.removeAll(lockedByMilestoneRule);

		// compute the graph of called test cases
		LockedFileInferenceGraph graph = createCallTestCaseGraph(candidateIds);

		graph.setCandidatesToDeletion(candidateIds);
		graph.resolveLockedFiles();

		// when nonDeletableData is not empty, some of those nodes belongs to
		// the deletion request itself
		// and the other ones are those that still need to be deleted.

		if (graph.hasLockedFiles()) {

			report = new NotDeletablePreviewReport();

			for (Node node : graph.collectLockedCandidates()) {
				report.addName(node.getName());
			}

			for (Node node : graph.collectLockers()) {
				report.addWhy(node.getName());
			}

		}

		return report;
	}


	protected LockedFileInferenceGraph createCallTestCaseGraph(List<Long> candidatesId) {

		LibraryGraph<NamedReference, SimpleNode<NamedReference>> calltree = calltreeFinder.getCallerGraph(candidatesId);

		LockedFileInferenceGraph graph = new LockedFileInferenceGraph();
		graph.init(calltree);

		return graph;
	}

	private LinkedToIterationPreviewReport previewAffectedNodes(List<Long> nodeIds) {

		LinkedToIterationPreviewReport report = null;

		List<TestCase> linkedNodes = leafDao.findAllLinkedToIteration(nodeIds);
		if (!linkedNodes.isEmpty()) {

			report = new LinkedToIterationPreviewReport();

			for (TestCase node : linkedNodes) {
				report.addName(node.getName());
			}


		}

		return report;
	}

	private void deleteActionStep(ActionTestStep step) {
		deletionDao.removeAttachmentList(step.getAttachmentList());
		deletionDao.removeEntity(step);
	}

	private void deleteCallStep(CallTestStep step) {
		deletionDao.removeEntity(step);
	}

	private boolean someNodesHaveMultipleMilestones(List<Long> nodeIds){
		List<Long> boundNodes = leafDao.findNodeIdsHavingMultipleMilestones(nodeIds);
		return ! (boundNodes.isEmpty());
	}

	private boolean someNodesAreLockedByMilestones(List<Long> nodeIds){
		List<Long> lockedNodes = deletionDao.findTestCasesWhichMilestonesForbidsDeletion(nodeIds);
		return ! (lockedNodes.isEmpty());
	}


	private DeletableIds findSeparateIds(List<Long> ids){
		List<Long>[] separatedIds = deletionDao.separateFolderFromTestCaseIds(ids);
		return new DeletableIds(separatedIds[0], separatedIds[1]);
	}



	/*
	 * Just find the test cases locked by non deletable milestones. This is used
	 * even in normal mode.
	 * 
	 */
	private List<Long> lockedByMilestoneNormalMode(List<Long> nodeIds){
		return deletionDao.findTestCasesWhichMilestonesForbidsDeletion(nodeIds);
	}

	/*
	 * milestone mode :
	 * - 1) no folder shall be deleted (enqueued outright)
	 * - 2) no test case that doesn't belong to the milestone shall be deleted (needed for graph resolution)
	 * - 3) no test case bound to more than one milestone shall be deleted (need for graph resolution) (they will be unbound though, but later).
	 * - 4) no test case bound to a milestone which status forbids deletion shall be deleted.
	 */
	private List<Long> lockedByMilestoneMilestoneMode(List<Long> nodeIds, Long milestoneId){

		List<Long> folderIds = deletionDao.separateFolderFromTestCaseIds(nodeIds)[0];
		List<Long> outOfMilestone = leafDao.findNonBoundTestCases(nodeIds, milestoneId);
		List<Long> belongsToMoreMilestones = leafDao.findNodeIdsHavingMultipleMilestones(nodeIds);
		List<Long> lockedByMilestones = deletionDao.findTestCasesWhichMilestonesForbidsDeletion(nodeIds);

		List<Long> milestoneLocked = new ArrayList<>(folderIds.size()+outOfMilestone.size()+belongsToMoreMilestones.size()+lockedByMilestones.size());

		milestoneLocked.addAll(folderIds);
		milestoneLocked.addAll(outOfMilestone);
		milestoneLocked.addAll(belongsToMoreMilestones);
		milestoneLocked.addAll(lockedByMilestones);

		return milestoneLocked;
	}


	private static final class DeletableIds {
		private final List<Long> folderIds;
		private final List<Long> testCaseIds;

		public DeletableIds(List<Long> folderIds, List<Long> testCaseIds) {
			super();
			this.folderIds = folderIds;
			this.testCaseIds = testCaseIds;
		}

		public List<Long> getFolderIds() {
			return folderIds;
		}

		public List<Long> getTestCaseIds() {
			return testCaseIds;
		}

		public List<Long> getAllIds(){
			ArrayList<Long> all = new ArrayList<>(folderIds.size() + testCaseIds.size());
			all.addAll(folderIds);
			all.addAll(testCaseIds);
			return all;
		}


	}

}

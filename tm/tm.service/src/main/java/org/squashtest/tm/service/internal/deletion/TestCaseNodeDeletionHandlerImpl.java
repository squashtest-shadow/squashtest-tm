/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.deletion;

import java.util.ArrayList;
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
import org.squashtest.tm.service.deletion.LinkedToIterationPreviewReport;
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

	/*
	 * ************************************ AbstractNodeDeletionHandlerImpl impl
	 * *****************************************
	 */

	@Override
	protected List<SuppressionPreviewReport> diagnoseSuppression(List<Long> nodeIds) {
		List<SuppressionPreviewReport> preview = new LinkedList<SuppressionPreviewReport>();

		NotDeletablePreviewReport report = previewLockedNodes(nodeIds);
		if(report != null){
			preview.add(report);
		}
		LinkedToIterationPreviewReport previewAffectedNodes = previewAffectedNodes(nodeIds);
		if(previewAffectedNodes != null){
			preview.add(previewAffectedNodes);
		}
		return preview;
	}
	@Override
	protected List<Long> detectLockedNodes(final List<Long> nodeIds) {

		LockedFileInferenceGraph graph = initLockGraph(nodeIds);

		List<Node> lockedCandidates = graph.collectLockedCandidates();

		List<Long> lockedCandidateIds = new ArrayList<Long>();

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
	protected OperationReport batchDeleteNodes(List<Long> ids) {

		OperationReport report = new OperationReport();

		if (!ids.isEmpty()) {

			List<Long>[] separatedIds = deletionDao.separateFolderFromTestCaseIds(ids);

			List<Long> stepIds = deletionDao.findTestSteps(ids);

			List<Long> testCaseAttachmentIds = deletionDao.findTestCaseAttachmentListIds(ids);
			List<Long> testStepAttachmentIds = deletionDao.findTestStepAttachmentListIds(stepIds);
			List<Long> testCaseFolderAttachmentIds = deletionDao.findTestCaseFolderAttachmentListIds(ids);

			deletionDao.removeCampaignTestPlanInboundReferences(ids);
			deletionDao.removeOrSetIterationTestPlanInboundReferencesToNull(ids);

			deletionDao.setExecutionInboundReferencesToNull(ids);
			deletionDao.setExecStepInboundReferencesToNull(stepIds);

			deletionDao.removeFromVerifyingTestStepsList(stepIds);
			deletionDao.removeFromVerifyingTestCaseLists(ids);

			customValueService.deleteAllCustomFieldValues(BindableEntity.TEST_STEP, stepIds);
			deletionDao.removeAllSteps(stepIds);

			customValueService.deleteAllCustomFieldValues(BindableEntity.TEST_CASE, ids);

			datasetService.removeAllByTestCaseIds(ids);
			parameterService.removeAllByTestCaseIds(ids);

			deletionDao.removeEntities(ids);

			// We merge the attachment list ids for
			// test cases, test step and folder first so that
			// we can make one only one query against the database.
			testCaseAttachmentIds.addAll(testStepAttachmentIds);
			testCaseAttachmentIds.addAll(testCaseFolderAttachmentIds);
			deletionDao.removeAttachmentsLists(testCaseAttachmentIds);

			report.addRemoved(separatedIds[0], "folder");
			report.addRemoved(separatedIds[1], "test-case");

			// Last, take care of the automated tests that could end up as "orphans" after the mass deletion
			autoTestDao.pruneOrphans();

		}

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

	private void deleteActionStep(ActionTestStep step) {
		deletionDao.removeAttachmentList(step.getAttachmentList());
		deletionDao.removeEntity(step);
	}

	private void deleteCallStep(CallTestStep step) {
		deletionDao.removeEntity(step);
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
	protected NotDeletablePreviewReport previewLockedNodes(List<Long> nodeIds) {

		NotDeletablePreviewReport report = null;

		LockedFileInferenceGraph graph = initLockGraph(nodeIds);

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


	protected LockedFileInferenceGraph initLockGraph(List<Long> candidatesId) {

		LibraryGraph<NamedReference, SimpleNode<NamedReference>> calltree = calltreeFinder.getCallerGraph(candidatesId);

		LockedFileInferenceGraph graph = new LockedFileInferenceGraph();
		graph.init(calltree);

		graph.setCandidatesToDeletion(candidatesId);
		graph.resolveLockedFiles();

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


}

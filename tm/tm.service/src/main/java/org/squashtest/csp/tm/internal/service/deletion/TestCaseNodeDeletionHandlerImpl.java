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
package org.squashtest.csp.tm.internal.service.deletion;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.CallTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.internal.repository.FolderDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDeletionDao;
import org.squashtest.csp.tm.internal.repository.TestCaseFolderDao;
import org.squashtest.csp.tm.internal.service.TestCaseNodeDeletionHandler;
import org.squashtest.csp.tm.internal.service.customField.PrivateCustomFieldValueService;
import org.squashtest.csp.tm.internal.service.deletion.LockedFileInferenceGraph.Node;
import org.squashtest.csp.tm.service.TestCaseImportanceManagerService;
import org.squashtest.csp.tm.service.deletion.AffectedEntitiesPreviewReport;
import org.squashtest.csp.tm.service.deletion.NotDeletablePreviewReport;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;

@Component("squashtest.tm.service.deletion.TestCaseNodeDeletionHandler")
public class TestCaseNodeDeletionHandlerImpl extends
		AbstractNodeDeletionHandlerImpl<TestCaseLibraryNode, TestCaseFolder> implements TestCaseNodeDeletionHandler {

	@Inject
	private TestCaseFolderDao folderDao;

	@Inject
	private TestCaseDao leafDao;

	@Inject
	private TestCaseDeletionDao deletionDao;
	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;
	
	@Inject
	private PrivateCustomFieldValueService customValueService;

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

		preview.add(previewLockedNodes(nodeIds));

		// TODO
		// preview.add(previewAffectedNodes(nodeIds));

		return preview;
	}

	@Override
	protected List<Long> detectLockedNodes(final List<Long> nodeIds) {

		LockedFileInferenceGraph graph = initLockGraph(nodeIds);

		List<Node> lockedCandidates = graph.collectLockedCandidates();

		List<Long> lockedCandidateIds = new ArrayList<Long>();

		for (Node node : lockedCandidates) {
			lockedCandidateIds.add(node.getKey());
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
	protected void batchDeleteNodes(List<Long> ids) {
		if (!ids.isEmpty()) {
			List<Long> stepIds = deletionDao.findTestSteps(ids);

			List<Long> testCaseAttachmentIds = deletionDao.findTestCaseAttachmentListIds(ids);
			List<Long> testStepAttachmentIds = deletionDao.findTestStepAttachmentListIds(stepIds);

			deletionDao.removeCallingCampaignItemTestPlan(ids);
			deletionDao.removeOrSetNullCallingIterationItemTestPlan(ids);

			deletionDao.setNullCallingExecutions(ids);
			deletionDao.setNullCallingExecutionSteps(stepIds);

			deletionDao.removeFromVerifyingTestCaseLists(ids);

			deletionDao.removeAllSteps(stepIds);
			
			customValueService.deleteAllCustomFieldValues(BindableEntity.TEST_CASE, ids);
			
			deletionDao.removeEntities(ids);

			// We merge the list for
			// test cases and test step first so that
			// we can make one only one query against the database.
			testCaseAttachmentIds.addAll(testStepAttachmentIds);
			deletionDao.removeAttachmentsLists(testCaseAttachmentIds);

			// supprimer les associations Parent - Enfant ici

		}
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
		deletionDao.setNullCallingExecutionSteps(stepId);

		if (step instanceof ActionTestStep) {
			deleteActionStep((ActionTestStep) step);
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

		NotDeletablePreviewReport report = new NotDeletablePreviewReport();

		LockedFileInferenceGraph graph = initLockGraph(nodeIds);

		// when nonDeletableData is not empty, some of those nodes belongs to
		// the deletion request itself
		// and the other ones are those that still need to be deleted.

		if (graph.hasLockedFiles()) {

			for (Node node : graph.collectLockedCandidates()) {
				report.addName(node.getName());
			}

			for (Node node : graph.collectLockers()) {
				report.addWhy(node.getName());
			}

		}

		return report;
	}

	protected AffectedEntitiesPreviewReport previewAffectedNodes(List<Long> nodeIds) {
		AffectedEntitiesPreviewReport report = new AffectedEntitiesPreviewReport();

		// FIXME

		return report;
	}

	/**
	 * See {@link TestCaseDao#findTestCasesHavingCallerDetails(java.util.Collection)} for more information regarding
	 * that mysterious Object[]
	 * 
	 */
	protected List<Object[]> getAllCallerCalledPairs(List<Long> calledIds) {

		List<Object[]> result = new ArrayList<Object[]>();

		List<Long> currentCalled = new LinkedList<Long>(calledIds);

		while (!currentCalled.isEmpty()) {
			List<Object[]> currentPair = leafDao.findTestCasesHavingCallerDetails(currentCalled);

			result.addAll(currentPair);

			/*
			 * collect the caller ids in the currentPair for the next loop, with the following restrictions : 1) if a
			 * caller id is not null, 2) if that id ( x[0] ) wasn't part of the previous query (ie, treated already) 3)
			 * if that id wasn't already included,
			 * 
			 * then we can add that id.
			 */

			List<Long> nextCalled = new LinkedList<Long>();

			for (Object[] item : currentPair) {
				Object key = item[0];
				if ((key != null) && (!currentCalled.contains(key)) && (!nextCalled.contains(key))) {
					nextCalled.add((Long) item[0]);
				}
			}

			currentCalled = nextCalled;

		}

		return result;

	}

	protected LockedFileInferenceGraph initLockGraph(List<Long> candidatesId) {

		// phase 1 : get the test case call dependencies
		List<Object[]> callGraphDetails = getAllCallerCalledPairs(candidatesId);

		// phase 2 : build the graph of dependencies and resolve the locks.
		LockedFileInferenceGraph graph = new LockedFileInferenceGraph();
		graph.build(callGraphDetails);

		graph.setCandidatesToDeletion(candidatesId);
		graph.resolveLockedFiles();

		// job done, let's return the result
		return graph;
	}

}

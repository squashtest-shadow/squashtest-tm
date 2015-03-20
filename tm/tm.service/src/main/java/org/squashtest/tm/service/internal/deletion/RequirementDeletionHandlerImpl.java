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
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.core.foundation.exception.ActionException;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.WhichNodeVisitor;
import org.squashtest.tm.domain.library.WhichNodeVisitor.NodeType;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.exception.requirement.IllegalRequirementModificationException;
import org.squashtest.tm.service.deletion.BoundToLockedMilestonesReport;
import org.squashtest.tm.service.deletion.BoundToMultipleMilestonesReport;
import org.squashtest.tm.service.deletion.MilestoneModeNoFolderDeletion;
import org.squashtest.tm.service.deletion.Node;
import org.squashtest.tm.service.deletion.NodeMovement;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.library.LibraryUtils;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.internal.repository.RequirementDeletionDao;
import org.squashtest.tm.service.internal.repository.RequirementFolderDao;
import org.squashtest.tm.service.internal.requirement.RequirementNodeDeletionHandler;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;

@Component("squashtest.tm.service.deletion.RequirementNodeDeletionHandler")
public class RequirementDeletionHandlerImpl extends
AbstractNodeDeletionHandler<RequirementLibraryNode, RequirementFolder> implements
RequirementNodeDeletionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementDeletionHandlerImpl.class);

	@Inject
	private RequirementFolderDao folderDao;

	@Inject
	private Provider<TestCaseImportanceManagerForRequirementDeletion> provider;

	@Inject
	private RequirementDao requirementDao;


	@Inject
	private RequirementDeletionDao deletionDao;

	@Inject
	private PrivateCustomFieldValueService customValueService;

	@Inject
	private VerifiedRequirementsManagerService verifiedRequirementsManagerService;

	@Override
	protected FolderDao<RequirementFolder, RequirementLibraryNode> getFolderDao() {
		return folderDao;
	}


	@Override
	protected List<SuppressionPreviewReport> diagnoseSuppression(List<Long> nodeIds, Long milestoneId) {

		List<SuppressionPreviewReport> preview = new LinkedList<SuppressionPreviewReport>();

		// milestone mode verification
		//
		if (milestoneId != null){


			// check if there are some folders in the selection
			List<Long>[] separatedIds = deletionDao.separateFolderFromRequirementIds(nodeIds);
			if (! separatedIds[0].isEmpty()){
				preview.add(new MilestoneModeNoFolderDeletion());
			}

			List<Long> targetVersionIds = deletionDao.findVersionIdsForMilestone(nodeIds, milestoneId);

			// check if some elements belong to milestones which status forbids that
			if (hasTargetVersionsLockedByMilestone(targetVersionIds)){
				preview.add(new BoundToLockedMilestonesReport());
			}


			// check if some versions are bound to multiple milestones
			if (hasTargetVersionsBelongingToManyMilestones(targetVersionIds)){
				preview.add(new BoundToMultipleMilestonesReport());
			}
		}

		return preview;
	}


	/*
	 * The milestone mode for requirement is a bit different from the other entities.
	 * It applies on the requirement versions, instead of the requirement themselves.
	 * 
	 * However the abstract superclass expects to know which requirements cannot be deleted.
	 * 
	 * So, we must return requirement ids, no version ids.
	 * 
	 * Here is how we compute this : at the end of the day, a requirement will be deleted
	 * only if it has only one version and that version will be deleted because it
	 * belong to the given milestone only.
	 * 
	 */
	@Override
	protected List<Long> detectLockedNodes(List<Long> nodeIds, Long milestoneId) {

		List<Long> lockedIds = new ArrayList<>();

		if (milestoneId != null){

			List<Long>[] separateIds = deletionDao.separateFolderFromRequirementIds(nodeIds);

			// a) no folder shall be deleted
			List<Long> folderIds = separateIds[0];
			lockedIds.addAll(folderIds);

			/*
			 * b) a requirement that can be deleted must :
			 * 	1 - have a deletable version AND
			 *  2 - have only one version
			 * 
			 *  Thus, non deletable requirements are the set of candidates minus requirement that can be
			 *  deleted.
			 */

			List<Long> requirementIds = separateIds[1];

			// 1 - have deletable version
			List<Long> deletableRequirements = deletionDao.filterRequirementsHavingDeletableVersions(requirementIds, milestoneId);

			// 2 - have only one version
			List<Long> reqHavingManyVersions = requirementDao.filterRequirementHavingManyVersions(deletableRequirements);
			deletableRequirements.removeAll(reqHavingManyVersions);

			// 3 - finally : non deletable requirements are all the others
			requirementIds.removeAll(deletableRequirements);

			lockedIds.addAll(requirementIds);

		}

		return lockedIds;
	}

	/**
	 * <p>
	 * 	Because nowaway deleting requirements is highly toxic for brain cells here is a method that will help out with
	 * 	deciding if a node should :
	 * </p>
	 * 
	 * <ul>
	 * 	<li>be deleted as a folder (which is simpler)</li>
	 * 	<li>be deleted outright as a requirement with all its versions</li>
	 * 	<li>rebind its subrequirements to its parent (usually the node is to be deleted afterward)</li>
	 * 	<li>delete only a version which happen to belong to a given milestone</li>
	 * 	<li>unbind only a version from a given milestone</li>
	 * </ul>
	 * 
	 * <p>Note that, in particular, the fate of the selected nodes depend on what the user specifically picked.
	 * For each node picked by the user :
	 * 	<ul>
	 * 		<p><strong>rule D1</strong> : if it is a folder : proceed as usual (delete the whole hierarchy) </p>
	 * 		<p><strong>rule D2</strong> : if it is a requirement : delete it and bind its children to its parent, then delete that requirement alone</p>
	 * </ul>
	 * </p>
	 * 
	 * <p>Then, we can safely proceed with peace in mind knowing which node requires which treatment</p>
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.tm.service.internal.deletion.AbstractNodeDeletionHandler#deleteNodes(java.util.List)
	 */
	protected TargetsSortedByAppropriatePunishment sortThatMess(List<Long> nodeIds, Long milestoneId){

		List<Long> deletableFolderIds = null;
		List<Long> deletableRequirementIds = null;
		List<Long> requirementWithRewirableChildren = null;
		List<Long> requirementsWithOneDeletableVersion = null;
		List<Long> requirementsWithOneUnbindableVersion = null;

		List<Long>[] candidateIds = deletionDao.separateFolderFromRequirementIds(nodeIds);

		// --------- find nodes deletable per rule D1 -------------
		List<Long> candidateFolders = candidateIds[0];

		LockedFolderInferenceTree tree = createLockedFileInferenceTree(candidateFolders, milestoneId);
		List<Long> deletableFoldersAndChildrenIds = tree.collectDeletableIds();

		deletableFolderIds = deletionDao.separateFolderFromRequirementIds(deletableFoldersAndChildrenIds)[0];
		deletableRequirementIds = deletionDao.separateFolderFromRequirementIds(deletableFoldersAndChildrenIds)[1];


		// ------- find nodes that needs children-rewiring then deletion per rule D2 ---------
		List<Long> candidateRequirementIds = candidateIds[1];
		List<Long> lockedCandidateIds = detectLockedNodes(candidateRequirementIds, milestoneId);

		requirementWithRewirableChildren = new ArrayList<>(candidateRequirementIds);
		requirementWithRewirableChildren.removeAll(lockedCandidateIds);

		// the rewirable nodes are also deletable nodes
		deletableRequirementIds.addAll(requirementWithRewirableChildren);



		/* ----------
		 * find the nodes which need special actions on
		 * their versions in milestone mode.
		 * 
		 * Those, if applied, are performed on the requirements
		 * encompassed by the selection minus those that
		 * must be deleted
		 * 
		 ------------- */
		if (milestoneId != null){
			List<Long> allRequirementsEncompassed = deletionDao.separateFolderFromRequirementIds(tree.collectKeys())[1];
			allRequirementsEncompassed.removeAll(deletableRequirementIds);
			allRequirementsEncompassed.addAll(lockedCandidateIds);

			requirementsWithOneDeletableVersion = deletionDao.filterRequirementsHavingDeletableVersions(allRequirementsEncompassed, milestoneId);
			requirementsWithOneUnbindableVersion = deletionDao.filterRequirementsHavingUnbindableVersions(allRequirementsEncompassed, milestoneId);

		}

		// -------- now fill our object ---------

		TargetsSortedByAppropriatePunishment sortedTargets = new TargetsSortedByAppropriatePunishment();

		sortedTargets.setDeletableFolderIds(deletableFolderIds);
		sortedTargets.setDeletableRequirementIds(deletableRequirementIds);
		sortedTargets.setRequirementsWithRewirableChildren(requirementWithRewirableChildren);
		sortedTargets.setRequirementsWithOneDeletableVersion(requirementsWithOneDeletableVersion);
		sortedTargets.setRequirementsWithOneUnbindableVersion(requirementsWithOneUnbindableVersion);

		return sortedTargets;

	}



	/**
	 * 
	 * <p>The following method is overridden from the abstract class because the business rule is special :
	 * for each node selected by the user :
	 * 	<ul>
	 * 		<li>a/ if it is a folder : proceed as usual,</li>
	 * 		<li>b/ if it is a requirement : delete it and bind its children to its parent.</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>Another concern is the milestone mode. When regular entities such as test cases are deleted,
	 * they are either deleted, either spared because of business or sec rules. For requirements the
	 * problem is a bit more complex because of their versions. A version can be deleted, and also
	 * if that deleted version was the only one in a requirement then only that requirement can
	 * be deleted.</p>
	 * 
	 * 
	 * <p>All of this is is handled for a good part by the logic in {@link #sortThatMess(List, Long)}</p>
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.tm.service.internal.deletion.AbstractNodeDeletionHandler#deleteNodes(java.util.List)
	 */
	@Override
	public OperationReport deleteNodes(List<Long> targetIds, Long milestoneId) {

		OperationReport globalReport = new OperationReport();

		TargetsSortedByAppropriatePunishment sortedTargets = sortThatMess(targetIds, milestoneId);


		// rewire future orphan requirements
		List<Long> childrenRewirableRequirements = sortedTargets.getRequirementsWithRewirableChildren();
		OperationReport rewiredRequirementsReport = rewireChildrenRequirements(childrenRewirableRequirements);
		globalReport.mergeWith(rewiredRequirementsReport);


		// delete requirements
		List<Long> deletableRequirements = sortedTargets.getDeletableRequirementIds();
		OperationReport deletedRequirementsReport = batchDeleteRequirement(deletableRequirements);
		globalReport.mergeWith(deletedRequirementsReport);


		// delete folders
		List<Long> deletableFolderIds = sortedTargets.getDeletableFolderIds();
		OperationReport deletedFoldersReport = batchDeleteFolders(deletableFolderIds);
		globalReport.mergeWith(deletedFoldersReport);


		// milestone mode :
		if (milestoneId != null){

			// delete just a version
			List<Long> requirementWithDeletableVersion = sortedTargets.getRequirementsWithOneDeletableVersion();
			OperationReport removedVersionsReport = batchRemoveMilestoneVersion(requirementWithDeletableVersion, milestoneId);
			globalReport.mergeWith(removedVersionsReport);

			// unbind just a version
			List<Long> requirementWithUnbindableVersion = sortedTargets.getRequirementsWithOneUnbindableVersion();
			OperationReport unboundVerionsReport = batchUnbindFromMilestone(requirementWithUnbindableVersion, milestoneId);
			globalReport.mergeWith(unboundVerionsReport);


		}


		return globalReport;
	}



	// ****************************** atrocious boilerplate here ************************


	protected OperationReport batchDeleteFolders(List<Long> folderIds){

		OperationReport report = new OperationReport();

		if (!folderIds.isEmpty()) {
			List<Long> requirementFolderAttachmentIds = deletionDao.findRequirementFolderAttachmentListIds(folderIds);
			deletionDao.removeEntities(folderIds);
			deletionDao.removeAttachmentsLists(requirementFolderAttachmentIds);
			report.addRemoved(folderIds, "folder");

			deletionDao.flush();
		}

		return report;
	}

	protected OperationReport batchDeleteRequirement(List<Long> ids){

		OperationReport report = new OperationReport();

		if (! ids.isEmpty()) {

			// prepare the recomputation of test case automatic importances
			TestCaseImportanceManagerForRequirementDeletion testCaseImportanceManager = provider.get();
			testCaseImportanceManager.prepareRequirementDeletion(ids);

			// now let's remove the requirement versions
			// don't forget to first remove the reference a requirement
			// has to the current version (see Requirement#resource)
			deletionDao.unsetRequirementCurrentVersion(ids);
			List<Long> allVersionIds = deletionDao.findVersionIds(ids);
			batchDeleteVersions(allVersionIds);

			// remove the requirement audit event
			deletionDao.deleteRequirementAuditEvents(ids);

			// remove the requirements now
			deletionDao.removeEntities(ids);

			// notify the test cases
			testCaseImportanceManager.changeImportanceAfterRequirementDeletion();

			// fill the report
			report.addRemoved(ids, "requirement");

			deletionDao.flush();

		}

		return report;
	}


	protected OperationReport batchRemoveMilestoneVersion(List<Long> requirementIds, Long milestoneId){
		OperationReport report = new OperationReport();

		if (! requirementIds.isEmpty()){

			// prepare the recomputation of test case automatic importances
			TestCaseImportanceManagerForRequirementDeletion testCaseImportanceManager = provider.get();
			testCaseImportanceManager.prepareRequirementDeletion(requirementIds);

			// now let's remove the requirement versions
			// don't forget to first remove the reference a requirement
			// has to the current version (see Requirement#resource)
			deletionDao.unsetRequirementCurrentVersion(requirementIds);
			List<Long> versionIds = deletionDao.findDeletableVersions(requirementIds, milestoneId);
			batchDeleteVersions(versionIds);

			// now reset the latest version of those requirements
			deletionDao.resetRequirementCurrentVersion(requirementIds);

			// notify the test cases
			testCaseImportanceManager.changeImportanceAfterRequirementDeletion();

			report.addRemoved(requirementIds, "requirement");

			deletionDao.flush();

		}

		return report;

	}

	protected OperationReport batchUnbindFromMilestone(List<Long> requirementIds, Long milestoneId){
		OperationReport report = new OperationReport();

		if (! requirementIds.isEmpty()){

			List<Long> versionIds = deletionDao.findUnbindableVersions(requirementIds, milestoneId);
			List<Long> unbindableRequirements = requirementDao.findByRequirementVersion(versionIds);

			deletionDao.unbindFromMilestone(unbindableRequirements, milestoneId);

			report.addRemoved(requirementIds, "requirement");

			deletionDao.flush();
		}

		return report;
	}


	private OperationReport batchDeleteVersions(List<Long> versionIds){
		OperationReport report = new OperationReport();

		if (! versionIds.isEmpty()) {

			customValueService.deleteAllCustomFieldValues(BindableEntity.REQUIREMENT_VERSION, versionIds);

			// save the attachment list ids for later reference
			List<Long> versionsAttachmentIds = deletionDao.findRequirementVersionAttachmentListIds(versionIds);

			// remove the changelog
			deletionDao.deleteRequirementVersionAuditEvents(versionIds);

			// remove binds to other entities
			deletionDao.removeTestStepsCoverageByRequirementVersionIds(versionIds);
			deletionDao.removeFromVerifiedVersionsLists(versionIds);

			// remove the elements now
			deletionDao.deleteVersions(versionIds);
			deletionDao.removeAttachmentsLists(versionsAttachmentIds);

			deletionDao.flush();

		}

		return report;
	}



	// todo : send back an object that describes which requirements where rebound to which entities, and how they were
	// renamed if so.
	private OperationReport rewireChildrenRequirements(List<Long> requirements) {

		try{
			if (!requirements.isEmpty()) {
				OperationReport rewireReport = new OperationReport();

				List<Object[]> pairedParentChildren = requirementDao.findAllParentsOf(requirements);

				for (Object[] pair : pairedParentChildren) {

					NodeContainer<Requirement> parent = (NodeContainer<Requirement>) pair[0];
					Requirement requirement = (Requirement) pair[1];

					renameContentIfNeededThenAttach(parent, requirement, rewireReport);

				}

				requirementDao.flush();

				return rewireReport;
			} else {
				return new OperationReport();
			}
		}catch(IllegalRequirementModificationException ex){
			throw new ImpossibleSuppression(ex);
		}
	}

	private void renameContentIfNeededThenAttach(NodeContainer<Requirement> parent, Requirement toBeDeleted,
			OperationReport report) {

		// abort if no operation is necessary
		if (toBeDeleted.getContent().isEmpty()) {
			return;
		}

		// init
		Collection<Requirement> children = new ArrayList<Requirement>(toBeDeleted.getContent());
		List<Node> movedNodesLog = new ArrayList<Node>(toBeDeleted.getContent().size());

		boolean needsRenaming = false;

		// renaming loop. Loop over each children, and for each of them ensure that they wont namecrash within their new
		// parent.
		// Log all these operations in the report object.
		for (Requirement child : children) {

			needsRenaming = false;
			String name = child.getName();

			while (!parent.isContentNameAvailable(name)) {
				name = LibraryUtils.generateNonClashingName(name, parent.getContentNames(), Requirement.MAX_NAME_SIZE);
				needsRenaming = true;
			}

			// log the renaming operation if happened.
			if (needsRenaming) {
				child.setName(name);
				report.addRenamed("requirement", child.getId(), name);
			}

			// log the movement operation.
			movedNodesLog.add(new Node(child.getId(), "requirement"));

		}

		// detach the children from their old parent.
		toBeDeleted.getContent().clear();
		parent.removeContent(toBeDeleted);

		// flushing here ensures that the DB calls will be carried on in the proper order.
		deletionDao.flush();

		// attach the children to their new parent.
		// TODO : perhaps use the navigation service facilities instead? For now I believe it's fine enough.
		for (Requirement child : children) {
			parent.addContent(child);
		}

		// fill the report
		NodeType type = new WhichNodeVisitor().getTypeOf(parent);
		String strtype;
		switch (type) {
		case REQUIREMENT_LIBRARY:
			strtype = "drive";
			break;
		case REQUIREMENT_FOLDER:
			strtype = "folder";
			break;
		default:
			strtype = "requirement";
			break;
		}

		NodeMovement nodeMovement = new NodeMovement(new Node(parent.getId(), strtype), movedNodesLog);
		report.addMoved(nodeMovement);

	}

	// ************************** predicates *****************************************

	private boolean hasTargetVersionsBelongingToManyMilestones(List<Long> versionIds){
		List<Long> boundNodes = deletionDao.filterVersionIdsHavingMultipleMilestones(versionIds);
		return ! (boundNodes.isEmpty());
	}

	private boolean hasTargetVersionsLockedByMilestone(List<Long> versionIds){
		List<Long> lockedNodes = deletionDao.filterVersionIdsWhichMilestonesForbidsDeletion(versionIds);
		return ! (lockedNodes.isEmpty());
	}

	// *********************** inner classes *****************************************

	private static final class ImpossibleSuppression extends ActionException{

		/**
		 * 
		 */
		private static final long serialVersionUID = 4901610054565947807L;
		private static final String impossibleSuppressionException = "squashtm.action.exception.impossiblerequirementsuppression.label";


		public ImpossibleSuppression(Exception ex){
			super(ex);
		}


		@Override
		public String getI18nKey() {
			return impossibleSuppressionException;
		}

	}


	private static final class TargetsSortedByAppropriatePunishment{

		/**
		 * those ids are deletable folder ids
		 */
		List<Long> deletableFolderIds;

		/**
		 * those ids are requirements that should be deleted
		 */
		List<Long> deletableRequirementIds;

		/**
		 * those ids are requirements we need to reassign the subrequirements to their grandparent first (before it is deleted)
		 */
		List<Long> requirementsWithRewirableChildren;

		/**
		 * those ids are requirements which have only one version that should be deleted
		 */
		List<Long> requirementsWithOneDeletableVersion;

		/**
		 * those ids are requirements which have one version that should be unbound from the milestone
		 */
		List<Long> requirementsWithOneUnbindableVersion;


		List<Long> getDeletableRequirementIds() {
			return (deletableRequirementIds != null) ? deletableRequirementIds : new ArrayList<Long>();
		}

		List<Long> getRequirementsWithOneDeletableVersion() {
			return (requirementsWithOneDeletableVersion != null) ? requirementsWithOneDeletableVersion : new ArrayList<Long>();
		}

		List<Long> getDeletableFolderIds() {
			return (deletableFolderIds != null) ? deletableFolderIds : new ArrayList<Long>();
		}

		List<Long> getRequirementsWithOneUnbindableVersion() {
			return (requirementsWithOneUnbindableVersion != null) ? requirementsWithOneUnbindableVersion : new ArrayList<Long>();
		}

		List<Long> getRequirementsWithRewirableChildren() {
			return (requirementsWithRewirableChildren != null) ? requirementsWithRewirableChildren : new ArrayList<Long>();
		}

		void setDeletableRequirementIds(List<Long> deletableRequirementIds) {
			this.deletableRequirementIds = deletableRequirementIds;
		}

		public void setDeletableFolderIds(List<Long> deletableFolderIds) {
			this.deletableFolderIds = deletableFolderIds;
		}

		void setRequirementsWithOneDeletableVersion(List<Long> requirementsWithOneDeletableVersion) {
			this.requirementsWithOneDeletableVersion = requirementsWithOneDeletableVersion;
		}

		void setRequirementsWithOneUnbindableVersion(List<Long> requirementsWithOneUnbindableVersion) {
			this.requirementsWithOneUnbindableVersion = requirementsWithOneUnbindableVersion;
		}

		void setRequirementsWithRewirableChildren(List<Long> requirementsWithRewirableChildren) {
			this.requirementsWithRewirableChildren = requirementsWithRewirableChildren;
		}

	}

	/* **************************************************************************************************************
	 * 												Legacy code
	 ************************************************************************************************************** */

	/*
	 * Removing a list of RequirementLibraryNodes means : - find all the attachment lists, - remove them, - remove the
	 * nodes themselves
	 */
	/*
	 * Deprecation notice
	 * 
	 * This method is deprecated because it is no longer called by the super class : the method #deleteNodes has
	 * been overridden in the present subclass, that now calls more specific methods.
	 * 
	 * However the class must still provide an implementation. So we leave this code as history, but marked as deprecated.
	 * Note that it doesn't support the milestone mode nor some other specific rules regarding requirement rewiring etc.
	 * 
	 */
	@Override
	@Deprecated
	protected OperationReport batchDeleteNodes(List<Long> ids, Long milestoneId) {

		OperationReport report = new OperationReport();

		if (!ids.isEmpty()) {

			List<Long>[] separatedIds = deletionDao.separateFolderFromRequirementIds(ids);

			TestCaseImportanceManagerForRequirementDeletion testCaseImportanceManager = provider.get();
			testCaseImportanceManager.prepareRequirementDeletion(ids);

			// remove the custom fields
			List<Long> allVersionIds = deletionDao.findVersionIds(ids);
			customValueService.deleteAllCustomFieldValues(BindableEntity.REQUIREMENT_VERSION, allVersionIds);

			// save the attachment list ids for later reference
			List<Long> requirementAttachmentIds = deletionDao.findRequirementAttachmentListIds(ids);
			List<Long> requirementFolderAttachmentIds = deletionDao.findRequirementFolderAttachmentListIds(ids);

			// remove the changelog
			deletionDao.deleteRequirementAuditEvents(ids);

			// remove binds to other entities
			deletionDao.removeTestStepsCoverageByRequirementVersionIds(allVersionIds);


			// remove the elements now
			deletionDao.removeEntities(ids);


			// finally delete the attachment lists
			requirementAttachmentIds.addAll(requirementFolderAttachmentIds);
			deletionDao.removeAttachmentsLists(requirementAttachmentIds);

			testCaseImportanceManager.changeImportanceAfterRequirementDeletion();

			// fill the report
			report.addRemoved(separatedIds[0], "folder");
			report.addRemoved(separatedIds[1], "requirement");
		}

		return report;
	}



}

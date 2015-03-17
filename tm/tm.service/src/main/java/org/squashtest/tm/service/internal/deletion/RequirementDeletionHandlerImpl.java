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
		if (milestoneId != null){

			// check if some elements belong to milestones which status forbids that
			if (someNodesAreLockedByMilestones(nodeIds)){
				preview.add(new BoundToLockedMilestonesReport());
			}

			// check if there are some folders in the selection
			List<Long>[] separatedIds = deletionDao.separateFolderFromRequirementIds(nodeIds);
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

	@Override
	protected List<Long> detectLockedNodes(List<Long> nodeIds, Long milestoneId) {

		List<Long> lockedIds;

		/*
		 * milestone mode :
		 * - 1) no folder shall be deleted (enqueued outright)
		 * - 2) no requirement that doesn't belong to the milestone shall be deleted
		 * - 3) no requirement bound to more than one milestone shall be deleted (they will be unbound though, but later).
		 * - 4) no requirement bound to a milestone which status forbids deletion shall be deleted.
		 */
		if (milestoneId != null){

			// 1 - no folder shall be deleted
			List<Long> folderIds = deletionDao.separateFolderFromRequirementIds(nodeIds)[0];

			// 2 - no requirement that aren't bound to the current milestone shall be deleted
			List<Long> notBoundToMilestone = requirementDao.findNonBoundRequirement(nodeIds, milestoneId);

			// 3 - no requirement bound to more than one milestone shall be deleted
			List<Long> boundToMoreMilestones = requirementDao.findRequirementIdsHavingMultipleMilestones(nodeIds);

			// 4 - no requirement bound to locked milestones shall be deleted
			List<Long> lockedByMiletones = deletionDao.findRequirementsWhichMilestonesForbidsDeletion(nodeIds);

			lockedIds = new ArrayList<>(folderIds.size()+
					notBoundToMilestone.size()+
					boundToMoreMilestones.size()+
					lockedByMiletones.size());

			lockedIds.addAll(folderIds);
			lockedIds.addAll(boundToMoreMilestones);
			lockedIds.addAll(notBoundToMilestone);
			lockedIds.addAll(lockedByMiletones);
		}
		else{
			lockedIds = new ArrayList<>();
		}

		return lockedIds;
	}

	/*
	 * The following method is overridden from the abstract class because the business rule is special : for each node
	 * selected by the user : - if it is a folder : proceed as usual, - if it is a requirement : delete it and bind its
	 * children to its parent.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.tm.service.internal.deletion.AbstractNodeDeletionHandler#deleteNodes(java.util.List)
	 */
	@Override
	public OperationReport deleteNodes(List<Long> targetIds, Long milestoneId) {

		OperationReport globalReport = new OperationReport();

		// first step : split the target ids into [folderIds, requirementIds].
		List<Long>[] separatedIds = deletionDao.separateFolderFromRequirementIds(targetIds);

		// the folderIds are treated as usual.
		OperationReport deletedFolders = super.deleteNodes(separatedIds[0], milestoneId);
		deletionDao.flush();
		globalReport.mergeWith(deletedFolders);

		// the requirements get a special treatment : first we rewire the children requirements
		// when a parent requirement is removed, second we bypass super#deleteNodes
		// because we don't need to re-compute which folders should be deleted by transitivity.
		OperationReport rewiredRequirements = rewireChildrenRequirements(separatedIds[1]);
		globalReport.mergeWith(rewiredRequirements);
		deletionDao.flush();

		OperationReport deletedRequirements = batchDeleteNodes(separatedIds[1], milestoneId);
		deletionDao.flush();
		globalReport.mergeWith(deletedRequirements);

		return globalReport;
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

				return rewireReport;
			} else {
				return new OperationReport();
			}
		}catch(IllegalRequirementModificationException ex){
			throw new ImpossibleSuppression(ex);
		}
	}

	// ****************************** atrocious boilerplate here ************************

	/*
	 * Removing a list of RequirementLibraryNodes means : - find all the attachment lists, - remove them, - remove the
	 * nodes themselves
	 */
	@Override
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
			//deletionDao.removeFromVerifiedRequirementLists(ids);


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


	@Override
	protected OperationReport batchUnbindFromMilestone(List<Long> ids, Long milestoneId) {

		List<Long> remainingIds = deletionDao.findRemainingRequirementIds(ids);

		// some node should not be unbound.
		List<Long> lockedIds = deletionDao.findRequirementsWhichMilestonesForbidsDeletion(remainingIds);
		remainingIds.removeAll(lockedIds);

		OperationReport report = new OperationReport();

		deletionDao.unbindFromMilestone(remainingIds, milestoneId);

		report.addRemoved(remainingIds, "requirement");

		return report;
	}

	private boolean someNodesHaveMultipleMilestones(List<Long> nodeIds){
		List<Long> boundNodes = requirementDao.findRequirementIdsHavingMultipleMilestones(nodeIds);
		return ! (boundNodes.isEmpty());
	}

	private boolean someNodesAreLockedByMilestones(List<Long> nodeIds){
		List<Long> lockedNodes = deletionDao.findRequirementsWhichMilestonesForbidsDeletion(nodeIds);
		return ! (lockedNodes.isEmpty());
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


}

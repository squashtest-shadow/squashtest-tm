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
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.WhichNodeVisitor;
import org.squashtest.tm.domain.library.WhichNodeVisitor.NodeType;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.service.deletion.Node;
import org.squashtest.tm.service.deletion.NodeMovement;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.internal.repository.RequirementDeletionDao;
import org.squashtest.tm.service.internal.repository.RequirementFolderDao;
import org.squashtest.tm.service.internal.requirement.RequirementNodeDeletionHandler;

@Component("squashtest.tm.service.deletion.RequirementNodeDeletionHandler")
public class RequirementDeletionHandlerImpl extends
		AbstractNodeDeletionHandler<RequirementLibraryNode, RequirementFolder> implements
		RequirementNodeDeletionHandler {
	
	private static final Logger LOGGER  =  LoggerFactory.getLogger(RequirementDeletionHandlerImpl.class);

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
	
	
	@Override
	protected FolderDao<RequirementFolder, RequirementLibraryNode> getFolderDao() {
		return folderDao;
	}

	@Override
	protected List<SuppressionPreviewReport> diagnoseSuppression(List<Long> nodeIds) {
		List<SuppressionPreviewReport> preview = new LinkedList<SuppressionPreviewReport>();

		// TODO : perform an actual verification

		return preview;
	}

	@Override
	protected List<Long> detectLockedNodes(List<Long> nodeIds) {
		List<Long> lockedIds = new LinkedList<Long>();

		// TODO : up to now a requirement is never locked for deletion (safe for security check)
		// however if it may change later put something here.

		return lockedIds;
	}

	
	/*
	 * The following method is overridden from the abstract class because the business rule is special : 
	 * for each node selected by the user :
	 * - if it is a folder : proceed as usual,
	 * - if it is a requirement : delete it and bind its children to its parent.
	 *   
	 * (non-Javadoc)
	 * @see org.squashtest.tm.service.internal.deletion.AbstractNodeDeletionHandler#deleteNodes(java.util.List)
	 * 
	 * 
	 * ALSO, POSSIBLY REFACTOR THE MAIN API SO THAT  
	 * 
	 */
	@Override
	public OperationReport deleteNodes(List<Long> targetIds){
		
		OperationReport globalReport = new OperationReport();
		
			// first step : split the target ids into (folderIds, requirementIds).
		List<Long>[] separatedIds = deletionDao.separateFolderFromRequirementIds(targetIds);
		
		//the folderIds are treated as usual.
		OperationReport deletedFolders = _deleteFolderContent(separatedIds[0]);
		globalReport.mergeWith(deletedFolders);
		
		//the requirements gets a special treatment.
		OperationReport rewiredRequirements = _rewireChildrenRequirements(separatedIds[1]);
		globalReport.mergeWith(rewiredRequirements);
		
		OperationReport deletedRequirements = batchDeleteNodes(separatedIds[1]);
		globalReport.mergeWith(deletedRequirements);
		
		return globalReport;
	}
	
	
	private OperationReport _deleteFolderContent(List<Long> folderIds){
		OperationReport report = super.deleteNodes(folderIds);	// in that case, business as usual
		deletionDao.flush();
		return report;
	}
	
	
	//todo : send back an object that describes which requirements where rebound to which entities, and how they were renamed if so.
	private OperationReport _rewireChildrenRequirements(List<Long> requirements){
		
		OperationReport rewireReport = new OperationReport();
		
		List<Object[]> pairedParentChildren = requirementDao.findAllParentsOf(requirements);
		
		for (Object[] pair : pairedParentChildren){
			
			NodeContainer<Requirement> parent = (NodeContainer<Requirement>)pair[0];
			Requirement requirement = (Requirement) pair[1];

			_renameContentIfNeededThenAttach(parent, requirement, rewireReport);
			
		}
		
		return rewireReport;
	}
	
	
	/*
	 * Removing a list of RequirementLibraryNodes means : - find all the attachment lists, - remove them, - remove
	 * the nodes themselves
	 */
	@Override
	protected OperationReport batchDeleteNodes(List<Long> ids) {
		if (!ids.isEmpty()) {

			TestCaseImportanceManagerForRequirementDeletion testCaseImportanceManager = provider.get();
			testCaseImportanceManager.prepareRequirementDeletion(ids);

			//remove the custom fields	
			List<Long> allVersionIds = deletionDao.findVersionIds(ids);
			customValueService.deleteAllCustomFieldValues(BindableEntity.REQUIREMENT_VERSION, allVersionIds);
			
			List<Long> requirementAttachmentIds = deletionDao.findRequirementAttachmentListIds(ids);

			deletionDao.removeTestStepsCoverageByRequirementVersionIds(allVersionIds);
			deletionDao.removeFromVerifiedRequirementLists(ids);

			deletionDao.deleteRequirementAuditEvents(ids);

			deletionDao.removeEntities(ids);

			deletionDao.removeAttachmentsLists(requirementAttachmentIds);

			testCaseImportanceManager.changeImportanceAfterRequirementDeletion();

		}
		
		OperationReport report = new OperationReport();
		report.addRemovedNodes(ids, "mixed-requirement-and-folders");
		return report;
	}
	
	
	// ****************************** atrocious boilerplate here ************************
	
	
	private void _renameContentIfNeededThenAttach(NodeContainer<Requirement> parent, Requirement toBeDeleted, OperationReport report){
		
		// initiate the NodeMovement object
		List<Node> movedNodesLog = new ArrayList<Node>(toBeDeleted.getContent().size());
		
		// init the rest
		boolean needsRenaming = false;
		Collection<Requirement> children = toBeDeleted.getContent();

		// renaming loop. Loop over each children, and for each of them ensure that they wont namecrash within their new parent. 
		// Log all these operations in the report object.
		for (Requirement child : children){

			needsRenaming = false;
			String name = child.getName();
			
			while(! parent.isContentNameAvailable(name)){
				needsRenaming = true;
				name = child.getName()+"-"+(""+Math.random()).substring(0, 4);	//collisions are unlikely.
			}
			
			// log the renaming operation if happened.
			if (needsRenaming){
				child.setName(name);
				report.addNodeRenaming("requirement", child.getId(), name);
			}
			
			// now move the node and log the movement operation. 
			// TODO : perhaps use the navigation service facilities instead? Although the following code is fine enough I think.
			parent.addContent(child);
			movedNodesLog.add(new Node(child.getId(), "requirement"));
		}
		
		//complete the node movement report
		NodeType type = new WhichNodeVisitor().getTypeOf(parent);
		String strtype;
		switch(type){
			case REQUIREMENT_LIBRARY : strtype = "drive"; break;
			case REQUIREMENT_FOLDER : strtype = "folder"; break;
			default : strtype = "requirement"; break;
		}
		
		NodeMovement nodeMovement = new NodeMovement(new Node(parent.getId(), strtype), movedNodesLog);
		report.addNodeMovement(nodeMovement);
	}

}

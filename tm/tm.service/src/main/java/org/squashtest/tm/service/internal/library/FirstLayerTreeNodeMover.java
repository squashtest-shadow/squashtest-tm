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
package org.squashtest.tm.service.internal.library;

import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.NodeContainerVisitor;
import org.squashtest.tm.domain.library.NodeVisitor;
import org.squashtest.tm.domain.library.TreeNode;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.exception.library.CannotMoveInHimselfException;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.CampaignFolderDao;
import org.squashtest.tm.service.internal.repository.CampaignLibraryDao;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.LibraryDao;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.RequirementFolderDao;
import org.squashtest.tm.service.internal.repository.RequirementLibraryDao;
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;
/**
 * This class is called when moving nodes to another one, it is called only for the first nodes of moved hierarchies.
 * If the move changes project, next layer nodes will be updated (not need to move them)
 * with the {@link NextLayersTreeNodeMover}.
 * 
 * @author mpagnon
 *
 */
@Component
@Scope("prototype")
public class FirstLayerTreeNodeMover  implements NodeVisitor, PasteOperation {
	@Inject
	@Qualifier("squashtest.tm.repository.RequirementLibraryNodeDao")
	private LibraryNodeDao<RequirementLibraryNode> requirementLibraryNodeDao;
	@Inject
	@Qualifier("squashtest.tm.repository.TestCaseLibraryNodeDao")
	private LibraryNodeDao<TestCaseLibraryNode> testCaseLibraryNodeDao;
	@Inject
	@Qualifier("squashtest.tm.repository.CampaignLibraryNodeDao")
	private LibraryNodeDao<CampaignLibraryNode> campaignLibraryNodeDao;
	@Inject
	private RequirementFolderDao requirementFolderDao;
	@Inject
	private TestCaseFolderDao testCaseFolderDao;
	@Inject
	private CampaignDao campaignDao;
	@Inject
	private CampaignFolderDao campaignFolderDao;
	@Inject
	private RequirementLibraryDao requirementLibraryDao;
	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;
	@Inject
	private TreeNodeUpdater treeNodeUpdater;
	@Inject
	private CampaignLibraryDao campaignLibraryDao;
	@Inject
	private PermissionEvaluationService permissionEvaluationService;
	private TreeNode movedNode;
	private NodeContainer<? extends TreeNode> destination;
	private boolean projectChanged = false;

	public TreeNode performOperation(TreeNode toMove, NodeContainer<TreeNode> destination) {
		//initialize attributes
		this.destination = destination;
		movedNode = null;
		//check destination's hierarchy doesn't contain node to move
		checkNotMovedInHimself(toMove);
		//project changed ? 
		Project sourceProject = toMove.getProject();
		GenericProject destinationProject = destination.getProject();
		this.projectChanged = changedProject(sourceProject, destinationProject);
		//process
		toMove.accept(this);
		if(projectChanged){
			movedNode.accept(treeNodeUpdater);
		}
		return movedNode;
	}

	@Override
	public boolean isOkToGoDeeper() {
		return this.projectChanged;
	}

	@Override
	public void visit(CampaignFolder campaignFolder) {
		visitLibraryNode(campaignFolder, campaignLibraryDao, campaignFolderDao);
		
	}

	@Override
	public void visit(RequirementFolder requirementFolder) {
		visitLibraryNode(requirementFolder, requirementLibraryDao, requirementFolderDao);
	}

	@Override
	public void visit(TestCaseFolder testCaseFolder) {
		visitLibraryNode(testCaseFolder, testCaseLibraryDao, testCaseFolderDao);
		
	}

	@Override
	public void visit(Campaign campaign) {
		visitLibraryNode(campaign, campaignLibraryDao, campaignFolderDao);
		
	}

	@Override
	/**
	 * Iterations cannot be moved.
	 */
	public void visit(Iteration iteration) {
		// NOOPE
	}

	@Override
	/**
	 * TestSuite cannot be moved.
	 * 
	 */
	public void visit(TestSuite testSuite) {
		// NOOP€
	}

	@Override
	public void visit(Requirement requirement) {
		visitLibraryNode(requirement, requirementLibraryDao, requirementFolderDao);
		
	}

	@Override
	public void visit(TestCase testCase) {
		visitLibraryNode(testCase, testCaseLibraryDao, testCaseFolderDao);
		
	}

	@SuppressWarnings("unchecked")
	private <LN extends LibraryNode> void visitLibraryNode(LN node, LibraryDao<?,?> libraryDao,
			FolderDao<?,?> folderDao) {
		
		NodeContainer<LN> parent = findFolderOrLibraryParent(node, libraryDao, folderDao);
		
		PermissionsUtils.checkPermission(permissionEvaluationService, new SecurityCheckableObject(destination, "CREATE"), new SecurityCheckableObject(
				parent, "DELETE"), new SecurityCheckableObject(node, "READ"));
		
		node.notifyAssociatedWithProject((Project)destination.getProject());
		moveNode(node, (NodeContainer<LN>) destination, parent);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <LN extends LibraryNode> NodeContainer<LN> findFolderOrLibraryParent(LN node, LibraryDao libraryDao,
			FolderDao folderDao) {
		Library<? extends LibraryNode> parentLib = libraryDao.findByRootContent(node);
		return ((parentLib != null) ? parentLib : folderDao.findByContent(node));
	}

	private <TN extends TreeNode> void moveNode(TN toMove, NodeContainer<TN> destination, NodeContainer<TN> toMoveParent) {
		toMoveParent.removeContent(toMove);
		campaignDao.flush();
		destination.addContent(toMove);
		movedNode = toMove;
	}



	/**
	 * Checks if node1's project is the same as node2's.
	 * 
	 * @param sourceProject , the project of the source node
	 * @param destinationProject , the project of the destination
	 * @return true if the source and destination projects are the same.
	 * 
	 */
	private boolean changedProject(Project sourceProject , GenericProject destinationProject) {		
		return (sourceProject != null && destinationProject != null && !sourceProject.getId().equals(destinationProject.getId()));
	}
	
	/**
	 * Will check it treeNode to move is not his destination and if it is not contained in the hierarchy of it's destination.
	 * @param toMove
	 * @return 
	 */
	private void checkNotMovedInHimself(TreeNode toMove) {
		destination.accept(new CheckMovedInHimselfVisitor(toMove));
	}
	
	private class CheckMovedInHimselfVisitor implements NodeContainerVisitor{
		
	private TreeNode toMove;

		public CheckMovedInHimselfVisitor(TreeNode toMove) {
			this.toMove = toMove;
		}

		@Override
		public void visit(CampaignLibrary campaignLibrary) {
			//NOOPE
		}

		@Override
		public void visit(RequirementLibrary requirementLibrary) {
			//NOOPE
			
		}

		@Override
		public void visit(TestCaseLibrary testCaseLibrary) {
			//NOOPE
			
		}

		@Override
		public void visit(CampaignFolder campaignFolder) {
			Long toMoveId = ((CampaignLibraryNode)toMove).getId();
			checkHierarchy(toMoveId, campaignFolder.getId(), campaignLibraryNodeDao);
		}


		@Override
		public void visit(RequirementFolder requirementFolder) {
			Long toMoveId = ((RequirementLibraryNode)toMove).getId();			
			checkHierarchy(toMoveId, requirementFolder.getId(), requirementLibraryNodeDao);
		}

		@Override
		public void visit(TestCaseFolder testCaseFolder) {
			Long toMoveId = ((TestCaseLibraryNode)toMove).getId();			
			checkHierarchy(toMoveId, testCaseFolder.getId(), testCaseLibraryNodeDao);
		}

		@Override
		public void visit(Campaign campaign) {
			//NOOPE
			
		}

		@Override
		public void visit(Iteration iteration) {
			//NOOPE
			
		}

		@Override
		public void visit(Requirement requirement) {
			//naaay
		}

		private void checkHierarchy(Long tocheckId, Long hierarchySourceId,
				LibraryNodeDao<? extends LibraryNode> libraryNodeDao) {
			
			if(tocheckId.equals(hierarchySourceId)){
				throw new CannotMoveInHimselfException();
			} else if(hierarchyContains(libraryNodeDao, hierarchySourceId, tocheckId)){
				throw new CannotMoveInHimselfException();
			}
			
			
		}
		
		private boolean hierarchyContains(LibraryNodeDao<? extends LibraryNode>  libraryNodeDao, Long hierarchySourceId,
				Long tocheck) {
			List<Long> hierarchyIds = libraryNodeDao.getParentsIds(hierarchySourceId);
			for(Long id : hierarchyIds){
				if(id.equals(tocheck)){
					return true;
				}
			}
			return false;
		}
		
	}

	

}

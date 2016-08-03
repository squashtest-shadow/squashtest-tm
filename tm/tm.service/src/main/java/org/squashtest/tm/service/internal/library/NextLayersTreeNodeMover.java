/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.NodeVisitor;
import org.squashtest.tm.domain.library.TreeNode;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
/**
 * This class is used after the {@link FirstLayerTreeNodeMover}.
 * It will make sure all nodes know their project are consistent with their project's parameters.
 * 
 * @author mpagnon
 *
 */
@Component
@Scope("prototype")
public class NextLayersTreeNodeMover  implements NodeVisitor, PasteOperation {
	
	@Inject
	private TreeNodeUpdater treeNodeUpdater;
	private NodeContainer<? extends TreeNode> destination;

	/**
	 * Will make sure all nodes are aware of their project and call the {@link TreeNodeUpdater} on each of them.
	 */

	@Override
	public TreeNode performOperation(TreeNode toMove, NodeContainer<TreeNode> destination, Integer position) {
		this.destination = destination;
		toMove.accept(this);
		toMove.accept(treeNodeUpdater);
		return toMove;
	}
	
	/**************************************************** PRIVATE **********************************************************/
	@Override
	public boolean isOkToGoDeeper() {
		return true;
	}

	@Override
	public void visit(CampaignFolder campaignFolder) {
		campaignFolder.notifyAssociatedWithProject((Project)destination.getProject());
		
	}

	@Override
	public void visit(RequirementFolder requirementFolder) {
		requirementFolder.notifyAssociatedWithProject((Project)destination.getProject());
	}

	@Override
	public void visit(TestCaseFolder testCaseFolder) {
		testCaseFolder.notifyAssociatedWithProject((Project)destination.getProject());
		
	}

	@Override
	public void visit(Campaign campaign) {
		campaign.notifyAssociatedWithProject((Project)destination.getProject());
	}

	@Override
	/**
	 * Iterations cannot be moved.
	 */
	public void visit(Iteration iteration) {
		//nothing to do
	}

	@Override
	public void visit(TestSuite testSuite) {
		//nothing to do
	}

	@Override
	public void visit(Requirement requirement) {
		requirement.notifyAssociatedWithProject((Project)destination.getProject());
	}

	@Override
	public void visit(TestCase testCase) {
		testCase.notifyAssociatedWithProject((Project)destination.getProject());
	}

	@Override
	public List<Long> getRequirementVersionToIndex() {
		return Collections.emptyList();
	}

	@Override
	public List<Long> getTestCaseToIndex() {
		return Collections.emptyList();
	}

}

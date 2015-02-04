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
package org.squashtest.tm.web.internal.model.builder;

import java.util.List;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.CampaignLibraryNodeVisitor;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode.State;

@Component
@Scope("prototype")
public class CampaignLibraryTreeNodeBuilder extends LibraryTreeNodeBuilder<CampaignLibraryNode> {
	/**
	 * This visitor is used to populate custom attributes of the {@link JsTreeNode} currently built
	 * 
	 */
	private class CustomAttributesPopulator implements CampaignLibraryNodeVisitor {
		private final JsTreeNode builtNode;

		public CustomAttributesPopulator(JsTreeNode builtNode) {
			super();
			this.builtNode = builtNode;
		}

		/**
		 * 
		 * @see org.squashtest.tm.domain.campaign.CampaignLibraryNodeVisitor#visit(org.squashtest.tm.domain.campaign.CampaignFolder)
		 */
		@Override
		public void visit(CampaignFolder folder) {
			addFolderAttributes("campaign-folders");
			State state = (folder.hasContent() ? State.closed : State.leaf);
			builtNode.setState(state);
		}

		/**
		 * 
		 * @see org.squashtest.tm.domain.campaign.CampaignLibraryNodeVisitor#visit(org.squashtest.tm.domain.campaign.Campaign)
		 */
		@Override
		public void visit(Campaign campaign) {
			builtNode.addAttr("rel", "campaign");
			builtNode.addAttr("resType", "campaigns");
			State state = (campaign.hasIterations() ? State.closed : State.leaf);
			builtNode.setState(state);
		}
	}

	/**
	 * This visitor is used to populate the children of the currently built {@link JsTreeNode}
	 * 
	 * @author Gregory Fouquet
	 * 
	 */
	private class ChildrenPopulator implements CampaignLibraryNodeVisitor {
		private final JsTreeNode builtNode;

		public ChildrenPopulator(JsTreeNode builtNode) {
			super();
			this.builtNode = builtNode;
		}

		/**
		 * @see org.squashtest.tm.domain.testcase.TestCaseLibraryNodeVisitor#visit(org.squashtest.tm.domain.testcase.TestCase)
		 */
		@Override
		public void visit(Campaign visited) {
			if (visited.hasContent()) {
				builtNode.setState(State.open);

				IterationNodeBuilder childrenBuilder = new IterationNodeBuilder(permissionEvaluationService);

				List<JsTreeNode> children = new JsTreeNodeListBuilder<Iteration>(childrenBuilder)
						.expand(getExpansionCandidates())
						.setModel(visited.getOrderedContent())
						.build();

				builtNode.setChildren(children);
			}
		}

		/**
		 * @see org.squashtest.tm.domain.testcase.TestCaseLibraryNodeVisitor#visit(org.squashtest.tm.domain.testcase.TestCaseFolder)
		 */
		@Override
		public void visit(CampaignFolder visited) {
			if (visited.hasContent()) {
				builtNode.setState(State.open);

				CampaignLibraryTreeNodeBuilder childrenBuilder = new CampaignLibraryTreeNodeBuilder(
						permissionEvaluationService);

				List<JsTreeNode> children = new JsTreeNodeListBuilder<CampaignLibraryNode>(childrenBuilder)
						.expand(getExpansionCandidates())
						.setModel(visited.getOrderedContent())
						.build();

				builtNode.setChildren(children);
			}
		}

	}

	@Inject
	public CampaignLibraryTreeNodeBuilder(PermissionEvaluationService permissionEvaluationService) {
		super(permissionEvaluationService);
	}

	@Override
	protected void addCustomAttributes(CampaignLibraryNode libraryNode, JsTreeNode treeNode) {
		libraryNode.accept(new CustomAttributesPopulator(treeNode));

	}

	/**
	 * @see org.squashtest.tm.web.internal.model.builder.GenericJsTreeNodeBuilder#doAddChildren(org.squashtest.tm.web.internal.model.jstree.JsTreeNode,
	 *      org.squashtest.tm.domain.Identified)
	 */
	@Override
	protected void doAddChildren(JsTreeNode node, CampaignLibraryNode model) {
		model.accept(new ChildrenPopulator(node));

	}

}

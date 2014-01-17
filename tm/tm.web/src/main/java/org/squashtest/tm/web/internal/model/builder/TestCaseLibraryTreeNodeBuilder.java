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
package org.squashtest.tm.web.internal.model.builder;

import java.util.List;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNodeVisitor;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode.State;

/**
 * Builds a {@link JsTreeNode} from a TestCaseLibraryNode. Can be reused in the same thread.
 * 
 * @author Gregory Fouquet
 * 
 */
@Component
@Scope("prototype")
public class TestCaseLibraryTreeNodeBuilder extends LibraryTreeNodeBuilder<TestCaseLibraryNode> {
	
	
	protected VerifiedRequirementsManagerService verifiedRequirementsManagerService;

	/**
	 * This visitor is used to populate custom attributes of the {@link JsTreeNode} currently built
	 * 
	 */
	private class CustomAttributesPopulator implements TestCaseLibraryNodeVisitor {
		private final JsTreeNode builtNode;

		public CustomAttributesPopulator(JsTreeNode builtNode) {
			super();
			this.builtNode = builtNode;
		}

		/**
		 * @see org.squashtest.tm.domain.testcase.TestCaseLibraryNodeVisitor#visit(org.squashtest.tm.domain.testcase.TestCase)
		 */
		@Override
		public void visit(TestCase visited) {
			addLeafAttributes("test-case", "test-cases");
			builtNode.addAttr("status", visited.getStatus().toString().toLowerCase());
			builtNode.addAttr("importance", visited.getImportance().toString().toLowerCase());
			String req = "ko";
			if (!visited.getRequirementVersionCoverages().isEmpty() || verifiedRequirementsManagerService.testCaseHasUndirectRequirementCoverage(visited.getId())) {
				req = "ok";
			}
			builtNode.addAttr("req", req);
			if (visited.getReference() != null && visited.getReference().length() > 0) {
				builtNode.setTitle(visited.getReference() + " - " + visited.getName());
				builtNode.addAttr("reference", visited.getReference());

			} else {
				builtNode.setTitle(visited.getName());
			}
		}

		/**
		 * @see org.squashtest.tm.domain.testcase.TestCaseLibraryNodeVisitor#visit(org.squashtest.tm.domain.testcase.TestCaseFolder)
		 */
		@Override
		public void visit(TestCaseFolder visited) {
			addFolderAttributes("test-case-folders");
			State state = (visited.hasContent() ? State.closed : State.leaf);
			builtNode.setState(state);
		}

	}

	/**
	 * This visitor is used to populate the children of the currently built {@link JsTreeNode}
	 * 
	 * @author Gregory Fouquet
	 * 
	 */
	private class ChildrenPopulator implements TestCaseLibraryNodeVisitor {
		private final JsTreeNode builtNode;

		public ChildrenPopulator(JsTreeNode builtNode) {
			super();
			this.builtNode = builtNode;
		}

		/**
		 * @see org.squashtest.tm.domain.testcase.TestCaseLibraryNodeVisitor#visit(org.squashtest.tm.domain.testcase.TestCase)
		 */
		@Override
		public void visit(TestCase visited) {
			// noop
		}

		/**
		 * @see org.squashtest.tm.domain.testcase.TestCaseLibraryNodeVisitor#visit(org.squashtest.tm.domain.testcase.TestCaseFolder)
		 */
		@Override
		public void visit(TestCaseFolder visited) {
			if (visited.hasContent()) {
				builtNode.setState(State.open);

				TestCaseLibraryTreeNodeBuilder childrenBuilder = new TestCaseLibraryTreeNodeBuilder(
						permissionEvaluationService, verifiedRequirementsManagerService);

				List<JsTreeNode> children = new JsTreeNodeListBuilder<TestCaseLibraryNode>(childrenBuilder)
						.expand(getExpansionCandidates()).setModel(visited.getOrderedContent()).build();

				builtNode.setChildren(children);
			}
		}

	}

	@Inject
	public TestCaseLibraryTreeNodeBuilder(PermissionEvaluationService permissionEvaluationService, VerifiedRequirementsManagerService verifiedRequirementsManagerService) {
		super(permissionEvaluationService);
		this.verifiedRequirementsManagerService = verifiedRequirementsManagerService;
	}

	/**
	 * 
	 * @see org.squashtest.tm.web.internal.model.builder.LibraryTreeNodeBuilder#addCustomAttributes(org.squashtest.tm.domain.library.LibraryNode,
	 *      org.squashtest.tm.web.internal.model.jstree.JsTreeNode)
	 */
	@Override
	protected void addCustomAttributes(TestCaseLibraryNode libraryNode, JsTreeNode treeNode) {
		libraryNode.accept(new CustomAttributesPopulator(treeNode));
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.builder.GenericJsTreeNodeBuilder#doAddChildren(org.squashtest.tm.web.internal.model.jstree.JsTreeNode,
	 *      org.squashtest.tm.domain.Identified)
	 */
	@Override
	protected void doAddChildren(JsTreeNode node, TestCaseLibraryNode model) {
		model.accept(new ChildrenPopulator(node));
	}

}

/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.web.internal.model.builder;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNodeVisitor;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode.State;

/**
 * Builds a {@link JsTreeNode} from a {@link TestCaseLibraryNode}. Can be reused in the same thread.
 *
 * @author Gregory Fouquet
 *
 */
@Component
@Scope("prototype")
public class TestCaseLibraryTreeNodeBuilder extends LibraryTreeNodeBuilder<TestCaseLibraryNode> implements TestCaseLibraryNodeVisitor {

	@Inject
	public TestCaseLibraryTreeNodeBuilder(PermissionEvaluationService permissionEvaluationService) {
		super(permissionEvaluationService);
	}

	@Override
	public void visit(TestCase visited) {
		addLeafAttributes("test-cases");
	}

	@Override
	public void visit(TestCaseFolder visited) {
		addFolderAttributes("test-case-folders");
		State state = (visited.hasContent() ? State.closed : State.leaf);
		getBuiltNode().setState(state);
	}

	@Override
	protected void addCustomAttributes(TestCaseLibraryNode libraryNode, JsTreeNode treeNode) {
		libraryNode.accept(this);
	}

}

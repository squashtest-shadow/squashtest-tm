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
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNodeVisitor;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode;

@Component
@Scope("prototype")
public class RequirementLibraryTreeNodeBuilder extends LibraryTreeNodeBuilder<RequirementLibraryNode> implements
		RequirementLibraryNodeVisitor {

	@Inject
	public RequirementLibraryTreeNodeBuilder(PermissionEvaluationService permissionEvaluationService) {
		super(permissionEvaluationService);
	}

	@Override
	protected void addCustomAttributes(RequirementLibraryNode libraryNode, JsTreeNode treeNode) {
		libraryNode.accept(this);

	}

	@Override
	public void visit(RequirementFolder folder) {
		addFolderAttributes("requirement-folders");

	}

	@Override
	public void visit(Requirement requirement) {
		addLeafAttributes("requirements");

		if (requirement.getReference() != null && requirement.getReference().length() > 0) {
			getBuiltNode().setTitle(requirement.getReference() + " - " + requirement.getName());
			getBuiltNode().addAttr("reference", requirement.getReference());
		} else {
			getBuiltNode().setTitle(requirement.getName());
		}
	}

}

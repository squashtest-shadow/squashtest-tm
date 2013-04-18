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

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.helper.HyphenedStringHelper;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode.State;

/**
 * Builds a {@link JsTreeNode} representing a "drive" from a {@link Library}
 *
 * @author Gregory Fouquet
 *
 */
@Component
@Scope("prototype")
public class DriveNodeBuilder extends JsTreeNodeBuilder<Library<?>, DriveNodeBuilder> {

	@Inject
	protected DriveNodeBuilder(PermissionEvaluationService permissionEvaluationService) {
		super(permissionEvaluationService);
	}

	@Override
	protected void doBuild(JsTreeNode node, Library<?> model) {
		node.addAttr("rel", "drive");
		node.addAttr("resId", String.valueOf(model.getId()));
		node.addAttr("resType", buildResourceType(model.getClassSimpleName()));
		node.setState(model.hasContent() ? State.closed : State.leaf);
		node.setTitle(model.getProject().getName());
		node.addAttr("name", model.getClassSimpleName());
		node.addAttr("id", model.getClassSimpleName() + '-' + model.getId());
		node.addAttr("title", model.getProject().getLabel());
		node.addAttr("project", model.getProject().getId());
		node.addAttr("wizards", model.getEnabledPlugins());
	}

	private String buildResourceType(String classSimpleName) {
		String singleResourceType =  HyphenedStringHelper.camelCaseToHyphened(classSimpleName);
		return singleResourceType.replaceAll("y$", "ies");
	}

}

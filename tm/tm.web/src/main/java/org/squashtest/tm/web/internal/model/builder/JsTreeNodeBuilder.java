/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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

import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;

/**
 * Generic superclass for builders of {@link JsTreeNode}
 *
 * @author Gregory Fouquet
 *
 */
public abstract class JsTreeNodeBuilder<MODEL, BUILDER extends JsTreeNodeBuilder<MODEL, ?>> {
	private final PermissionEvaluationService permissionEvaluationService;
	private MODEL model;
	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	protected JsTreeNodeBuilder(PermissionEvaluationService permissionEvaluationService) {
		super();
		this.permissionEvaluationService = permissionEvaluationService;
	}

	@SuppressWarnings("unchecked")
	public final BUILDER setModel(MODEL model) {
		this.model = model;
		return (BUILDER) this;
	}

	public JsTreeNode build() {
		JsTreeNode node = new JsTreeNode();
		boolean editable = (permissionEvaluationService.hasRoleOrPermissionOnObject(ROLE_ADMIN, "WRITE", model)) ;
		node.addAttr("editable", String.valueOf(editable));
		boolean smallEdit =permissionEvaluationService.hasRoleOrPermissionOnObject(ROLE_ADMIN, "SMALL_EDIT", model) ;
		node.addAttr("smallEdit", String.valueOf(smallEdit));
		boolean creatable = permissionEvaluationService.hasRoleOrPermissionOnObject(ROLE_ADMIN, "CREATE", model);
		node.addAttr("creatable", String.valueOf(creatable));
		boolean deletable = permissionEvaluationService.hasRoleOrPermissionOnObject(ROLE_ADMIN, "DELETE", model);
		node.addAttr("deletable", String.valueOf(deletable));
		boolean executable = permissionEvaluationService.hasRoleOrPermissionOnObject(ROLE_ADMIN, "EXECUTE", model);
		node.addAttr("executable", String.valueOf(executable));
		doBuild(node, model);
		return node;
	}

	/**
	 * Implementors should "build" (ie populate) the given {@link JsTreeNode} from the model object.
	 *
	 * @param node
	 *            the node to populate
	 * @param model
	 *            the model used to build the node
	 */
	protected abstract void doBuild(JsTreeNode node, MODEL model);
}

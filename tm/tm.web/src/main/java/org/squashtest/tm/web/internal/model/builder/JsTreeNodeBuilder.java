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

import static org.squashtest.tm.api.security.acls.Permission.CREATE;
import static org.squashtest.tm.api.security.acls.Permission.DELETE;
import static org.squashtest.tm.api.security.acls.Permission.EXECUTE;
import static org.squashtest.tm.api.security.acls.Permission.SMALL_EDIT;
import static org.squashtest.tm.api.security.acls.Permission.WRITE;

import org.squashtest.tm.api.security.acls.Permission;
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
	private static final Permission[] NODE_PERMISSIONS = { WRITE, CREATE, DELETE, SMALL_EDIT, EXECUTE };

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

		for (Permission permission : NODE_PERMISSIONS) {
			boolean hasPermission = permissionEvaluationService.hasRoleOrPermissionOnObject(ROLE_ADMIN,
					permission.name(), model);
			node.addAttr(permission.getQuality(), String.valueOf(hasPermission));
		}

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

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

import org.apache.commons.lang.NullArgumentException;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode.State;

/**
 * Superclass which builds a {@link JsTreeNode} from a LibraryNode.
 *
 * @author Gregory Fouquet
 *
 */
public abstract class LibraryTreeNodeBuilder<T extends LibraryNode> {
	private final PermissionEvaluationService permissionEvaluationService;
	private T node;
	private JsTreeNode builtNode;

	public LibraryTreeNodeBuilder(PermissionEvaluationService permissionEvaluationService) {
		super();
		this.permissionEvaluationService = permissionEvaluationService;
	}

	/**
	 * Hook for template method {@link #build()}. Implementors should add to the {@link JsTreeNode} attributes specific
	 * to the {@link LibraryNode}.
	 *
	 * @param libraryNode
	 * @param treeNode
	 * @see #build()
	 */
	protected abstract void addCustomAttributes(T libraryNode, JsTreeNode treeNode);

	/**
	 * Adds to the node being build attributes for a leaf node
	 *
	 * @param resType
	 *            the nodeType attribute of the node
	 */
	protected final void addLeafAttributes(String resType) {
		builtNode.addAttr("rel", "file");
		builtNode.addAttr("resType", resType);
		builtNode.setState(State.leaf);
	}

	/**
	 * Adds to the node being build attributes for a folder node
	 *
	 * @param resType
	 */
	protected final void addFolderAttributes(String resType) {
		builtNode.addAttr("rel", "folder");
		builtNode.addAttr("resType", resType);
		builtNode.setState(State.closed);
	}

	/**
	 * Builds a {@link JsTreeNode} from the {@link LibraryNode} previously set with {@link #setNode(LibraryNode)}
	 *
	 * @return
	 */
	public final JsTreeNode build() {
		builtNode = new JsTreeNode();

		boolean editable = permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "SMALL_EDIT", node) ;
		builtNode.addAttr("smallEdit", String.valueOf(editable));
		boolean creatable = permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "CREATE", node);
		builtNode.addAttr("creatable", String.valueOf(creatable));
		boolean deletable = permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "DELETE", node);
		builtNode.addAttr("deletable", String.valueOf(deletable));
		addCommonAttributes();
		addCustomAttributes(node, builtNode);

		return builtNode;

	}

	private void addCommonAttributes() {
		String name = node.getName();
		builtNode.setTitle(name);
		builtNode.addAttr("name", name);
		builtNode.addAttr("resId", String.valueOf(node.getId()));
		builtNode.addAttr("id", node.getClass().getSimpleName() + '-' + node.getId());
	}

	/**s
	 * sets the {@link LibraryNode} which will be used to build a {@link JsTreeNode}
	 *
	 * @param node
	 * @return
	 */
	public final LibraryTreeNodeBuilder<T> setNode(T node) {
		if (node == null) {
			throw new NullArgumentException("node");
		}
		this.node = node;

		return this;
	}

	protected JsTreeNode getBuiltNode() {
		return builtNode;
	}

}
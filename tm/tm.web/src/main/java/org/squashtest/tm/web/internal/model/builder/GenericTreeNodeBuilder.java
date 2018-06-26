/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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

import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.api.security.acls.Permission;
import org.squashtest.tm.domain.tree.GenericTreeLibraryNode;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.security.PermissionEvaluationService;

import javax.inject.Inject;
import java.util.Map;

import static org.squashtest.tm.api.security.acls.Permission.*;

public abstract class GenericTreeNodeBuilder<NODE extends GenericTreeLibraryNode> {
	protected static final String ROLE_ADMIN = "ROLE_ADMIN";
	protected static final Permission[] NODE_PERMISSIONS = { WRITE, CREATE, DELETE, EXECUTE, EXPORT };
	protected static final String[] PERM_NAMES = {WRITE.name(), CREATE.name(), DELETE.name(), EXECUTE.name(), EXPORT.name()};

	private final PermissionEvaluationService permissionEvaluationService;

	@Inject
	public GenericTreeNodeBuilder(PermissionEvaluationService permissionEvaluationService){
		this.permissionEvaluationService = permissionEvaluationService;
	}

	public JsTreeNode buildGenericNode(NODE node){
		JsTreeNode builtNode = new JsTreeNode();
		builtNode.setTitle(HtmlUtils.htmlEscape(node.getName()));
		builtNode.addAttr("resId", String.valueOf(node.getId()));
		builtNode.addAttr("name", HtmlUtils.htmlEscape(node.getName()));

		//No milestone for custom report tree in first version so yes for all perm
		builtNode.addAttr("milestone-creatable-deletable", "true");
		builtNode.addAttr("milestone-editable", "true");

		doPermissionCheck(builtNode,node);

		return builtNode;
	}

	public abstract JsTreeNode build(NODE node);

	private void doPermissionCheck(JsTreeNode builtNode, NODE node){
		Map<String, Boolean> permByName = permissionEvaluationService.hasRoleOrPermissionsOnObject(ROLE_ADMIN, PERM_NAMES, node);
		for (Permission perm : NODE_PERMISSIONS) {
			builtNode.addAttr(perm.getQuality(), permByName.get(perm.name()).toString());
		}
	}

	protected void setStateForNodeContainer(JsTreeNode builtNode, TreeLibraryNode tln){
		if (tln.hasContent()) {
			builtNode.setState(JsTreeNode.State.closed);
		}
		else {
			builtNode.setState(JsTreeNode.State.leaf);
		}
	}

	protected void setNodeRel(JsTreeNode builtNode, String rel){
		builtNode.addAttr("rel", rel);
	}

	protected void setNodeResType(JsTreeNode builtNode, String resType){
		builtNode.addAttr("resType", resType);
	}

	protected void setNodeLeaf(JsTreeNode builtNode){
		builtNode.setState(JsTreeNode.State.leaf);
	}

	protected void setNodeHTMLId(JsTreeNode builtNode, String id){
		builtNode.addAttr("id", id);
	}


}

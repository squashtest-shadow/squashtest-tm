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

import static org.squashtest.tm.api.security.acls.Permission.CREATE;
import static org.squashtest.tm.api.security.acls.Permission.DELETE;
import static org.squashtest.tm.api.security.acls.Permission.EXECUTE;
import static org.squashtest.tm.api.security.acls.Permission.EXPORT;
import static org.squashtest.tm.api.security.acls.Permission.WRITE;

import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.api.security.acls.Permission;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportTreeDefinition;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode.State;

/**
 * No param/generic for v1, also no milestone in tree. 
 * These class should be completed and probably be generisized for future workspaces
 * @author jthebault
 *
 */
@Component
@Scope("prototype")
public class CustomReportTreeNodeBuilder {
	
	private final JsTreeNode builtNode = new JsTreeNode();
	
	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final Permission[] NODE_PERMISSIONS = { WRITE, CREATE, DELETE, EXECUTE, EXPORT };
	private static final String[] PERM_NAMES = {WRITE.name(), CREATE.name(), DELETE.name(), EXECUTE.name(), EXPORT.name()};

	protected final PermissionEvaluationService permissionEvaluationService;
	
	public CustomReportTreeNodeBuilder(PermissionEvaluationService permissionEvaluationService) {
		super();
		this.permissionEvaluationService = permissionEvaluationService;
	}

	public JsTreeNode build(CustomReportLibraryNode crln){
		builtNode.setTitle(crln.getName());
		builtNode.addAttr("resId", String.valueOf(crln.getId()));
		
		//No milestone for custom report tree in first version so yes for all perm
		builtNode.addAttr("milestone-creatable-deletable", "true");
		builtNode.addAttr("milestone-editable", "true");
		
		
		doPermissionCheck(crln);
		
		//A visitor would be elegant here and allow interface type development but we don't want hibernate to fetch each linked entity
		//for each node and we don't want subclass for each node type. sooooo the good old switch on enumerated type will do the job...
		CustomReportTreeDefinition entityType = (CustomReportTreeDefinition) crln.getEntityType();//NO SONAR the argument for this method is a CustomReportLibraryNode so entity type is a CustomReportTreeDefinition 
		
		switch (entityType) {
		case LIBRARY:
			doLibraryBuild(crln);
			break;
		case FOLDER:
			doFolderBuild(crln);
			break;
		case CHART:
			doChartBuild(crln);
			break;
		case DASHBOARD:
			doDashboardBuild(crln);
			break;
		default:
			throw new UnsupportedOperationException("The node builder isn't implemented for node of type : " + entityType);
		}
		
		return builtNode;
	} 

	private void doLibraryBuild(CustomReportLibraryNode crln) {
		builtNode.addAttr("rel", rel);
		builtNode.addAttr("resType", resType);
		setStateForNodeContainer(crln);
	}
	private void doLibraryBuild(CustomReportLibraryNode crln) {
		builtNode.addAttr("rel", "drive");
		builtNode.addAttr("resType", "custom-report-libraries");
		setStateForNodeContainer(crln);
	}

	private void doFolderBuild(CustomReportLibraryNode crln) {
		builtNode.addAttr("rel", "folder");
		builtNode.addAttr("resType", "custom-report-folders");
		setStateForNodeContainer(crln);
	}

	private void doChartBuild(CustomReportLibraryNode crln) {
		builtNode.addAttr("rel", "chart");
		builtNode.addAttr("resType", "custom-report-chart");
		builtNode.setState(State.leaf);
	}

	private void doDashboardBuild(CustomReportLibraryNode crln) {
		builtNode.addAttr("rel", "dashboard");
		builtNode.addAttr("resType", "custom-report-dashboard");
		setStateForNodeContainer(crln);
	}


	private void doPermissionCheck(CustomReportLibraryNode crln){
		Map<String, Boolean> permByName = permissionEvaluationService.hasRoleOrPermissionsOnObject(ROLE_ADMIN, PERM_NAMES, crln);
		for (Permission perm : NODE_PERMISSIONS) {
			builtNode.addAttr(perm.getQuality(), permByName.get(perm.name()).toString());
		}
	}
	
	private void setStateForNodeContainer(TreeLibraryNode tln){
		if (tln.hasContent()) {
			builtNode.setState(State.closed);
		}
		else {
			builtNode.setState(State.leaf);
		}
	}

}

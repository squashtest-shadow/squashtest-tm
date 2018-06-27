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


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportTreeDefinition;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State;

import javax.inject.Inject;

/**
 * No param/generic for v1, also no milestone in tree.
 * These class should be completed and probably be generisized for future workspaces
 * @author jthebault
 *
 */
@Component("customReport.nodeBuilder")
@Scope("prototype")
public class CustomReportTreeNodeBuilder extends GenericTreeNodeBuilder<CustomReportLibraryNode> {

	@Inject
	public CustomReportTreeNodeBuilder(PermissionEvaluationService permissionEvaluationService) {
		super(permissionEvaluationService);
	}

	@Override
	public JsTreeNode build(CustomReportLibraryNode crln){
		JsTreeNode builtNode = buildGenericNode(crln);

		//A visitor would be elegant here and allow interface type development but we don't want hibernate to fetch each linked entity
		//for each node and we don't want subclass for each node type. sooooo the good old switch on enumerated type will do the job...
		CustomReportTreeDefinition entityType = (CustomReportTreeDefinition) crln.getEntityType();//NO SONAR the argument for this method is a CustomReportLibraryNode so entity type is a CustomReportTreeDefinition

		switch (entityType) {
		case LIBRARY:
			doLibraryBuild(builtNode,crln);
			break;
		case FOLDER:
			doFolderBuild(builtNode,crln);
			break;
		case CHART:
			doChartBuild(builtNode,crln);
			break;
		case REPORT:
			doReportBuild(builtNode,crln);
			break;
		case DASHBOARD:
			doDashboardBuild(builtNode,crln);
			break;
		default:
			throw new UnsupportedOperationException("The node builder isn't implemented for node of type : " + entityType);
		}

		return builtNode;
	}

	private void doLibraryBuild(JsTreeNode builtNode, CustomReportLibraryNode crln) {
		setNodeHTMLId(builtNode, "CustomReportLibrary-"+crln.getId());
		setNodeRel(builtNode,"drive");
		setNodeResType(builtNode,"custom-report-libraries");
		setStateForNodeContainer(builtNode,crln);
	}

	private void doFolderBuild(JsTreeNode builtNode, CustomReportLibraryNode crln) {
		setNodeHTMLId(builtNode, "CustomReportFolder-"+crln.getId());
		setNodeRel(builtNode, "folder");
		setNodeResType(builtNode, "custom-report-folders");
		setStateForNodeContainer(builtNode, crln);
	}

	private void doChartBuild(JsTreeNode builtNode, CustomReportLibraryNode crln) {
		setNodeHTMLId(builtNode, "CustomReportChart-"+crln.getId());
		setNodeRel(builtNode, "chart");
		setNodeResType(builtNode, "custom-report-chart");
		setNodeLeaf(builtNode);
	}

	private void doReportBuild(JsTreeNode builtNode, CustomReportLibraryNode crln) {
		setNodeHTMLId(builtNode, "CustomReportReport-"+crln.getId());
		setNodeRel(builtNode, "report");
		setNodeResType(builtNode, "custom-report-report");
		setNodeLeaf(builtNode);
	}

	private void doDashboardBuild(JsTreeNode builtNode, CustomReportLibraryNode crln) {
		setNodeHTMLId(builtNode, "CustomReportDashboard-"+crln.getId());
		setNodeRel(builtNode, "dashboard");
		setNodeResType(builtNode, "custom-report-dashboard");
		setStateForNodeContainer(builtNode, crln);
	}





}

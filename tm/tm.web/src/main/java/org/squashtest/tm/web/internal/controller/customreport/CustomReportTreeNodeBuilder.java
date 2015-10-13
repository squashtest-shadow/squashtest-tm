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
package org.squashtest.tm.web.internal.controller.customreport;

import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportTreeDefinition;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;

/**
 * No param/generic for v1, also no milestone in tree. 
 * These class should be completed and probably be generisized for future workspaces
 * @author jthebault
 *
 */
public class CustomReportTreeNodeBuilder {
	
	private final JsTreeNode builtNode = new JsTreeNode();
	
	
	public JsTreeNode build(CustomReportLibraryNode tln){
		builtNode.setTitle(tln.getName());
		builtNode.addAttr("resId", String.valueOf(tln.getId()));
		
		//No milestone for custom report tree in first version so yes for all perm
		builtNode.addAttr("milestone-creatable-deletable", "true");
		builtNode.addAttr("milestone-editable", "true");
		
		//A visitor would be elegant here and allow interface type development but we don't want hibernate to fetch each linked entity
		//for each node and we don't want subclass for each node type. sooooo the good old switch on enumerated type will do the job...
		CustomReportTreeDefinition entityType = (CustomReportTreeDefinition) tln.getEntityType();//NO SONAR the argument for this method is a CustomReportLibraryNode so entity type is a CustomReportTreeDefinition 
		
		switch (entityType) {
		case LIBRARY:
			doLibraryBuild();
			break;
		case FOLDER:
			doFolderBuild();
			break;
		case CHART:
			doChartBuild();
			break;
		case DASHBOARD:
			doDashboardBuild();
			break;
		default:
			throw new UnsupportedOperationException("The node builder isn't implemented for node of type : " + entityType);
		}
		
		return builtNode;
	} 
	
	private void doLibraryBuild() {
		builtNode.addAttr("rel", "drive");
		builtNode.addAttr("resType", "custom-report-libraries");
	}

	private void doFolderBuild() {
		builtNode.addAttr("rel", "folder");
		builtNode.addAttr("resType", "custom-report-folders");
	}

	private void doChartBuild() {
		builtNode.addAttr("rel", "chart");
		builtNode.addAttr("resType", "custom-report-chart");
	}

	private void doDashboardBuild() {
		builtNode.addAttr("rel", "dashboard");
		builtNode.addAttr("resType", "custom-report-dashboard");
	}

}

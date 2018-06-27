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
package org.squashtest.tm.service.internal.customreport;

import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportTreeEntityVisitor;
import org.squashtest.tm.domain.report.ReportDefinition;
import org.squashtest.tm.domain.tree.GenericTreeLibraryNode;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.service.builder.GenericTreeLibraryNodeBuilder;

/**
 * Builder for new {@link CustomReportLibraryNode}.
 * Implement {@link CustomReportTreeEntityVisitor} if type dependent process is necessary
 * @author jthebault
 */
public class CustomReportLibraryNodeBuilder extends GenericTreeLibraryNodeBuilder<CustomReportLibraryNode> implements CustomReportTreeEntityVisitor {

	public CustomReportLibraryNodeBuilder(CustomReportLibraryNode parentNode,TreeEntity treeEntity) {
		builtNode = new CustomReportLibraryNode();
		this.treeEntity = treeEntity;
		this.parentNode = parentNode;
	}

	//--------------- SPECIFIC JOB TO EACH ENTITY TYPE --------------------

	@Override
	public void visit(CustomReportFolder crf) {
		linkToProject();
	}

	@Override
	public void visit(CustomReportLibrary crl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CustomReportDashboard crf) {
		linkToProject();

	}

	@Override
	public void visit(ChartDefinition chartDefinition) {
		linkToProject();

	}

	public void visit(ReportDefinition reportDefinition) {
		linkToProject();

	}


}

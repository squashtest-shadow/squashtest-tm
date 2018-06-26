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
package org.squashtest.tm.domain.customreport;

import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.report.ReportDefinition;
import org.squashtest.tm.domain.tree.TreeEntityVisitor;

public interface CustomReportTreeEntityVisitor extends TreeEntityVisitor {
	void visit(CustomReportFolder crf);
	void visit(CustomReportLibrary crl);
	void visit(CustomReportDashboard crf);
	void visit(ChartDefinition chartDefinition);
	void visit(ReportDefinition reportDefinition);
}

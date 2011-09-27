/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.domain.report.query;

import org.squashtest.csp.tm.domain.report.Report;


/**
 * A ReportQueryFactory will create a ReportQuery for a given Report. Usually one instance of a ReportQueryFactory will 
 * generate an instance of an implementation of a ReportQuery dedicated to :
 * 		- a given Report,
 * 		- a given persistence layer. 
 * 
 * 
 * Implementing a ReportQueryFactory :
 * ===================================
 * 
 * the method setReport allows the ReportQueryFactory to call setQueryFactory() of the target Report if needed.
 * the method makeReportQuery() must create an instance of an implementation of ReportQuery that must :
 * 		- match a particular Report, namely the one to which this ReportQueryFactory is attached,
 * 		- work for a given implementation of the underlying repository.
 * 
 * @author bsiri
 *
 */
public interface ReportQueryFactory {

	ReportQuery makeReportQuery();
	void setReport(Report report);

}

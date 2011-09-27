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
package org.squashtest.csp.tm.domain.report.common.hibernate;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.report.Report;
import org.squashtest.csp.tm.domain.report.query.ReportQuery;
import org.squashtest.csp.tm.domain.report.query.ReportQueryFactory;

/*
 * This ReportQueryFactory will return a HibernateReportQuery designed for a ExecutionProgress. So all this 
 * factory needs to do is to set itself to the right Report instance and return a HibernateExecutionProgressQuery on demand. 
 */
@Component("executionProgressQueryFactory")
public class ExecutionProgressQueryFactory implements
		ReportQueryFactory {

	@Override
	public ReportQuery makeReportQuery() {
		return new HibernateExecutionProgressQuery();		
	}

	@Override
	@Resource(name="reportExecutionProgressFollowUp")
	public void setReport(Report report) {
		report.setQueryFactory(this);
	}

}

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
package org.squashtest.csp.tm.domain.report.common;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.report.Report;
import org.squashtest.csp.tm.domain.report.ReportCategory;
import org.squashtest.csp.tm.domain.report.ReportType;
import org.squashtest.csp.tm.domain.report.view.ReportView;
import org.squashtest.csp.tm.domain.report.view.ReportViewCatalog;

@Component("reportExecutionProgressFollowUp")
public class ReportExecutionProgressFollowUp extends Report {

	{
		resourceKeyDescription = "squashtest.report.report.executionprogressfollowup.description";
		resourceKeyName = "squashtest.report.report.executionprogressfollowup.name";
		initViewCatalog();
	}

	@Inject
	@Named("reportTypeProgressFollowUp")
	@Override
	protected void setReportType(ReportType reportType) {
		this.reportType = reportType;
	}

	@Inject
	@Named("categoryExecutionPhase")
	@Override
	protected void setReportCategory(ReportCategory reportCategory) {
		this.reportCategory = reportCategory;
		this.reportCategory.addReport(this);
	}

	private void initViewCatalog() {
		ReportViewCatalog viewCatalog = new ReportViewCatalog();
		// REMOVED XLS OPTION UNTIL THE BUG 680 IS FIXED
		// ReportView view1 = new ReportView().setTitleKey("squashtest.report.view.title.listtcbycampaign")
		// .setCodeKey("squashtest.report.view.code.datatable").setFormats("xls", "ods", "csv", "pdf", "html")
		// .setModel("executionProgression1");
		ReportView view1 = new ReportView().setTitleKey("squashtest.report.view.title.listtcbycampaign")
				.setCodeKey("squashtest.report.view.code.datatable").setFormats("ods", "csv", "pdf", "html")
				.setModel("executionProgression1");
		ReportView view2 = new ReportView().setTitleKey("squashtest.report.view.title.campaigndashboard")
				.setCodeKey("squashtest.report.view.code.dashboard").setFormats("pdf", "html")
				.setModel("executionProgression2");

		viewCatalog.addView(view2);
		viewCatalog.addView(view1);

		viewCatalog.setDefaultViewIndex(0);

		setViewCatalog(viewCatalog);

	}

}

/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.testcase.executions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.web.internal.controller.campaign.TestSuiteHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;

/**
 * Builder of {@link DataTableModel} for the table of a test case's executions.
 * 
 * @author Gregory Fouquet
 * 
 */
/* package-private */class ExecutionsTableModelBuilder extends
		DataTableModelBuilder<Execution> {
	/**
	 * The locale to use to format the labels.
	 */
	private final Locale locale;
	/**
	 * The source for localized label messages.
	 */
	private final InternationalizationHelper i18nHelper;

	public ExecutionsTableModelBuilder(@NotNull Locale locale,
			@NotNull InternationalizationHelper i18nHelper) {
		super();
		this.locale = locale;
		this.i18nHelper = i18nHelper;
	}

	@Override
	protected Object buildItemData(Execution item) {
		IterationTestPlanItem testPlanItem = item.getTestPlan();
		Iteration iteration = testPlanItem.getIteration();

		Map<String, Object> data = new HashMap<String, Object>(11);
		data.put("exec-id", item.getId());
		data.put("project-name", iteration.getProject().getName());
		data.put("campaign-name", iteration.getCampaign().getName());
		data.put("iteration-name", iteration.getName());
		data.put("exec-name", item.getName());
		data.put("exec-mode", i18nHelper.internationalize(item
				.getExecutionMode().getI18nKey(), locale));
		data.put("test-suite-name", testSuiteNameList(testPlanItem));
		data.put("raw-exec-status", item.getExecutionStatus().name());
		data.put("exec-status", i18nHelper.internationalize(item
				.getExecutionStatus().getI18nKey(), locale));
		data.put("last-exec-by", item.getLastExecutedBy());
		data.put("last-exec-on",
				i18nHelper.localizeShortDate(item.getLastExecutedOn(), locale));

		return data;
	}

	private String testSuiteNameList(IterationTestPlanItem item) {
		return TestSuiteHelper.buildEllipsedSuiteNameList(item.getTestSuites(), 20);
	}

}

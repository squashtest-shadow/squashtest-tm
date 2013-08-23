/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.campaign;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;

class IterationViewTestPlanTableModelHelper extends DataTableModelBuilder<IterationTestPlanItem> {

	private InternationalizationHelper messageSource;
	private Locale locale;

	IterationViewTestPlanTableModelHelper(InternationalizationHelper messageSource, Locale locale) {
		this.messageSource = messageSource;
		this.locale = locale;
	}

	@Override
	public Map<String, Object> buildItemData(IterationTestPlanItem item) {

		Map<String, Object> res = new HashMap<String, Object>();

		String projectName;
		String testCaseName;
		Long tcId;
		String importance;
		String reference;
		String datasetName;
		final String automationMode = item.isAutomated() ? "A" : "M";

		String testSuiteNameList = "";
		
		User assignee = item.getUser();
		Long assigneeId = User.NO_USER_ID;
		String assigneeLogin = formatString(item.getLastExecutedBy(), locale);
		
		if  (assignee  != null) {
			assigneeId = assignee.getId();
			assigneeLogin = assignee.getLogin();
		}

		if (item.isTestCaseDeleted()) {
			projectName = formatNoData(locale);
			testCaseName = formatDeleted(locale);
			tcId = null;
			importance = formatNoData(locale);
			reference = formatNoData(locale);
		} else {
			projectName = item.getReferencedTestCase().getProject().getName();
			testCaseName = item.getReferencedTestCase().getName();
			tcId = item.getReferencedTestCase().getId();
			reference = item.getReferencedTestCase().getReference();
			importance = messageSource.internationalize(item.getReferencedTestCase().getImportance(), locale);
		}

		if (item.getReferencedDataset() == null) {
			datasetName = formatNoData(locale);
		} else {
			datasetName = item.getReferencedDataset().getName();
		}

		if (item.getTestSuites().isEmpty()) {
			testSuiteNameList = formatNone(locale);
		} else {
			testSuiteNameList = TestSuiteHelper.buildEllipsedSuiteNameList(item.getTestSuites(), 20);
		}

		res.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
		res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
		res.put("project-name", projectName);
		res.put("reference", reference);
		res.put("tc-id", tcId);
		res.put("tc-name", testCaseName);
		res.put("importance", importance);
		res.put("suite", testSuiteNameList);
		res.put("status",item.getExecutionStatus());
		res.put("assignee-id", assigneeId);
		res.put("assignee-login", assigneeLogin);
		res.put("last-exec-on", messageSource.localizeDate(item.getLastExecutedOn(), locale));
		res.put("is-tc-deleted", item.isTestCaseDeleted());
		res.put(DataTableModelConstants.DEFAULT_EMPTY_EXECUTE_HOLDER_KEY, " ");
		res.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
		res.put("exec-mode", automationMode);
		res.put("dataset", datasetName);

		return res;
	}

	/* ***************** data formatter *************************** */

	private String formatString(String arg, Locale locale) {
		return messageSource.messageOrNoData(arg, locale);
	}

	private String formatNoData(Locale locale) {
		return messageSource.noData(locale);
	}

	private String formatDeleted(Locale locale) {
		return messageSource.itemDeleted(locale);
	}

	private String formatNone(Locale locale) {
		return messageSource.internationalize("squashtm.none.f", locale);
	}
}
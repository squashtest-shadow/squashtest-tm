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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.squashtest.tm.core.foundation.lang.IsoDateUtils;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;

class TestPlanTableModelHelper extends DataTableModelBuilder<IndexedIterationTestPlanItem> {

	private InternationalizationHelper messageSource;
	private Locale locale;

	TestPlanTableModelHelper(InternationalizationHelper messageSource, Locale locale) {
		this.messageSource = messageSource;
		this.locale = locale;
	}

	@Override
	public Map<String, Object> buildItemData(IndexedIterationTestPlanItem indexedItem) {

		Integer index = indexedItem.getIndex() + 1;
		IterationTestPlanItem item = indexedItem.getItem();
		
		Map<String, Object> res = new HashMap<String, Object>();
		
		//automation mode
		final String automationMode = item.isAutomated() ? "A" : "M";

		//assigne
		User assignee = item.getUser();
		Long assigneeId = User.NO_USER_ID;
		String assigneeLogin = formatString(item.getLastExecutedBy(), locale);		
		if  (assignee  != null) {
			assigneeId = assignee.getId();
			assigneeLogin = assignee.getLogin();
		}
		
		//if test case deleted 
		String projectName;
		String testCaseName;
		Long tcId;
		String importance;
		String reference;
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
			if(item.getReferencedTestCase().getReference().isEmpty()){
				reference = formatNoData(locale);
			}else{
				reference = item.getReferencedTestCase().getReference();
			}
			importance = messageSource.internationalize(item.getReferencedTestCase().getImportance(), locale);
		}
		
		
		//dataset
		String datasetName;
		if (item.getReferencedDataset() == null) {
			datasetName = formatNoData(locale);
		} else {
			datasetName = item.getReferencedDataset().getName();
		}
		// test suite name
		String testSuiteNameList = "";
		// test suite name
		List<Long> testSuiteIdsList;
		if (item.getTestSuites().isEmpty()) {
			testSuiteNameList = formatNoData(locale);
			testSuiteIdsList = Collections.emptyList();
		} else {
			testSuiteNameList = TestSuiteHelper.buildEllipsedSuiteNameList(item.getTestSuites(), 20);
			testSuiteIdsList = IdentifiedUtil.extractIds(item.getTestSuites());
		}
		
		

		res.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
		res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, index);
		res.put("project-name", projectName);
		res.put("reference", reference);
		res.put("tc-id", tcId);
		res.put("tc-name", testCaseName);
		res.put("importance", importance);
		res.put("suite", testSuiteNameList);
		res.put("suiteIds", testSuiteIdsList);
		res.put("status",item.getExecutionStatus());
		res.put("assignee-id", assigneeId);
		res.put("assignee-login", assigneeLogin);
		res.put("last-exec-on", IsoDateUtils.formatIso8601DateTime(item.getLastExecutedOn()));
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

}
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
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;

class TestSuiteTestPlanTableModelHelper extends DataTableModelBuilder<IterationTestPlanItem> {
	private InternationalizationHelper messageSource;
	private Locale locale;

	TestSuiteTestPlanTableModelHelper(InternationalizationHelper messageSource, Locale locale) {
		this.messageSource = messageSource;
		this.locale = locale;
	}

	@Override
	public Map<String, Object> buildItemData(IterationTestPlanItem item) {

		String projectName;
		String testCaseName;
		String reference;
		String importance;
		final String automationMode = item.isAutomated() ? "A" : "M";

		if (item.isTestCaseDeleted()) {
			projectName = formatNoData(locale);
			testCaseName = formatDeleted(locale);
			importance = formatNoData(locale);
			reference = formatNoData(locale);
		} else {
			projectName = item.getReferencedTestCase().getProject().getName();
			testCaseName = item.getReferencedTestCase().getName();
			reference = item.getReferencedTestCase().getReference();
			importance = messageSource.internationalize(item.getReferencedTestCase().getImportance(), locale);
		}

		// ugly copypasta from IterationThingieBuilder
		String datasetName;
		if (item.getReferencedDataset() == null) {
			datasetName = formatNoData(locale);
		} else {
			datasetName = item.getReferencedDataset().getName();
		}

		Map<String, Object> rowMap = new HashMap<String, Object>(14);

		rowMap.put("entity-id", item.getId());
		rowMap.put("entity-index", getCurrentIndex());
		rowMap.put("project-name", projectName);
		rowMap.put("exec-mode", automationMode);
		rowMap.put("reference", reference);
		rowMap.put("tc-name", testCaseName);
		rowMap.put("importance", importance);
		rowMap.put("status", messageSource.internationalize(item.getExecutionStatus(), locale));
		rowMap.put("last-exec-by", formatString(item.getLastExecutedBy(), locale));
		rowMap.put("last-exec-on", messageSource.localizeDate(item.getLastExecutedOn(), locale));
		rowMap.put("is-tc-deleted", item.isTestCaseDeleted());
		rowMap.put("empty-execute-holder", null);
		rowMap.put("empty-delete-holder", null);
		rowMap.put("dataset", datasetName);

		return rowMap;

	}

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
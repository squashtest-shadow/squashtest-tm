/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.controller.campaign;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.squashtest.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.IndexedCampaignTestPlanItem;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;

final class CampaignTestPlanTableModelHelper extends DataTableModelBuilder<IndexedCampaignTestPlanItem> {

	private Locale locale;
	private InternationalizationHelper messageSource;

	CampaignTestPlanTableModelHelper(InternationalizationHelper messageSource, Locale locale) {
		this.messageSource = messageSource;
		this.locale = locale;
	}
	

	private String formatNoData(Locale locale) {
		return messageSource.noData(locale);
	}

	public Map<String, Object> buildItemData(IndexedCampaignTestPlanItem indexedItem) {

		Integer index = indexedItem.getIndex() + 1;
		CampaignTestPlanItem item = indexedItem.getItem();
		
		Map<String, Object> result = new HashMap<String, Object>();

		TestCase testCase = item.getReferencedTestCase();
		String user = (item.getUser() != null) ? item.getUser().getLogin() : formatNoData(locale);
		Long assigneeId = (item.getUser() != null) ? item.getUser().getId() : User.NO_USER_ID;
		String reference = (testCase.getReference().isEmpty()) ? formatNoData(locale) : testCase.getReference();
		
		result.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
		result.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, index);
		result.put("project-name", testCase.getProject().getName());
		result.put("reference", reference);
		result.put("tc-name", testCase.getName());
		result.put("assigned-user", user);
		result.put("assigned-to", assigneeId);
		result.put("importance", formatImportance(testCase.getImportance(), locale));
		result.put("exec-mode", testCase.isAutomated() ? "A" : "M");
		result.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
		result.put("tc-id", testCase.getId());

		return result;

	}
	

	private String formatImportance(TestCaseImportance importance, Locale locale) {
		return messageSource.internationalize(importance, locale);
	}


}
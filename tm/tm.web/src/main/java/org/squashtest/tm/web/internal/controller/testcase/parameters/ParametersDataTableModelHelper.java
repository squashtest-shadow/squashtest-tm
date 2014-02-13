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
package org.squashtest.tm.web.internal.controller.testcase.parameters;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;

/**
 * Helps create the datas (for the jQuery DataTable) for the parameters table in the test case view.
 * 
 * @author mpagnon
 * 
 */
public final class ParametersDataTableModelHelper extends DataTableModelBuilder<Parameter> {

	private long ownerId;
	private MessageSource messageSource;
	private Locale locale;

	public ParametersDataTableModelHelper(long ownerId, MessageSource messageSource, Locale locale) {
		super();

		this.ownerId = ownerId;
		this.messageSource = messageSource;
		this.locale = locale;
	}

	@Override
	public Map<String, Object> buildItemData(Parameter item) {
		Map<String, Object> res = new HashMap<String, Object>();
		String testCaseName = buildTestCaseName(item);
		res.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
		res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
		res.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY,
				ParametersDataTableModelHelper.buildParameterName(item, ownerId, messageSource, locale));
		res.put("description", item.getDescription());
		res.put("test-case-name", testCaseName);
		res.put("directly-associated", Long.valueOf(ownerId).equals(item.getTestCase().getId()));
		res.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, "");
		return res;
	}

	public static String buildParameterName(Parameter item, Long ownerId2, MessageSource messageSource2, Locale locale2) {
		String tcSmall = messageSource2.getMessage("label.testCases.short", null, locale2);
		TestCase paramTC = item.getTestCase();
		if (!ownerId2.equals(paramTC.getId())) {
			return item.getName() + " (" + tcSmall + "_" + paramTC.getId() + ")";
		} else {
			return item.getName();
		}
	}

	/**
	 * Will build the test case name for display in the table. The name will be : tReference-tcName (tcProjectName)
	 * 
	 * @param item
	 * @return
	 */
	public static String buildTestCaseName(Parameter item) {
		TestCase testCase = item.getTestCase();
		Project project = testCase.getProject();
		String testCaseName = testCase.getName() + " (" + project.getName() + ')';
		if (testCase.getReference().length() > 0) {
			testCaseName = testCase.getReference() + '-' + testCaseName;
		}
		return testCaseName;
	}

}
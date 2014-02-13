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
package org.squashtest.tm.web.internal.controller.testcase;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.i18n.LocaleContextHolder;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;

class CallingTestCasesTableModelBuilder extends DataTableModelBuilder<TestCase> {

	private InternationalizationHelper i18nHelper;
	private Locale locale = LocaleContextHolder.getLocale();
	
	CallingTestCasesTableModelBuilder(InternationalizationHelper i18nHelper){
		this.i18nHelper = i18nHelper;
	}
	
	@Override
	protected Map<String, String> buildItemData(TestCase tc) {
		
		Map<String, String> row = new HashMap<String, String>(6);
		String executionMode = i18nHelper.internationalize(tc.getExecutionMode(), locale);
		
		row.put("tc-id", Long.toString(tc.getId()));
		row.put("tc-index", Long.toString(getCurrentIndex()));
		row.put("project-name", tc.getProject().getName());
		row.put("tc-reference", tc.getReference());
		row.put("tc-name", tc.getName());
		row.put("tc-mode", executionMode);
		
		return row;
		
	}

}

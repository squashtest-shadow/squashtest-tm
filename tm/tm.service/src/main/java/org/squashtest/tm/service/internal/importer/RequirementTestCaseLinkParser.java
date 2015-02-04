/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.internal.importer;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.ColumnHeaderNotFoundException;

interface RequirementTestCaseLinkParser {
	
	String ID_REQUIREMENT_TAG = "ID_REQ";
	String ID_TEST_CASE_TAG = "ID_TC";
	String VERSION_TAG = "VERSION";

	void parseRow( Row row, ImportRequirementTestCaseLinksSummaryImpl summary, Map<String, Integer> columnsMapping, Map<TestCase, List<RequirementVersion>> requirementVersionsByTestCaseList) ;
	
	/**
	 * @throws ColumnHeaderNotFoundException if one mandatory column is not found, and the summary will be filled accordingly.
	 * @param columnsMapping
	 */
	void checkColumnsMapping(Map<String, Integer> columnsMapping, ImportRequirementTestCaseLinksSummaryImpl summary);
	
}

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

package org.squashtest.tm.service.internal.batchimport.testcase.excel;

import java.util.HashMap;
import java.util.Map;

/**
 * Metadata of a test case import workbook. It collects data about the worksheets and the columns we have to process.
 * 
 * @author Gregory Fouquet
 * 
 */
public class WorkbookMetaData {
	private Map<TemplateWorksheet, WorksheetDef> worksheetDefs = new HashMap<TemplateWorksheet, WorksheetDef>();

	/**
	 * @param worksheetDef
	 */
	public void addWorksheetDef(WorksheetDef worksheetDef) {
		worksheetDefs.put(worksheetDef.getWorksheetType(), worksheetDef);
		
	}
}
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

/**
 * Definition of a worksheet that is to be processd by the importer.
 * 
 * @author Gregory Fouquet
 * 
 */
class WorksheetDef<COL extends TemplateColumn> {
	private final TemplateWorksheet worksheetType;
	private final Map<COL, ColumnDef<COL>> columnDefs = new HashMap<COL, ColumnDef<COL>>();

	public WorksheetDef(@NotNull TemplateWorksheet worksheetType) {
		super();
		this.worksheetType = worksheetType;
	}

	/**
	 * @return the worksheetType
	 */
	public TemplateWorksheet getWorksheetType() {
		return worksheetType;
	}

	/**
	 * @param columnDef
	 */
	void addColumnDef(@NotNull ColumnDef<COL> columnDef) {
		columnDefs.put(columnDef.getType(), columnDef);

	}

	/**
	 * 
	 */
	void validate() throws TemplateMismatchException {
		List<MissingMandatoryColumnMismatch> mmces = new ArrayList<MissingMandatoryColumnMismatch>();

		for (TemplateColumn col : worksheetType.getColumnTypes()) {
			if (isMandatory(col) && noColumnDef(col)) {
				mmces.add(new MissingMandatoryColumnMismatch(col));
			}
		}

		if (!mmces.isEmpty()) {
			throw new TemplateMismatchException(mmces);
		}
	}

	private boolean isMandatory(TemplateColumn col) {
		return ColumnProcessingMode.MANDATORY.equals(col.getProcessingMode());
	}

	private boolean noColumnDef(TemplateColumn col) {
		return columnDefs.get(col) == null;
	}
}

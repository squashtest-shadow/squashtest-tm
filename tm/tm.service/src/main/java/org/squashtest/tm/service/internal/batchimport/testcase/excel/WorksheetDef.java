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
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.service.batchimport.excel.ColumnMismatch;
import org.squashtest.tm.service.batchimport.excel.WorksheetFormatStatus;

/**
 * Definition of a worksheet that is to be processd by the importer.
 * 
 * @author Gregory Fouquet
 * 
 */
class WorksheetDef<COL extends TemplateColumn> {
	private static final Logger LOGGER = LoggerFactory.getLogger(WorksheetDef.class);

	private final TemplateWorksheet worksheetType;
	private final Map<COL, StdColumnDef<COL>> stdColumnDefs = new HashMap<COL, StdColumnDef<COL>>();
	private final List<CustomFieldColumnDef> customFieldDefs = new ArrayList<CustomFieldColumnDef>();

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
	private void addColumnDef(@NotNull StdColumnDef<COL> columnDef) {
		//TODO check for duplicate column definitions and return a template Mismatch
		stdColumnDefs.put(columnDef.getType(), columnDef);

	}

	/**
	 * Validates this {@link WorksheetDef}. Unrecoverable mismatches from template will throw an exception.
	 * 
	 * @returns {@link WorksheetFormatStatus}
	 *            that holds the possible Column mismatches
	 */
	WorksheetFormatStatus validate()  {

		List<TemplateColumn> missingMandatoryColumnMismatch = new ArrayList<TemplateColumn>();

		for (TemplateColumn col : worksheetType.getColumnTypes()) {
			if (isMandatory(col) && noColumnDef(col)) {
				missingMandatoryColumnMismatch.add(col);
			}
		}
		WorksheetFormatStatus worksheetStatus = new WorksheetFormatStatus(worksheetType);
		worksheetStatus.addMismatches(ColumnMismatch.MISSING_MANDATORY, missingMandatoryColumnMismatch);
		return worksheetStatus;
	}

	private boolean isMandatory(TemplateColumn col) {
		return ColumnProcessingMode.MANDATORY.equals(col.getProcessingMode());
	}

	private boolean noColumnDef(TemplateColumn col) {
		return stdColumnDefs.get(col) == null;
	}

	public boolean isCustomFieldHeader(String header) {
		return parseCustomFieldHeader(header) != null;
	}

	/**
	 * Adds a column. This should not be used after build time / validation.
	 * 
	 * @param header
	 * @param colIndex
	 * @return
	 */
	@SuppressWarnings("unchecked")
	ColumnDef addColumnDef(String header, int colIndex) {
		ColumnDef res = null;

		COL colType = (COL) TemplateColumnUtils.coerceFromHeader(worksheetType.columnTypesClass, header);

		if (colType != null) {
			LOGGER.trace("Column named '{}' will be added to metamodel as standard column {}", header, colType);
			res = new StdColumnDef<COL>(colType, colIndex);
			addColumnDef((StdColumnDef<COL>) res);

		} else if (isCustomFieldHeader(header)) {
			LOGGER.trace("Column named '{}' will be added to metamodel as custom field", header);
			res = new CustomFieldColumnDef(parseCustomFieldHeader(header), colIndex);
			//TODO check for duplicate columns
			getCustomFieldDefs().add((CustomFieldColumnDef) res);

		} else {
			LOGGER.trace("Column named '{}' will be ignored", header);
			// else unknown columns are ditched
		}

		return res;
	}

	private String parseCustomFieldHeader(String header) {
		return worksheetType.customFieldPattern.parseFieldCode(header);
	}

	public StdColumnDef<COL> getColumnDef(COL col) {
		return stdColumnDefs.get(col);
	}

	public List<StdColumnDef<COL>> getImportableColumnDefs() {
		List<StdColumnDef<COL>> res = new ArrayList<StdColumnDef<COL>>(stdColumnDefs.size());

		for (Entry<COL, StdColumnDef<COL>> entry : stdColumnDefs.entrySet()) {
			if (!isIgnored(entry.getKey())) {
				res.add(entry.getValue());
			}
		}

		return res;
	}

	/**
	 * @param key
	 * @return
	 */
	private boolean isIgnored(COL col) {
		return ColumnProcessingMode.IGNORED.equals(col.getProcessingMode());
	}

	/**
	 * Name of the worksheet in the workbook
	 * 
	 * @return
	 */
	public String getSheetName() {
		return worksheetType.sheetName;
	}

	/**
	 * @return the customFieldDefs
	 */
	public List<CustomFieldColumnDef> getCustomFieldDefs() {
		return customFieldDefs;
	}
}

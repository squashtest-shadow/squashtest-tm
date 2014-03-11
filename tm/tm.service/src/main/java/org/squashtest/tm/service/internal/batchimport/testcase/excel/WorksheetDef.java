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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		stdColumnDefs.put(columnDef.getType(), columnDef);

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
		return stdColumnDefs.get(col) == null;
	}

	public boolean isCustomFieldHeader(String header) {
		return parseCustomFieldHeader(header) != null;
	}

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
			customFieldDefs.add((CustomFieldColumnDef) res);

		} else {
			LOGGER.trace("Column named '{}' will be ignored", header);
			// else unknown columns are ditched
		}

		return res;
	}

	private String parseCustomFieldHeader(String header) {
		return worksheetType.customFieldPattern.parseFieldCode(header);
	}
}

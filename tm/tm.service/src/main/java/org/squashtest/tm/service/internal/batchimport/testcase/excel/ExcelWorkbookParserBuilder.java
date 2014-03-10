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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.exception.SheetCorruptedException;

/**
 * Builds an excel parser. It checks the structure of the excel file and configures the parser accordingly.
 * 
 * @author Gregory Fouquet
 * 
 */
class ExcelWorkbookParserBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExcelWorkbookParserBuilder.class);
	private final File xls;

	public ExcelWorkbookParserBuilder(@NotNull File xls) {
		super();
		this.xls = xls;
	}

	public ExcelWorkbookParser build() throws SheetCorruptedException, TemplateMismatchException {
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(xls));
		} catch (FileNotFoundException e) {
			IOUtils.closeQuietly(is);
			throw new RuntimeException(e);
		}
		Workbook wb = openWorkbook(is);
		WorkbookMetaData wmd = buildMetaData(wb);
		wmd.validate();

		return new ExcelWorkbookParser();
	}

	/**
	 * @param wb
	 * @return
	 */
	private WorkbookMetaData buildMetaData(Workbook wb) {
		WorkbookMetaData wmd = new WorkbookMetaData();
		processSheets(wb, wmd);

		return wmd;
	}

	@SuppressWarnings({ "rawtypes" })
	private void processSheets(Workbook wb, WorkbookMetaData wmd) {
		for (int iSheet = 0; iSheet < wb.getNumberOfSheets(); iSheet++) {
			Sheet ws = wb.getSheetAt(iSheet);
			TemplateWorksheet sheetType = TemplateWorksheet.coerceFromSheetName(ws.getSheetName());

			if (sheetType != null) {
				WorksheetDef<?> wd = new WorksheetDef(sheetType);
				wmd.addWorksheetDef(wd);
				populateColumnDefs(wd, ws);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void populateColumnDefs(WorksheetDef<?> wd, Sheet ws) {
		Row headerRow = findHeaderRow(ws);

		if (headerRow == null) {
			return;
		}

		for (int iCell = 0; iCell < headerRow.getLastCellNum(); iCell++) {
			Cell cell = headerRow.getCell(iCell);
			try {
				String header = cell.getStringCellValue();

				TemplateColumn colType = TemplateColumnUtils.coerceFromHeader(wd.getWorksheetType().columnTypesClass,
						header);

				if (colType != null) {
					wd.addColumnDef(new ColumnDef(colType, iCell));
				} else {
					// TODO PROCESS CUSTOM FIELDS HEADERS !
					// unknown columns are ditched
				}

			} catch (IllegalStateException e) {
				// seems this cell aint a string cell...
				LOGGER.trace(
						"We expected a string cell, but it was not. Not an error case so we silently skip it. Exception message : {}",
						e.getMessage());
			}
		}
	}

	/**
	 * 
	 * @param ws
	 * @return header row or <code>null</code>
	 */
	private Row findHeaderRow(Sheet ws) {
		Row headerRow = ws.getRow(0);
		return headerRow;
	}

	private Workbook openWorkbook(InputStream is) throws SheetCorruptedException {
		try {
			return WorkbookFactory.create(is);

		} catch (InvalidFormatException e) {
			LOGGER.info(e.getMessage());
			IOUtils.closeQuietly(is);
			throw new SheetCorruptedException(e);

		} catch (IOException e) {
			LOGGER.info(e.getMessage());
			IOUtils.closeQuietly(is);
			throw new SheetCorruptedException(e);

		} catch (IllegalArgumentException e) {
			LOGGER.info(e.getMessage());
			IOUtils.closeQuietly(is);
			throw new SheetCorruptedException(e);
		}
	}

}

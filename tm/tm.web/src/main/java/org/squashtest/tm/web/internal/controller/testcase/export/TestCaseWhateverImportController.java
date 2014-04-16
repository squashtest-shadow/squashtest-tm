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
package org.squashtest.tm.web.internal.controller.testcase.export;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.lang.IsoDateUtils;
import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.ImportLog;
import org.squashtest.tm.service.importer.LogEntry;

@Controller
@RequestMapping("/testcase-import")
public class TestCaseWhateverImportController {

	@Inject
	private MessageSource messageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseWhateverImportController.class);

	private void writeToTab(Collection<LogEntry> entries, XSSFWorkbook workbook, String sheetName, Locale locale) {

		// Create a blank sheet
		XSSFSheet sheet = workbook.createSheet(sheetName);

		writeHeaderToTab(sheet);
		writeEntriesToTab(entries, sheet, locale);

	}

	private void writeHeaderToTab(XSSFSheet sheet) {
		Row row = sheet.createRow(0);
		int cellnum = 0;
		Cell cell = row.createCell(cellnum++);
		writeValueToCell(cell, "LINE");
		cell = row.createCell(cellnum++);
		writeValueToCell(cell, "TYPE");
		cell = row.createCell(cellnum++);
		writeValueToCell(cell, "ID");
		cell = row.createCell(cellnum++);
		writeValueToCell(cell, "ERROR");
		cell = row.createCell(cellnum++);
		writeValueToCell(cell, "IMPACT");
	}

	private void writeValueToCell(Cell cell, Object value) {
	}

	private void writeEntriesToTab(Collection<LogEntry> entries, XSSFSheet sheet, Locale locale) {

		int rownum = 1;
		for (LogEntry entry : entries) {
			Row row = sheet.createRow(rownum++);
			int cellnum = 0;
			Cell cell = row.createCell(cellnum++);
			writeValueToCell(cell, entry.getLine());
			cell = row.createCell(cellnum++);
			writeValueToCell(cell, entry.getStatus().shortName());
			cell = row.createCell(cellnum++);
			writeValueToCell(cell, "MISSING INFO");
			cell = row.createCell(cellnum++);
			writeValueToCell(cell, messageSource.getMessage(entry.getI18nError(), null, locale));
			cell = row.createCell(cellnum++);
			writeValueToCell(cell, messageSource.getMessage(entry.getI18nImpact(), null, locale));
		}
	}

	@RequestMapping(method = RequestMethod.GET, params = { "importLog", "export=csv" })
	public @ResponseBody
	void writeImportController(ImportLog importLog, HttpServletResponse response, Locale locale) throws IOException {

		// Building the workbook
		XSSFWorkbook workbook = new XSSFWorkbook();

		Collection<LogEntry> logEntriesForTestcases = importLog.findAllFor(EntityType.TEST_CASE);
		if (logEntriesForTestcases.size() > 0) {
			writeToTab(logEntriesForTestcases, workbook, "TEST CASE", locale);
		}

		Collection<LogEntry> logEntriesForTeststeps = importLog.findAllFor(EntityType.TEST_STEP);
		if (logEntriesForTeststeps.size() > 0) {
			writeToTab(logEntriesForTeststeps, workbook, "TEST STEP", locale);
		}

		Collection<LogEntry> logEntriesForParameters = importLog.findAllFor(EntityType.PARAMETER);
		if (logEntriesForParameters.size() > 0) {
			writeToTab(logEntriesForParameters, workbook, "PARAMETER", locale);
		}

		Collection<LogEntry> logEntriesForDatasets = importLog.findAllFor(EntityType.DATASET);
		if (logEntriesForDatasets.size() > 0) {
			writeToTab(logEntriesForDatasets, workbook, "DATASET", locale);
		}

		// Writing the workbook to the response output stream
		try {

			response.setContentType("application/octet-stream");

			response.setHeader("Content-Disposition",
					"attachment; filename=" + "import-log-" + IsoDateUtils.formatIso8601DateTime(new Date()) + ".csv");

			workbook.write(response.getOutputStream());

		} catch (IOException ex) {
			LOGGER.error(ex.getMessage());
			throw ex;
		}

	}
}

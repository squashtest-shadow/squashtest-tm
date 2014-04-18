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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.ImportLog;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

@Component
class TestCaseImportLogHelper {

	@Inject
	private InternationalizationHelper messageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseImportLogHelper.class);

	private void writeToTab(Collection<LogEntry> entries, XSSFWorkbook workbook, String sheetName, Locale locale) {
		if (entries.size() > 0) {
			// Create a blank sheet
			XSSFSheet sheet = workbook.createSheet(sheetName);

			writeHeaderToTab(sheet);
			writeEntriesToTab(entries, sheet, locale);
		}
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
			writeValueToCell(cell, messageSource.internationalize(entry.getI18nError(), locale));
			cell = row.createCell(cellnum++);
			writeValueToCell(cell, messageSource.internationalize(entry.getI18nImpact(), locale));
		}
	}

	public void writeToFile(ImportLog importLog, File emptyFile) throws IOException {
		XSSFWorkbook workbook = buildWorkbook(importLog);
		writeToFile(emptyFile, workbook);

	}

	private void writeToFile(File emptyFile, XSSFWorkbook workbook) throws IOException {
		BufferedOutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(emptyFile));
			workbook.write(os);
		} catch (IOException e) {
			LOGGER.warn(e.getMessage(), e);
			throw e;
		} finally {
			IOUtils.closeQuietly(os);
		}
	}

	private XSSFWorkbook buildWorkbook(ImportLog importLog) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		Locale locale = LocaleContextHolder.getLocale();

		Collection<LogEntry> logEntriesForTestcases = importLog.findAllFor(EntityType.TEST_CASE);
		writeToTab(logEntriesForTestcases, workbook, "TEST CASE", locale);

		Collection<LogEntry> logEntriesForTeststeps = importLog.findAllFor(EntityType.TEST_STEP);
		writeToTab(logEntriesForTeststeps, workbook, "TEST STEP", locale);

		Collection<LogEntry> logEntriesForParameters = importLog.findAllFor(EntityType.PARAMETER);
		writeToTab(logEntriesForParameters, workbook, "PARAMETER", locale);

		Collection<LogEntry> logEntriesForDatasets = importLog.findAllFor(EntityType.DATASET);
		writeToTab(logEntriesForDatasets, workbook, "DATASET", locale);
		return workbook;
	}

	/**
	 * Builds filename from iso timestamp
	 * 
	 * @param logTimeStamp
	 * @return
	 */
	public String logFilename(@NotNull String logTimeStamp) {
		return "test-case-import-log-" + logTimeStamp;
	}

	public void storeLogFile(WebRequest request, File xlsSummary, String logTimeStamp) {
		String logFilename = logFilename(logTimeStamp);
		request.setAttribute(logFilename, xlsSummary, RequestAttributes.SCOPE_SESSION);
	}

	public File fetchLogFile(WebRequest request, String logTimeStamp) {
		String logFilename = logFilename(logTimeStamp);
		return (File) request.getAttribute(logFilename, RequestAttributes.SCOPE_SESSION);
	}

}

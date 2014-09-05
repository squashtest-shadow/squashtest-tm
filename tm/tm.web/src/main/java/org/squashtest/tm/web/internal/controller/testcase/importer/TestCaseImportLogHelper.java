/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.controller.testcase.importer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.ImportLog;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

@Component
class TestCaseImportLogHelper {
	public static final String XLS_SUFFIX = ".xls";

	private static final String IMPORT_LOG_PREFIX = "test-case-import-log-";

	@Inject
	private InternationalizationHelper messageSource;

	private File tempDir;

	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseImportLogHelper.class);

	private void writeToTab(Collection<LogEntry> entries, Workbook workbook, String sheetName, Locale locale) {
		if (entries.size() > 0) {
			// Create a blank sheet
			Sheet sheet = workbook.createSheet(sheetName);

			writeHeaderToTab(sheet);
			writeEntriesToTab(entries, sheet, locale);
		}
	}

	private void writeHeaderToTab(Sheet sheet) {
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

	private void writeValueToCell(Cell cell, String value) {
		cell.setCellValue(value);
	}

	private void writeValueToCell(Cell cell, Number value) {
		cell.setCellValue(value.doubleValue());
	}

	@SuppressWarnings("deprecation")
	private void writeEntriesToTab(Collection<LogEntry> entries, Sheet sheet, Locale locale) {

		int rownum = 1;
		for (LogEntry entry : entries) {
			Row row = sheet.createRow(rownum++);
			int cellnum = 0;
			Cell cell = row.createCell(cellnum++);
			writeValueToCell(cell, entry.getLine());
			cell = row.createCell(cellnum++);
			writeValueToCell(cell, entry.getStatus().shortName());
			cell = row.createCell(cellnum++);
			//writeValueToCell(cell, "MISSING INFO");
			writeValueToCell(cell, "");
			cell = row.createCell(cellnum++);
			if (entry.getI18nError() != null){
				writeValueToCell(cell, messageSource.getMessage(entry.getI18nError(), entry.getErrorArgs(), locale));
			}
			cell = row.createCell(cellnum++);
			if (entry.getI18nImpact() != null){
				writeValueToCell(cell, messageSource.getMessage(entry.getI18nImpact(), entry.getImpactArgs(), locale));
			}
		}
	}

	private void writeToFile(ImportLog importLog, File emptyFile) throws IOException {
		Workbook workbook = buildWorkbook(importLog);
		writeToFile(emptyFile, workbook);

	}

	private void writeToFile(File emptyFile, Workbook workbook) throws IOException {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(emptyFile);
			workbook.write(os);
			os.flush();
		} catch (IOException e) {
			LOGGER.warn(e.getMessage(), e);
			throw e;
		} finally {
			IOUtils.closeQuietly(os);
		}
	}

	private Workbook buildWorkbook(ImportLog importLog) {
		Workbook workbook = new HSSFWorkbook();
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

	public File fetchLogFile(String filename) {
		return new File(getTempDir(), filename);

	}

	/**
	 * @return
	 */
	private File getTempDir() {
		if (tempDir == null) {
			try {
				tempDir = File.createTempFile("temp", null).getParentFile();
			} catch (IOException e) {
				LOGGER.error("Impossible to create a temp file ! Check fs permissions", e);
				throw new RuntimeException(e);
			}
		}
		return tempDir;
	}

	/**
	 * @param summary
	 * @return
	 * @throws IOException
	 */
	public File storeLogFile(ImportLog summary) throws IOException {
		File logFile = File.createTempFile(IMPORT_LOG_PREFIX, XLS_SUFFIX);
		writeToFile(summary, logFile);

		return logFile;
	}

}

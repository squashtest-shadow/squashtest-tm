package org.squashtest.tm.web.internal.controller.testcase.importer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.ImportLog;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

@Component
public abstract class ImportLogHelper {

	public static final String XLS_SUFFIX = ".xls";

	public static final List<String> headers = Arrays.asList("LINE", "TYPE", "ERROR", "IMPACT");

	@Inject
	private InternationalizationHelper messageSource;

	private File tempDir;

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

		for (String header : headers) {
			writeValueToCell(row.createCell(cellnum++), header);
		}
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
			if (entry.getI18nError() != null) {
				writeValueToCell(cell, messageSource.getMessage(entry.getI18nError(), entry.getErrorArgs(), locale));
			}
			cell = row.createCell(cellnum++);
			if (entry.getI18nImpact() != null) {
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
			getLogger().warn(e.getMessage(), e);
			throw e;
		} finally {
			IOUtils.closeQuietly(os);
		}
	}

	private Workbook buildWorkbook(ImportLog importLog) {
		Workbook workbook = new HSSFWorkbook();
		Locale locale = LocaleContextHolder.getLocale();

		Map<String, EntityType> entityTypeByTab = getEntityTypeByTab();

		for (String tabName : entityTypeByTab.keySet()) {
			Collection<LogEntry> logEntries = importLog.findAllFor(entityTypeByTab.get(tabName));
			writeToTab(logEntries, workbook, tabName, locale);
			getLogger().debug(getImportLogPrefix() + logEntries);
		}
		return workbook;
	}

	/**
	 * Builds filename from iso timestamp
	 *
	 * @param logTimeStamp
	 * @return
	 */
	public String logFilename(@NotNull String logTimeStamp) {
		return getImportLogPrefix() + logTimeStamp;
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
				getLogger().error("Impossible to create a temp file ! Check fs permissions", e);
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
		File logFile = File.createTempFile(getImportLogPrefix(), XLS_SUFFIX);
		writeToFile(summary, logFile);

		return logFile;
	}

	protected abstract String getImportLogPrefix();

	protected abstract Logger getLogger();

	protected abstract Map<String, EntityType> getEntityTypeByTab();

}

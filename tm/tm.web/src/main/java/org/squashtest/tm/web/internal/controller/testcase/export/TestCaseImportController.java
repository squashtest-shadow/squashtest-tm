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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.service.importer.ImportLog;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;

/**
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/test-cases/importer")
public class TestCaseImportController {

	private interface Command<T, U> {
		U execute(T arg);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseImportController.class);

	@Inject
	private TestCaseLibraryNavigationService navigationService;

	@Inject
	private TestCaseImportLogHelper logHelper;

	@RequestMapping(value = "/zip", method = RequestMethod.POST, produces = "text/html")
	public ModelAndView importZippedTestCases(@RequestParam("archive") MultipartFile archive,
			@RequestParam("projectId") long projectId, @RequestParam("zipEncoding") String zipEncoding)
					throws IOException {

		InputStream stream = archive.getInputStream();
		ModelAndView mav = new ModelAndView("fragment/import/import-summary");

		ImportSummary summary = navigationService.importZipTestCase(stream, projectId, zipEncoding);
		mav.addObject("summary", summary);
		mav.addObject("workspace", "test-case");

		return mav;
	}

	@RequestMapping(value = "/xls", method = RequestMethod.POST, params = "dry-run")
	public ModelAndView dryRunExcelWorkbook(@RequestParam("archive") MultipartFile archive, WebRequest request) {
		LOGGER.debug("dryRunExcelWorkbook");

		return importWorkbook(archive, request, new Command<File, ImportLog>() {
			@Override
			public ImportLog execute(File xls) {
				return navigationService.simulateImportExcelTestCase(xls);
			}
		});
	}

	private ModelAndView importWorkbook(MultipartFile archive, WebRequest request, Command<File, ImportLog> callback) {
		ModelAndView mav = new ModelAndView("fragment/import/import-summary");

		File xls = null;

		try {
			xls = multipartToImportFile(archive);
			ImportLog summary = callback.execute(xls); // TODO parser may throw ex we should handle
			summary.recompute(); // TODO why is it here ? shouldnt it be in service ?
			generateImportLog(request, summary);
			mav.addObject("summary", summary);
			mav.addObject("workspace", "test-case");

		} catch (IOException e) {
			LOGGER.error("An exception prevented processing of test-case import file", e);

		} finally {
			if (xls != null) {
				xls.deleteOnExit();
			}
		}

		return mav;
	}

	/**
	 * Generates a downloadable xls import log file and stores it where it should.
	 * 
	 * @param request
	 * @param summary
	 */
	private void generateImportLog(WebRequest request, ImportLog summary) {
		File xlsSummary = null;

		try {
			xlsSummary = importLogToLogFile(summary);

			String reportUrl = request.getContextPath() + "/test-cases/import-logs/" + xlsSummary.getName();
			summary.setReportUrl(reportUrl);

		} catch (IOException e) {
			LOGGER.warn("An error occured during import log generation", e);

		} finally {
			if (xlsSummary != null) {
				xlsSummary.deleteOnExit();
			}

		}
	}

	private File importLogToLogFile(ImportLog summary) throws IOException {
		return logHelper.storeLogFile(summary);
	}

	private File multipartToImportFile(MultipartFile archive) throws IOException, FileNotFoundException {
		InputStream is = archive.getInputStream();
		File xls = File.createTempFile("test-case-import-", ".xls");
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(xls));
		IOUtils.copy(is, os);
		IOUtils.closeQuietly(os);
		return xls;
	}

	@RequestMapping(value = "/xls", params = "!dry-run", method = RequestMethod.POST)
	public ModelAndView importExcelWorkbook(@RequestParam("archive") MultipartFile archive, WebRequest request) {

		return importWorkbook(archive, request, new Command<File, ImportLog>() {
			@Override
			public ImportLog execute(File xls) {
				return navigationService.performImportExcelTestCase(xls);
			}
		});

	}
}

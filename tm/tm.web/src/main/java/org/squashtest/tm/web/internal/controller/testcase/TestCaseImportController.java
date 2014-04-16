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

package org.squashtest.tm.web.internal.controller.testcase;

import java.io.BufferedOutputStream;
import java.io.File;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.service.importer.ImportLog;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.internal.importer.ImportSummaryImpl;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;

/**
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("test-cases/importer")
public class TestCaseImportController {
	private interface Command<T, U> {
		U execute(T arg);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseImportController.class);

	@Inject
	private TestCaseLibraryNavigationService navigationService;

	@RequestMapping(value="/zip", method = RequestMethod.POST, produces = "text/html")
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

	@RequestMapping(value="/xls", method = RequestMethod.POST, params = "dry-run")
	public ModelAndView dryRunExcelWorkbook(@RequestParam("archive") MultipartFile archive,
			@RequestParam("projectId") long projectId) throws IOException {
		LOGGER.debug("dryRunExcelWorkbook");

		return importWorkbook(archive, new Command<File, ImportLog>() {
			@Override
			public ImportLog execute(File xls) {
				return navigationService.importExcelTestCase(xls);
			}
		});
	}

	private ModelAndView importWorkbook(MultipartFile archive, Command<File, ImportLog> callback) throws IOException {
		ModelAndView mav = new ModelAndView("fragment/import/import-summary");

		//		ImportLog summary = null;
		//
		//		InputStream is = archive.getInputStream();
		//		File xls = File.createTempFile("dryrun-testcase-import", "xls");
		//		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(xls));
		//
		//		try {
		//			IOUtils.copy(is, os);
		//			summary = callback.execute(xls);
		//
		//		} catch (IOException e) {
		//			LOGGER.warn("Error duting test-case dry-run import", e);
		//			throw e; // TODO not sure what to do
		//
		//		} finally {
		//			IOUtils.closeQuietly(os);
		//
		//			if (xls != null) {
		//				xls.deleteOnExit();
		//			}
		//		}
		//
		//		mav.addObject("summary", summary);
		mav.addObject("summary", new ImportSummary() {

			@Override
			public int getTotal() {
				return 10;
			}

			@Override
			public int getSuccess() {
				return 20;
			}

			@Override
			public int getRenamed() {
				return 30;
			}

			@Override
			public int getModified() {
				return 40;
			}

			@Override
			public int getFailures() {
				return 50;
			}

			@Override
			public int getRejected() {
				return 60;
			}

			@Override
			public void add(ImportSummary summary) {
			}});
		mav.addObject("workspace", "test-case");

		return mav;
	}

	@RequestMapping(value="/xls", params = "!dry-run", method = RequestMethod.POST)
	public ModelAndView importExcelWorkbook(@RequestParam("archive") MultipartFile archive,
			@RequestParam("projectId") long projectId) throws IOException {

		return importWorkbook(archive, new Command<File, ImportLog>() {
			@Override
			public ImportLog execute(File xls) {
				return navigationService.importExcelTestCase(xls);
			}
		});

	}
}

/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.search;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.SearchExportCSVModel;
import org.squashtest.tm.domain.search.SearchExportCSVModel.Row;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;
import org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

@Controller
@RequestMapping("/advanced-search")
public class AdvancedSearchExportController {

	@Inject
	private TestCaseAdvancedSearchService testCaseAdvancedSearchService;
	
	@Inject
	private RequirementVersionAdvancedSearchService requirementVersionAdvancedSearchService;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AdvancedSearchExportController.class);

	@RequestMapping(method = RequestMethod.GET, params = { "searchModel",
			"export=csv", "requirement" })
	public @ResponseBody
	void exportRequirementVersionAdvancedSearchResult(
			HttpServletResponse response,
			@RequestParam(value = "searchModel") String searchModel, final Locale locale)
			throws IOException {

		AdvancedSearchModel parsedSearchModel = new ObjectMapper().readValue(
				searchModel, AdvancedSearchModel.class);

		SearchExportCSVModel model = requirementVersionAdvancedSearchService
				.exportRequirementVersionSearchResultsToCSV(parsedSearchModel, locale);

		// prepare the response
		response.setContentType("application/octet-stream");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		response.setHeader("Content-Disposition", "attachment; filename="
				+ "RequirementExport" + sdf.format(new Date()) + ".csv");

		// print
		PrintWriter writer = response.getWriter();
		Row header = model.getHeader();
		writer.write(header.toString() + "\n");

		Iterator<Row> iterator = model.dataIterator();
		while (iterator.hasNext()) {
			Row datarow = iterator.next();
			String cleanRowValue = HTMLCleanupUtils
					.htmlToText(datarow.toString()).replaceAll("\\n", " ")
					.replaceAll("\\r", " ");
			writer.write(cleanRowValue + "\n");
		}

	}

	@RequestMapping(method = RequestMethod.GET, params = { "searchModel",
			"export=csv", "testcase" })
	public @ResponseBody
	void exportTestCaseAdvancedSearchResult(HttpServletResponse response,
			@RequestParam(value = "searchModel") String searchModel, final Locale locale)
			throws IOException {

		BufferedWriter writer = null;

		try {
			AdvancedSearchModel parsedSearchModel = new ObjectMapper()
					.readValue(searchModel, AdvancedSearchModel.class);
			SearchExportCSVModel model = testCaseAdvancedSearchService
					.exportTestCaseSearchResultsToCSV(parsedSearchModel, locale);

			// prepare the response
			writer = new BufferedWriter(new OutputStreamWriter(
					response.getOutputStream()));

			response.setContentType("application/octet-stream");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

			response.setHeader("Content-Disposition", "attachment; filename="
					+ "TestCaseExport" + sdf.format(new Date()) + ".csv");

			// print
			Row header = model.getHeader();
			writer.write(header.toString() + "\n");

			Iterator<Row> iterator = model.dataIterator();
			while (iterator.hasNext()) {
				Row datarow = iterator.next();
				String cleanRowValue = HTMLCleanupUtils
						.htmlToText(datarow.toString()).replaceAll("\\n", " ")
						.replaceAll("\\r", " ");
				writer.write(cleanRowValue + "\n");
			}

			// closes stream in the finally clause
		} catch (IOException ex) {
			LOGGER.error(ex.getMessage());
			throw new RuntimeException(ex);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ex) {
					LOGGER.warn(ex.getMessage());
				}
			}
		}

	}
}

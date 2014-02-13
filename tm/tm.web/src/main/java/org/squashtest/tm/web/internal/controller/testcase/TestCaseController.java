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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.testcase.TestCaseFinder;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.model.json.JsonTestCase;
import org.squashtest.tm.web.internal.model.json.JsonTestCaseBuilder;

/**
 * @author Gregory Fouquet
 * 
 */
@RequestMapping("/test-cases")
@Controller
public class TestCaseController {
	/**
	 * ids post param
	 */
	private static final String IDS = RequestParams.IDS;

	/**
	 * folder ids post param
	 */
	private static final String FOLDER_IDS = RequestParams.FOLDER_IDS;

	@Inject
	private Provider<JsonTestCaseBuilder> builder;

	@Inject
	private TestCaseFinder finder;


	@Inject
	private Provider<TestCaseImportanceJeditableComboDataBuilder> importanceComboBuilderProvider;

	@Inject
	private Provider<TestCaseNatureJeditableComboDataBuilder> natureComboBuilderProvider;
	
	@Inject
	private Provider<TestCaseStatusJeditableComboDataBuilder> statusComboBuilderProvider;
	
	@Inject
	private Provider<TestCaseTypeJeditableComboDataBuilder> typeComboBuilderProvider;
	/**
	 * Fetches and returns a list of json test cases from their ids
	 * 
	 * @param testCaseIds
	 *            non null list of test cases ids.
	 * @return
	 * 
	 */
	@RequestMapping(method = RequestMethod.GET, params = IDS, headers = "Accept=application/json, text/javascript")
	public @ResponseBody
	List<JsonTestCase> getJsonTestCases(@RequestParam(IDS) List<Long> testCaseIds, Locale locale) {
		List<TestCase> testCases = finder.findAllByIds(testCaseIds);
		return builder.get().locale(locale).entities(testCases).toJson();
	}

	/**
	 * Fetches and returns a list of json test cases from their containers
	 * 
	 * @param foldersIds
	 *            non null list of folders ids.
	 * @return
	 * 
	 */
	@RequestMapping(method = RequestMethod.GET, params = FOLDER_IDS, headers = "Accept=application/json, text/javascript")
	public @ResponseBody
	List<JsonTestCase> getJsonTestCasesFromFolders(@RequestParam(FOLDER_IDS) List<Long> folderIds, Locale locale) {
		return buildJsonTestCasesFromAncestorIds(folderIds, locale);
	}

	private List<JsonTestCase> buildJsonTestCasesFromAncestorIds(List<Long> folderIds, Locale locale) {
		List<TestCase> testCases = finder.findAllByAncestorIds(folderIds);
		return builder.get().locale(locale).entities(testCases).toJson();
	}

	/**
	 * Fetches and returns a list of json test cases from their ids and containers
	 * 
	 * @param testCaseIds
	 * @param folderIds
	 * @param locale
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, params = { IDS, FOLDER_IDS }, headers = "Accept=application/json, text/javascript")
	public @ResponseBody
	List<JsonTestCase> getJsonTestCases(@RequestParam(IDS) List<Long> testCaseIds,
			@RequestParam(FOLDER_IDS) List<Long> folderIds, Locale locale) {
		List<Long> consolidatedIds = new ArrayList<Long>(testCaseIds.size() + folderIds.size());
		consolidatedIds.addAll(testCaseIds);
		consolidatedIds.addAll(folderIds);
		return buildJsonTestCasesFromAncestorIds(consolidatedIds, locale);
	}
	
	@RequestMapping(value = "/importance-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public String buildImportanceComboData(Locale locale) {
		return importanceComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}

	@RequestMapping(value = "/status-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public String buildStatusComboData(Locale locale) {
		return statusComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}
	
	@RequestMapping(value = "/type-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public String buildTypeComboData(Locale locale) {
		return typeComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}
	
	@RequestMapping(value = "/nature-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public String buildNatureComboData(Locale locale) {
		return natureComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}
}

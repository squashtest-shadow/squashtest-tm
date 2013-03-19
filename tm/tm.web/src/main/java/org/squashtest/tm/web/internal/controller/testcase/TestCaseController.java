/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
import org.squashtest.tm.web.internal.model.json.JsonTestCase;
import org.squashtest.tm.web.internal.model.json.JsonTestCaseBuilder;

/**
 * @author Gregory Fouquet
 * 
 */
@RequestMapping("/test-cases")
@Controller
public class TestCaseController {
	@Inject
	private Provider<JsonTestCaseBuilder> builder;

	@Inject
	private TestCaseFinder finder;

	/**
	 * Fetches and returns a list of json test cases from their ids
	 * 
	 * @param testCaseIds
	 *            non null list of test cases ids.
	 * @return
	 * 
	 */
	@RequestMapping(method = RequestMethod.GET, params = "ids[]", headers = "Accept=application/json, text/javascript")
	public @ResponseBody
	List<JsonTestCase> getJsonTestCases(@RequestParam("ids[]") List<Long> testCaseIds, Locale locale) {
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
	@RequestMapping(method = RequestMethod.GET, params = "folderIds[]", headers = "Accept=application/json, text/javascript")
	public @ResponseBody
	List<JsonTestCase> getJsonTestCasesFromFolders(@RequestParam("folderIds[]") List<Long> folderIds, Locale locale) {
		List<TestCase> testCases = finder.findAllByAncestorIds(folderIds);
		return builder.get().locale(locale).entities(testCases).toJson();
	}

	@RequestMapping(method = RequestMethod.GET, params = { "ids[]", "folderIds[]" }, headers = "Accept=application/json, text/javascript")
	public @ResponseBody
	List<JsonTestCase> getJsonTestCases(@RequestParam("ids[]") List<Long> testCaseIds,
			@RequestParam("folderIds[]") List<Long> folderIds, Locale locale) {
		List<Long> consolidatedIds = new ArrayList<Long>(testCaseIds.size() + folderIds.size());
		consolidatedIds.addAll(testCaseIds);
		consolidatedIds.addAll(folderIds);
		List<TestCase> testCases = finder.findAllByAncestorIds(consolidatedIds);
		return builder.get().locale(locale).entities(testCases).toJson();
	}
}

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
package org.squashtest.tm.web.internal.controller.bugtracker;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.testcase.TestCaseRestInfo;
import org.squashtest.tm.service.bugtracker.JiraPluginRestService;

@Controller
@RequestMapping("/jira/rest")
public class JiraPluginRestController {

	@Inject
	JiraPluginRestService jiraPluginRestService;

	@RequestMapping("/getTestCases")
	public @ResponseBody List<TestCaseRestInfo> getTestCases(
			@RequestParam(value="issueKey", required=true) String issueKey,
			@RequestParam(value="bugTrackerKey", required=true) String bugTrackerKey) {
		return jiraPluginRestService.getTestcases("2849", "LOL");
		//return jiraPluginRestService.getTestcases(issueKey, bugTrackerKey);
	}
}

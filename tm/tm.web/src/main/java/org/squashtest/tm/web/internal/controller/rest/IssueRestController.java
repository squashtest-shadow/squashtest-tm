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
package org.squashtest.tm.web.internal.controller.rest;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.web.internal.model.rest.RestExecution;
import org.squashtest.tm.web.internal.model.rest.RestIssue;

@Controller
@RequestMapping("/rest/api/issue")
public class IssueRestController {

	@Inject
	BugTrackersLocalService bugTrackersLocalService;
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public RestIssue getIssueById(@PathVariable Long id) {
		Issue issue = bugTrackersLocalService.findIssueById(id);
		return new RestIssue(issue);
	}

	@RequestMapping(value = "/{id}/execution", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public RestExecution getExecutionByIssueId(@PathVariable Long id) {
		Execution execution = bugTrackersLocalService.findExecutionByIssueId(id);
		RestExecution restExecution = new RestExecution(execution);
		return restExecution;
	}
	
}

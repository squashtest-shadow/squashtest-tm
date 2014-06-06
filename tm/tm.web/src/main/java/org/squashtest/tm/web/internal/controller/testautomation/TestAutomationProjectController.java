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
package org.squashtest.tm.web.internal.controller.testautomation;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.testautomation.TestAutomationProjectManagerService;

@Controller
@RequestMapping("/test-automation-projects")
public class TestAutomationProjectController {

	@Inject
	private TestAutomationProjectManagerService service;

	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationServerController.class);

	private static final String PROJECT_ID = "/{projectId}";

	@RequestMapping(value = PROJECT_ID, method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteTestAutomationProject(@PathVariable long projectId) {
		LOGGER.info("Delete test automation project of id #{}", projectId);
		service.deleteProject(projectId);
	}

	@RequestMapping(value = PROJECT_ID, method = RequestMethod.POST)
	@ResponseBody
	public void editTestAutomationProject(@PathVariable long projectId, @RequestBody TestAutomationProject project) {
		LOGGER.info("Edit test automation project of id #{}", projectId);
		service.changeJobName(projectId, project.getJobName());
		service.changeLabel(projectId, project.getLabel());
		service.changeSlaves(projectId, project.getSlaves());
	}

}

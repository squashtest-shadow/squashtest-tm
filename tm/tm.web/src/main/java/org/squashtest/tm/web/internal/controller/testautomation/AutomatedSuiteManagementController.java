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
package org.squashtest.tm.web.internal.controller.testautomation;

import java.util.Locale;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.csp.tm.service.TestAutomationFinderService;
import org.squashtest.tm.web.internal.controller.execution.AutomatedExecutionViewUtils;
import org.squashtest.tm.web.internal.controller.execution.AutomatedExecutionViewUtils.AutomatedSuiteOverview;

@Controller
@RequestMapping("/automated-suites/{suiteId}")
public class AutomatedSuiteManagementController {

	@Inject
	private MessageSource messageSource;
	
	private TestAutomationFinderService testAutomationManagementService;
	
	@ServiceReference
	public void setTestAutomationManagementService(TestAutomationFinderService testAutomationManagementService) {
		this.testAutomationManagementService = testAutomationManagementService;
	}
	
	@RequestMapping(value = "/executions", method = RequestMethod.GET)
	public @ResponseBody AutomatedSuiteOverview updateExecutionInfo(@PathVariable String suiteId, Locale locale) {
		AutomatedSuite suite = testAutomationManagementService.findAutomatedTestSuiteById(suiteId);
		return AutomatedExecutionViewUtils.buildExecInfo(suite, locale, messageSource);
	}
	
}

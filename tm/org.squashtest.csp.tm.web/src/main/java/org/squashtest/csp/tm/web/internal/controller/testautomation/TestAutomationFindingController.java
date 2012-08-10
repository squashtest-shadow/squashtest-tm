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
package org.squashtest.csp.tm.web.internal.controller.testautomation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.service.TestAutomationFinderService;


@Controller
@RequestMapping("/test-automation")
public class TestAutomationFindingController {

	
	private TestAutomationFinderService testAutomationManagementService;

	
	@ServiceReference
	public void setTestAutomationManagementService(
			TestAutomationFinderService testAutomationManagementService) {
		this.testAutomationManagementService = testAutomationManagementService;
	}



	@RequestMapping(value = "/servers/projects-list", method = RequestMethod.GET, headers = "Accept=application/json", params = {"url", "login", "password"} )
	@ResponseBody
	public Collection<TestAutomationProject> listProjectsOnServer(@RequestParam("url") String strURL, 
																  @RequestParam("login") String login, 
																  @RequestParam("password") String password) throws MalformedURLException{
		
		return testAutomationManagementService.listProjectsOnServer(new URL(strURL), login, password);
		
	}
}

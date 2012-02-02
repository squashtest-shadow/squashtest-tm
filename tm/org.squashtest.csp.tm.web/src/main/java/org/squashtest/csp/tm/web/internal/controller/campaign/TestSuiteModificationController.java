/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.web.internal.controller.campaign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.tm.service.TestSuiteModificationService;


@Controller
@RequestMapping("/test-suites/{suiteId}")
public class TestSuiteModificationController {

	private TestSuiteModificationService service;
	
	@ServiceReference
	public void setTestSuiteModificationService(TestSuiteModificationService service){
		this.service=service;
	}
	
	@RequestMapping(value="/rename", method=RequestMethod.POST, params="newName" )
	public @ResponseBody Map<String, String> renameTestSuite(@PathVariable("suiteId") Long suiteId, @RequestParam("newName") String newName ){
		service.rename(suiteId, newName);
		Map<String, String> result = new HashMap<String, String>();
		result.put("id", suiteId.toString());
		result.put("name", newName);
		return result;
	}
	
	@RequestMapping(value="/test-cases", method=RequestMethod.POST, params="ids[]")
	public @ResponseBody Map<String, String> bindTestPlan(@PathVariable("suiteId") long suiteId, @RequestParam("ids[]") List<Long> itpIds){
		service.bindTestPlan(suiteId, itpIds);
		Map<String, String> result = new HashMap<String, String>();
		result.put("id", Long.toString(suiteId));
		return result;
	}
	
	
}

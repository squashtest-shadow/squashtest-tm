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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.tm.web.internal.helper.JsonHelper;

@Controller
@RequestMapping("/automated-suites/{suiteId}")
public class AutomatedSuiteManagementController {

	//MOCK
	@RequestMapping(value = "/executions", method = RequestMethod.GET)
	public @ResponseBody String updateExecutionInfo(@PathVariable String suiteId, Locale locale) {
		List<Map<String, Object>> executionInfo = new ArrayList<Map<String, Object>>();
		//TODO GET REAL INFO
		for(int i=0; i<2; i++)
		{
			Map<String, Object> infos = new HashMap<String, Object>(4);
			infos.put("id", "1");		
			infos.put("name", "Test1");
			if(suiteId == "fails") {
				infos.put("status", "Failure");
				infos.put("localizedStatus", "Failure");
			}
			else {
				infos.put("status", "Success");
				infos.put("localizedStatus", "Success");				
			}
			executionInfo.add(infos);
		}

		return JsonHelper.serialize(executionInfo);
	}
}

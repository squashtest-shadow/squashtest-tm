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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.testcase.TestCaseFinder;
import org.squashtest.tm.service.testcase.TestStepFinder;
import org.squashtest.tm.web.internal.model.rest.RestTestCase;
import org.squashtest.tm.web.internal.model.rest.RestTestStep;

@Controller
@RequestMapping("/rest/api/testcase")
public class TestCaseRestController {
	
	@Inject
	TestCaseFinder testCaseFinder;

	@Inject
	TestStepFinder testStepFinder;
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public RestTestCase getTestCaseById(@PathVariable Long id) {
		TestCase testCase = this.testCaseFinder.findById(id);
		return new RestTestCase(testCase);
	}

	@RequestMapping(value = "/{id}/teststeps", method = RequestMethod.GET)
	public List<RestTestStep> getTestStepsByTestCaseId(@PathVariable Long id) {
		List<TestStep> testSteps = this.testCaseFinder.findStepsByTestCaseId(id);
		List<RestTestStep> restTestSteps = new ArrayList<RestTestStep>(testSteps.size());
		for(TestStep testStep : testSteps){
			restTestSteps.add(new RestTestStep(testStep));
		}
		return restTestSteps;
	}
	
}

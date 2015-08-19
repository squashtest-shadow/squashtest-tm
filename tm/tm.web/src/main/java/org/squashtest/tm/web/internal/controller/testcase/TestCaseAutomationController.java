/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.web.internal.model.testautomation.TATestNode;
import org.squashtest.tm.web.internal.model.testautomation.TATestNodeListBuilder;


@Controller
@RequestMapping("/test-cases/{testCaseId}/test-automation")
public class TestCaseAutomationController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseAutomationController.class);
	private TestCaseModificationService testCaseModificationService;
	@ServiceReference
	public void setTestCaseModificationService(TestCaseModificationService testCaseModificationService) {
		this.testCaseModificationService = testCaseModificationService;
	}
	private static final String NAME_KEY = "name";
	private static final String PATH	=	"path";
	private static final String TEST_CASE_ID = "testCaseId";
	private static final String PROJECT_ID = "projectId";


	@RequestMapping(value="/tests", method = RequestMethod.GET)
	@ResponseBody
	public Collection<TATestNode> findAssignableAutomatedTests(@PathVariable(TEST_CASE_ID) Long testCaseId){
		LOGGER.trace("Find assignable automated tests for TC#"+testCaseId);

		Collection<TestAutomationProjectContent> projectContents = testCaseModificationService.findAssignableAutomationTests(testCaseId);
		return new TATestNodeListBuilder().build(projectContents);


		/*
		 * STUB (obsolete

		if (testCaseId == 238){
			Collection<TestAutomationProjectContent> projectContents = mockTests();

			return new TATestNodeListBuilder().build(projectContents);
		}
		else{
			throw new UnknownConnectorKind("pof");
		}
		 */
	}


	@RequestMapping(value="/tests", method = RequestMethod.POST, params = { PROJECT_ID, NAME_KEY})
	@ResponseBody
	public void bindAutomatedTest(@PathVariable(TEST_CASE_ID) long testCaseId,@RequestParam(PROJECT_ID) long projectId, @RequestParam(NAME_KEY) String testName){
		LOGGER.trace("Bind automated test "+testName+" to TC#"+testCaseId);
		testCaseModificationService.bindAutomatedTest(testCaseId, projectId, testName);

	}

	@RequestMapping(value="/tests", method = RequestMethod.POST, params = { PATH })
	@ResponseBody
	public String bindAutomatedTest(@PathVariable(TEST_CASE_ID) long testCaseId, @RequestParam(PATH) String testPath){
		LOGGER.trace("Bind automated test "+testPath+" to TC#"+testCaseId);
		testCaseModificationService.bindAutomatedTest(testCaseId, testPath);
		return testPath;
	}



	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	public void removeAutomation(@PathVariable(TEST_CASE_ID) long testCaseId){

		testCaseModificationService.removeAutomation(testCaseId);

	}


	/*
	@Deprecated
	private Collection<TestAutomationProjectContent> mockTests() throws RuntimeException{

		Field pid = ReflectionUtils.findField(TestAutomationProject.class, "id");
		pid.setAccessible(true);

		TestAutomationProject p1 = new TestAutomationProject("jobbook", "localjob1", null);

		// TODO : change access from private to public
		ReflectionUtils.setField(pid, p1, 1l);

		AutomatedTest t11 = new AutomatedTest("database/stresstest", p1);
		AutomatedTest t12 = new AutomatedTest("database/index", p1);
		AutomatedTest t13 = new AutomatedTest("UI/login", p1);
		AutomatedTest t14 = new AutomatedTest("UI/admin/adduser", p1);
		AutomatedTest t15 = new AutomatedTest("basic", p1);

		TestAutomationProjectContent content1 = new TestAutomationProjectContent(p1, Arrays.asList(t11, t12, t13, t14, t15));

		TestAutomationProject p2 = new TestAutomationProject("grandproject", "Grand Project", null);
		ReflectionUtils.setField(pid, p2, 2l);

		AutomatedTest t21 = new AutomatedTest("step1/be grand", p2);
		AutomatedTest t22 = new AutomatedTest("step1/be smart", p2);
		AutomatedTest t23 = new AutomatedTest("step2/be bold", p2);
		AutomatedTest t24 = new AutomatedTest("step2/optional/have a side kick", p2);
		AutomatedTest t25 = new AutomatedTest("just be", p2);

		TestAutomationProjectContent content2 = new TestAutomationProjectContent(p2, Arrays.asList(t21, t22, t23, t24, t25));

		return Arrays.asList(content1, content2);

	}
	 */
}

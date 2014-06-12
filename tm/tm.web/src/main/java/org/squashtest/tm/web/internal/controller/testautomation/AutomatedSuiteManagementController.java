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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService;
import org.squashtest.tm.service.testautomation.model.SuiteExecutionConfiguration;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.web.internal.controller.execution.AutomatedExecutionViewUtils;
import org.squashtest.tm.web.internal.controller.execution.AutomatedExecutionViewUtils.AutomatedSuiteOverview;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

@Controller
@RequestMapping("/automated-suites")
public class AutomatedSuiteManagementController {

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	private AutomatedSuiteManagerService service;

	@RequestMapping(value = "/new", method = RequestMethod.POST, params="iteration-id", produces="application/json")
	@ResponseBody
	public AutomatedSuiteDetails createNewAutomatedSuiteForIteration(@RequestParam("iteration-id") Long iterationId){
		AutomatedSuite suite = service.createFromIterationTestPlan(iterationId);
		return toProjectContentModel(suite.getId());
	}
	@RequestMapping(value = "/new", method = RequestMethod.POST, params="test-suite-id", produces="application/json")
	@ResponseBody
	public AutomatedSuiteDetails createNewAutomatedSuiteForTestSuite(@RequestParam("test-suite-id") Long testSuiteId){
		AutomatedSuite suite = service.createFromTestSuiteTestPlan(testSuiteId);
		return toProjectContentModel(suite.getId());
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST, params="test-plan-item-ids", produces="application/json")
	@ResponseBody
	public AutomatedSuiteDetails createNewAutomatedSuite(@RequestParam("test-plan-item-ids") List<Long> testPlanIds){
		AutomatedSuite suite = service.createFromItemIds(testPlanIds);
		return toProjectContentModel(suite.getId());
	}

	@RequestMapping(value = "/{suiteId}/executor", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public AutomatedSuiteOverview runAutomatedSuite(@PathVariable("suiteId") String suiteId,
			@RequestBody Collection<SuiteExecutionConfiguration> configuration,
			Locale locale){
		service.start(suiteId, configuration);
		return updateExecutionInfo(suiteId, locale);
	}

	@RequestMapping(value = "/{suiteId}/executions", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public  AutomatedSuiteOverview updateExecutionInfo(@PathVariable String suiteId, Locale locale) {
		AutomatedSuite suite = service.findById(suiteId);
		return AutomatedExecutionViewUtils.buildExecInfo(suite, locale, messageSource);
	}


	@RequestMapping(value = "/{suiteId}/details", method = RequestMethod.GET, produces="application/json")
	public AutomatedSuiteDetails toProjectContentModel(@PathVariable("suiteId") String suiteId){
		AutomatedSuite suite = service.findById(suiteId);
		Collection<TestAutomationProjectContent> projectContents = service.sortByProject(suite);

		Collection<TestAutomationProjectContentModel> models =
				new ArrayList<TestAutomationProjectContentModel>(projectContents.size());

		for (TestAutomationProjectContent content : projectContents){
			models.add(new TestAutomationProjectContentModel(content));
		}

		return new AutomatedSuiteDetails(suiteId, models);
	}



	private static final class AutomatedSuiteDetails{

		private String suiteId;
		private Collection<TestAutomationProjectContentModel> projectContents;

		public AutomatedSuiteDetails(String suiteId, Collection<TestAutomationProjectContentModel> projectContents) {
			super();
			this.suiteId = suiteId;
			this.projectContents = projectContents;
		}

		public String getSuiteId() {
			return suiteId;
		}

		public Collection<TestAutomationProjectContentModel> getProjectContents() {
			return projectContents;
		}



	}

}

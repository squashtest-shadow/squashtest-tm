/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.campaign;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.campaign.IterationTestPlanFinder;
import org.squashtest.tm.service.campaign.TestSuiteModificationService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.controller.execution.AutomatedExecutionViewUtils;
import org.squashtest.tm.web.internal.controller.execution.AutomatedExecutionViewUtils.AutomatedSuiteOverview;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;

@Controller
@RequestMapping("/test-suites/{suiteId}")
public class TestSuiteModificationController {
	

	private static final String TEST_SUITE = "testSuite";
	private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteModificationController.class);

	private static final String NAME = "name";

	@Inject
	private TestSuiteModificationService service;

	@Inject
	private IterationTestPlanFinder iterationTestPlanFinder;

	@Inject
	private CustomFieldValueFinderService cufValueService;
	
	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentsHelper;

	@Inject
	private InternationalizationHelper messageSource;

	


	// will return the fragment only
	@RequestMapping(method = RequestMethod.GET)
	public String showTestSuite(Model model, @PathVariable("suiteId") long suiteId) {

		populateTestSuiteModel(model, suiteId);
		return "fragment/test-suites/edit-test-suite";
	}

	// will return the iteration in a full page
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String showTestSuiteInfo(Model model, @PathVariable("suiteId") long suiteId) {

		populateTestSuiteModel(model, suiteId);
		return "page/campaign-libraries/show-test-suite";
	}
	
	private void populateTestSuiteModel(Model model, long testSuiteId){
		
		TestSuite testSuite = service.findById(testSuiteId);
		TestPlanStatistics testSuiteStats = service.findTestSuiteStatistics(testSuiteId);
		boolean hasCUF = cufValueService.hasCustomFields(testSuite);
		DataTableModel attachmentsModel = attachmentsHelper.findPagedAttachments(testSuite);
		Map<String, String> assignableUsers = getAssignableUsers(testSuiteId);
		
		model.addAttribute(TEST_SUITE, testSuite);
		model.addAttribute("statistics", testSuiteStats);
		model.addAttribute("hasCUF", hasCUF);
		model.addAttribute("attachmentsModel", attachmentsModel);
		model.addAttribute("assignableUsers", assignableUsers);
		
	}

	
	
	private Map<String, String> getAssignableUsers(long testSuiteId){

		Locale locale = LocaleContextHolder.getLocale();
		TestSuite ts = service.findById(testSuiteId);
		
		List<User> usersList = iterationTestPlanFinder.findAssignableUserForTestPlan(ts.getIteration().getId());
		Collections.sort(usersList, new UserLoginComparator());

		String unassignedLabel = messageSource.internationalize("label.Unassigned", locale);

		Map<String, String> jsonUsers = new LinkedHashMap<String, String>(usersList.size());
		
		jsonUsers.put(User.NO_USER_ID.toString(), unassignedLabel);
		for (User user : usersList){
			jsonUsers.put(user.getId().toString(), user.getLogin());
		}
		
		return jsonUsers;
	}


	@RequestMapping(value = "/general", method = RequestMethod.GET)
	public ModelAndView refreshGeneralInfos(@PathVariable("suiteId") long suiteId) {

		TestSuite testSuite = service.findById(suiteId);

		ModelAndView mav = new ModelAndView("fragment/generics/general-information-fragment");

		mav.addObject("auditableEntity", testSuite);
		mav.addObject("entityContextUrl", "/test-suites/" + suiteId);

		return mav;
	}

	@RequestMapping(value = "/statistics", method = RequestMethod.GET)
	public ModelAndView refreshStats(@PathVariable("suiteId") long suiteId) {

		TestPlanStatistics testSuiteStats = service.findTestSuiteStatistics(suiteId);

		ModelAndView mav = new ModelAndView("fragment/generics/statistics-fragment");
		mav.addObject("statisticsEntity", testSuiteStats);

		return mav;
	}

	@RequestMapping(value = "/exec-button", method = RequestMethod.GET)
	public ModelAndView refreshExecButton(@PathVariable("suiteId") long suiteId) {

		TestPlanStatistics testSuiteStats = service.findTestSuiteStatistics(suiteId);

		ModelAndView mav = new ModelAndView("fragment/generics/test-suite-execution-button");

		mav.addObject("testSuiteId", suiteId);
		mav.addObject("statisticsEntity", testSuiteStats);

		return mav;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=test-suite-description", VALUE })
	@ResponseBody
	public String updateDescription(@RequestParam(VALUE) String newDescription, @PathVariable("suiteId") long suiteId) {

		service.changeDescription(suiteId, newDescription);
		LOGGER.trace("Test-suite " + suiteId + ": updated description to " + newDescription);
		return newDescription;

	}

	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	@ResponseBody
	public Object rename(HttpServletResponse response, @RequestParam("newName") String newName, @PathVariable("suiteId") long suiteId) {

		LOGGER.info("TestSuiteModificationController : renaming " + suiteId + " as " + newName);
		service.rename(suiteId, newName);
		return new RenameModel(newName);

	}

	// that method is redundant but don't remove it yet.
	@RequestMapping(value = "/rename", method = RequestMethod.POST, params = NAME)
	public @ResponseBody
	Map<String, String> renameTestSuite(@PathVariable("suiteId") Long suiteId, @RequestParam(NAME) String name) {
		service.rename(suiteId, name);
		Map<String, String> result = new HashMap<String, String>();
		result.put("id", suiteId.toString());
		result.put(NAME, name);
		return result;
	}


	
	// ****************** execution of the whole suite ****************************************
	
	@RequestMapping(method = RequestMethod.POST, params = { "id=execute-auto", "testPlanItemsIds[]" })
	public @ResponseBody
	AutomatedSuiteOverview executeSelectionAuto(@PathVariable("suiteId") long suiteId,
			@RequestParam("testPlanItemsIds[]") List<Long> ids, Locale locale) {
		

		AutomatedSuite autoSuite = service.createAndStartAutomatedSuite(suiteId, ids);

		LOGGER.debug("Test-Suite #" + suiteId + " : execute selected test plans");

		return AutomatedExecutionViewUtils.buildExecInfo(autoSuite, locale, messageSource);

	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=execute-auto", "!testPlanItemsIds[]" })
	public @ResponseBody
	AutomatedSuiteOverview executeAllAuto(@PathVariable("suiteId") long suiteId, Locale locale) {
		AutomatedSuite suite = service.createAndStartAutomatedSuite(suiteId);

		LOGGER.debug("Test-Suite #" + suiteId + " : execute all test plan auto");

		return AutomatedExecutionViewUtils.buildExecInfo(suite, locale, messageSource);

	}
	
	
	// ******************** other stuffs ********************
	
	
	private static final class UserLoginComparator implements Comparator<User>{
		@Override
		public int compare(User u1, User u2) {
			return u1.getLogin().compareTo(u2.getLogin());
		}
		
	}
	
	
}

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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.core.foundation.lang.IsoDateUtils;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.campaign.IterationTestPlanFinder;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.statistics.iteration.IterationStatisticsBundle;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.execution.AutomatedExecutionViewUtils;
import org.squashtest.tm.web.internal.controller.execution.AutomatedExecutionViewUtils.AutomatedSuiteOverview;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseImportanceJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.jquery.TestSuiteModel;

@Controller
@RequestMapping("/iterations/{iterationId}")
public class IterationModificationController {

	private static final String NAME = "name";

	private static final Logger LOGGER = LoggerFactory.getLogger(IterationModificationController.class);

	private static final String ITERATION_KEY = "iteration";
	private static final String ITERATION_ID_KEY = "iterationId";
	private static final String PLANNING_URL = "/planning";

	@Inject
	private IterationModificationService iterationModService;
	
	@Inject
	private IterationTestPlanManagerService iterationTestPlanManagerService;

	@Inject
	private PermissionEvaluationService permissionService;

	@Inject
	private CustomFieldValueFinderService cufValueService;

	@Inject
	private IterationTestPlanFinder testPlanFinder;	
	
	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentHelper;

	@Inject
	private Provider<TestCaseImportanceJeditableComboDataBuilder> importanceComboBuilderProvider;
	
	@Inject
	private InternationalizationHelper messageSource;


														

	@RequestMapping(method = RequestMethod.GET)
	public String showIteration(Model model, @PathVariable long iterationId) {
		
		populateIterationModel(model, iterationId);
		return "fragment/iterations/edit-iteration";
	}

	// will return the iteration in a full page
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String showIterationInfo(Model model, @PathVariable long iterationId) {

		populateIterationModel(model, iterationId);
		return "page/campaign-libraries/show-iteration";
	}
	
	private void populateIterationModel(Model model, long iterationId){
		
		Iteration iteration = iterationModService.findById(iterationId);
		TestPlanStatistics statistics = iterationModService.getIterationStatistics(iterationId);
		boolean hasCUF = cufValueService.hasCustomFields(iteration);
		DataTableModel attachmentsModel = attachmentHelper.findPagedAttachments(iteration);
		Map<String, String> assignableUsers = getAssignableUsers(iterationId);
		Map<String, String> weights = getWeights();
		
		model.addAttribute(ITERATION_KEY, iteration);
		model.addAttribute("statistics", statistics);
		model.addAttribute("hasCUF", hasCUF);		
		model.addAttribute("attachmentsModel", attachmentsModel);		
		model.addAttribute("assignableUsers", assignableUsers);
		model.addAttribute("weights", weights);
	}

	private Map<String, String> getWeights(){
		Locale locale = LocaleContextHolder.getLocale();
		return importanceComboBuilderProvider.get().useLocale(locale).buildMap();
	}
	
	private Map<String, String> getAssignableUsers(@PathVariable long iterationId){

		Locale locale = LocaleContextHolder.getLocale();
		
		List<User> usersList = iterationTestPlanManagerService.findAssignableUserForTestPlan(iterationId);
		Collections.sort(usersList, new UserLoginComparator());

		String unassignedLabel = messageSource.internationalize("label.Unassigned", locale);

		Map<String, String> jsonUsers = new LinkedHashMap<String, String>(usersList.size());
		
		jsonUsers.put(User.NO_USER_ID.toString(), unassignedLabel);
		for (User user : usersList){
			jsonUsers.put(user.getId().toString(), user.getLogin());
		}
		
		return jsonUsers;
	}

	//URL should have been /statistics, but that was already used by another method in this controller
	@RequestMapping (value = "/dashboard-statistics", method = RequestMethod.GET, produces="application/json"/*, params = {"date"}*/)
	public @ResponseBody IterationStatisticsBundle getStatisticsAsJson(@PathVariable("iterationId") long iterationId/*, @RequestParam(value="date", defaultValue="") String strDate*/){
			
		return iterationModService.gatherIterationStatisticsBundle(iterationId);
	}
	
	@RequestMapping (value = "/dashboard", method = RequestMethod.GET, produces="text/html"/*, params = {"date"}*/)
	public ModelAndView getDashboard(Model model, @PathVariable("iterationId") long iterationId/*, @RequestParam(value="date", defaultValue="") String strDate*/){
			
		Iteration iteration = iterationModService.findById(iterationId);
		IterationStatisticsBundle bundle = iterationModService.gatherIterationStatisticsBundle(iterationId);
		
		ModelAndView mav  = new ModelAndView("fragment/iterations/iteration-dashboard");
		mav.addObject("iteration", iteration);
		mav.addObject("dashboardModel", bundle);
		
		return mav;
	}
	
	@RequestMapping(value = "/statistics", method = RequestMethod.GET)
	public ModelAndView refreshStats(@PathVariable long iterationId) {

		TestPlanStatistics iterationStatistics = iterationModService.getIterationStatistics(iterationId);

		ModelAndView mav = new ModelAndView("fragment/generics/statistics-fragment");
		mav.addObject("statisticsEntity", iterationStatistics);

		return mav;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=iteration-description", VALUE })
	@ResponseBody
	public String updateDescription(@RequestParam(VALUE) String newDescription, @PathVariable long iterationId) {

		iterationModService.changeDescription(iterationId, newDescription);
		LOGGER.trace("Iteration " + iterationId + ": updated description to " + newDescription);
		return newDescription;

	}

	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	@ResponseBody
	public Object rename(HttpServletResponse response, @RequestParam("newName") String newName,
			@PathVariable long iterationId) {

		LOGGER.info("IterationModificationController : renaming " + iterationId + " as " + newName);
		iterationModService.rename(iterationId, newName);
		return new RenameModel(newName);

	}

	@RequestMapping(value = "/duplicateTestSuite/{testSuiteId}", method = RequestMethod.POST)
	public @ResponseBody
	Long duplicateTestSuite(@PathVariable(ITERATION_ID_KEY) Long iterationId,
			@PathVariable("testSuiteId") Long testSuiteId) {
		TestSuite duplicate = iterationModService.copyPasteTestSuiteToIteration(testSuiteId, iterationId);
		return duplicate.getId();
	}

	@RequestMapping(value = "/general", method = RequestMethod.GET)
	public ModelAndView refreshGeneralInfos(@PathVariable long iterationId) {

		Iteration iteration = iterationModService.findById(iterationId);

		ModelAndView mav = new ModelAndView("fragment/generics/general-information-fragment");

		mav.addObject("auditableEntity", iteration);
		mav.addObject("entityContextUrl", "/iterations/" + iterationId);

		return mav;
	}

	/* *************************************** planning ********************************* */

	/**
	 * returns null if the string is empty, or a date otherwise. No check regarding the actual content of strDate.
	 */
	private Date strToDate(String strDate) {
		return IsoDateUtils.millisecondsToDate(strDate);
	}

	private String dateToStr(Date date) {
		return IsoDateUtils.dateToMillisecondsAsString(date);
	}

	@RequestMapping(value = PLANNING_URL, params = { "scheduledStart" })
	public @ResponseBody
	String setScheduledStart(HttpServletResponse response, @PathVariable long iterationId,
			@RequestParam(value = "scheduledStart") String strDate) {

		Date newScheduledStart = strToDate(strDate);
		String toReturn = dateToStr(newScheduledStart);

		LOGGER.info("IterationModificationController : setting scheduled start date for iteration " + iterationId
				+ ", new date : " + newScheduledStart);

		iterationModService.changeScheduledStartDate(iterationId, newScheduledStart);

		return toReturn;

	}

	@RequestMapping(value = PLANNING_URL, params = { "scheduledEnd" })
	@ResponseBody
	public String setScheduledEnd(HttpServletResponse response, @PathVariable long iterationId,
			@RequestParam(value = "scheduledEnd") String strDate) {

		Date newScheduledEnd = strToDate(strDate);
		String toReturn = dateToStr(newScheduledEnd);

		LOGGER.info("IterationModificationController : setting scheduled end date for iteration " + iterationId
				+ ", new date : " + newScheduledEnd);

		iterationModService.changeScheduledEndDate(iterationId, newScheduledEnd);

		return toReturn;

	}

	/** the next functions may receive null arguments : empty string **/

	@RequestMapping(value = PLANNING_URL, params = { "actualStart" })
	@ResponseBody
	public String setActualStart(HttpServletResponse response, @PathVariable long iterationId,
			@RequestParam(value = "actualStart") String strDate) {

		Date newActualStart = strToDate(strDate);
		String toReturn = dateToStr(newActualStart);

		LOGGER.info("IterationModificationController : setting actual start date for iteration " + iterationId
				+ ", new date : " + newActualStart);

		iterationModService.changeActualStartDate(iterationId, newActualStart);

		return toReturn;

	}

	@RequestMapping(value = PLANNING_URL, params = { "actualEnd" })
	@ResponseBody
	public String setActualEnd(HttpServletResponse response, @PathVariable long iterationId,
			@RequestParam(value = "actualEnd") String strDate) {

		Date newActualEnd = strToDate(strDate);
		String toReturn = dateToStr(newActualEnd);

		LOGGER.info("IterationModificationController : setting actual end date for iteration " + iterationId
				+ ", new date : " + newActualEnd);

		iterationModService.changeActualEndDate(iterationId, newActualEnd);

		return toReturn;

	}

	@RequestMapping(value = PLANNING_URL, params = { "setActualStartAuto" })
	@ResponseBody
	public String setActualStartAuto(HttpServletResponse response, @PathVariable long iterationId,
			@RequestParam(value = "setActualStartAuto") boolean auto) {

		LOGGER.info("IterationModificationController : autosetting actual start date for iteration " + iterationId
				+ ", new value " + auto);

		iterationModService.changeActualStartAuto(iterationId, auto);
		Iteration iteration = iterationModService.findById(iterationId);

		String toreturn = dateToStr(iteration.getActualStartDate());

		return toreturn;
	}

	@RequestMapping(value = PLANNING_URL, params = { "setActualEndAuto" })
	@ResponseBody
	public String setActualEndAuto(HttpServletResponse response, @PathVariable long iterationId,
			@RequestParam(value = "setActualEndAuto") boolean auto) {
		LOGGER.info("IterationModificationController : autosetting actual end date for campaign " + iterationId
				+ ", new value " + auto);

		iterationModService.changeActualEndAuto(iterationId, auto);
		Iteration iteration = iterationModService.findById(iterationId);

		String toreturn = dateToStr(iteration.getActualEndDate());

		return toreturn;

	}

	/* *************************************** test plan ********************************* */


	// returns the ID of the newly created execution
	@RequestMapping(value = "/test-plan/{testPlanItemId}/executions/new", method = RequestMethod.POST, params = { "mode=manual" })
	public @ResponseBody
	String addManualExecution(@PathVariable long testPlanItemId, @PathVariable long iterationId) {
		iterationModService.addExecution(testPlanItemId);
		List<Execution> executionList = iterationModService.findExecutionsByTestPlan(iterationId, testPlanItemId);

		return executionList.get(executionList.size() - 1).getId().toString();

	}

	@RequestMapping(value = "/test-plan/{testPlanId}/executions/new", method = RequestMethod.POST, params = { "mode=auto" })
	public @ResponseBody
	AutomatedSuiteOverview addAutoExecution(@PathVariable("testPlanId") long testPlanId,
			@PathVariable(ITERATION_ID_KEY) long iterationId, Locale locale) {
		Collection<Long> testPlanIds = new ArrayList<Long>(1);
		testPlanIds.add(testPlanId);

		AutomatedSuite suite = iterationModService.createAndStartAutomatedSuite(iterationId, testPlanIds);

		return AutomatedExecutionViewUtils.buildExecInfo(suite, locale, messageSource);

	}

	@RequestMapping(value = "/test-plan/{itemId}/executions", method = RequestMethod.GET)
	public ModelAndView getExecutionsForTestPlan(@PathVariable("iterationId") long iterationId,
			@PathVariable("itemId") long itemId) {

		// TODO
		List<Execution> executionList = iterationModService.findExecutionsByTestPlan(iterationId,
				itemId);
		// get the iteraction to check access rights
		Iteration iter = iterationModService.findById(iterationId);
		boolean editable = permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", iter);
		IterationTestPlanItem testPlanItem = testPlanFinder.findTestPlanItem(itemId);
		ModelAndView mav = new ModelAndView("fragment/iterations/iteration-test-plan-row");

		mav.addObject("editableIteration", editable);
		mav.addObject("testPlanItem", testPlanItem);
		mav.addObject(ITERATION_ID_KEY, iterationId);
		mav.addObject(ITERATION_KEY, iter);
		mav.addObject("executions", executionList);

		return mav;

	}


	/* ********************** test suites **************************** */

	@RequestMapping(value = "/test-suites/new", params = NAME, method = RequestMethod.POST)
	public @ResponseBody
	Map<String, String> addTestSuite(@PathVariable long iterationId,
			@Valid @ModelAttribute("new-test-suite") TestSuite suite) {
		iterationModService.addTestSuite(iterationId, suite);
		Map<String, String> res = new HashMap<String, String>();
		res.put("id", suite.getId().toString());
		res.put(NAME, suite.getName());
		return res;
	}

	@RequestMapping(value = "/test-suites", method = RequestMethod.GET)
	public @ResponseBody
	List<TestSuiteModel> getTestSuites(@PathVariable long iterationId) {
		Collection<TestSuite> testSuites = iterationModService.findAllTestSuites(iterationId);
		List<TestSuiteModel> result = new ArrayList<TestSuiteModel>();
		for (TestSuite testSuite : testSuites) {
			TestSuiteModel model = new TestSuiteModel(testSuite.getId(), testSuite.getName());
			result.add(model);
		}
		return result;
	}

	@RequestMapping(value = "/test-suites/delete", method = RequestMethod.POST, params = { RequestParams.IDS })
	public @ResponseBody
	OperationReport removeTestSuites(@RequestParam(RequestParams.IDS) List<Long> ids) {
		OperationReport report = iterationModService.removeTestSuites(ids);
		LOGGER.debug("removal of " + report.getRemoved().size() + " Test Suites");
		return report;
	}

	/* ************** execute auto *********************************** */

	@RequestMapping(method = RequestMethod.POST, params = { "id=execute-auto", "testPlanItemsIds[]" })
	public @ResponseBody
	AutomatedSuiteOverview executeSelectionAuto(@PathVariable long iterationId,
			@RequestParam("testPlanItemsIds[]") List<Long> ids, Locale locale) {
		AutomatedSuite suite = iterationModService.createAndStartAutomatedSuite(iterationId, ids);
		

		LOGGER.debug("Iteration #" + iterationId + " : execute selected test plans");

		return AutomatedExecutionViewUtils.buildExecInfo(suite, locale, messageSource);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=execute-auto", "!testPlanItemsIds[]" })
	public @ResponseBody
	AutomatedSuiteOverview executeAllAuto(@PathVariable long iterationId, Locale locale) {
		AutomatedSuite suite = iterationModService.createAndStartAutomatedSuite(iterationId);

		LOGGER.debug("Iteration #" + iterationId + " : execute all test plan auto");

		return AutomatedExecutionViewUtils.buildExecInfo(suite, locale, messageSource);
	}

	
	// ******************** other stuffs ***********************
	
	private static final class UserLoginComparator implements Comparator<User>{
		@Override
		public int compare(User u1, User u2) {
			return u1.getLogin().compareTo(u2.getLogin());
		}
		
	}

}

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
package org.squashtest.csp.tm.web.internal.controller.campaign;

import static org.squashtest.csp.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.csp.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.IterationModificationService;
import org.squashtest.csp.tm.service.IterationTestPlanFinder;
import org.squashtest.csp.tm.web.internal.controller.execution.AutomatedExecutionViewUtils;
import org.squashtest.csp.tm.web.internal.controller.execution.AutomatedExecutionViewUtils.AutomatedSuiteOverview;
import org.squashtest.csp.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableFilterSorter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.csp.tm.web.internal.model.jquery.TestSuiteModel;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;
import org.squashtest.csp.tm.web.internal.utils.DateUtils;

@Controller
@RequestMapping("/iterations/{iterationId}")
public class IterationModificationController {

	private static final String NAME = "name";

	private static final Logger LOGGER = LoggerFactory.getLogger(IterationModificationController.class);

	private IterationModificationService iterationModService;

	@Inject
	private PermissionEvaluationService permissionService;

	private IterationTestPlanFinder testPlanFinder;

	@ServiceReference
	public void setIterationModificationService(IterationModificationService iterationModificationService) {
		this.iterationModService = iterationModificationService;
	}

	@ServiceReference
	public void setIterationTestPlanFinder(IterationTestPlanFinder iterationTestPlanFinder) {
		this.testPlanFinder = iterationTestPlanFinder;
	}

	@Inject
	private InternationalizationHelper messageSource;

	private final DataTableMapper testPlanMapper = new DataTableMapper("unused", IterationTestPlanItem.class,
			TestCase.class, Project.class, TestSuite.class).initMapping(12)
			.mapAttribute(Project.class, 3, NAME, String.class).mapAttribute(TestCase.class, 4, NAME, String.class)
			.mapAttribute(TestCase.class, 5, "importance", TestCaseImportance.class)
			.mapAttribute(TestCase.class, 6, "executionMode", TestCaseExecutionMode.class)
			.mapAttribute(IterationTestPlanItem.class, 7, "executionStatus", ExecutionStatus.class)
			.mapAttribute(TestSuite.class, 8, NAME, String.class)
			.mapAttribute(IterationTestPlanItem.class, 9, "lastExecutedBy", String.class)
			.mapAttribute(IterationTestPlanItem.class, 11, "lastExecutedOn", Date.class);

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showIteration(@PathVariable long iterationId) {

		Iteration iteration = iterationModService.findById(iterationId);

		ModelAndView mav = new ModelAndView("fragment/iterations/edit-iteration");
		mav.addObject("iteration", iteration);

		return mav;
	}

	// will return the iteration in a full page
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView showIterationInfo(@PathVariable long iterationId) {

		Iteration iteration = iterationModService.findById(iterationId);

		ModelAndView mav = new ModelAndView("page/campaign-libraries/show-iteration");

		if (iteration != null) {
			mav.addObject("iteration", iteration);
		} else {
			iteration = new Iteration();
			iteration.setName("Not found");
			iteration.setDescription("This iteration either do not exists, or was removed");
			mav.addObject("iteration", new Iteration());

		}
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
	Long duplicateTestSuite(@PathVariable("iterationId") Long iterationId, @PathVariable("testSuiteId") Long testSuiteId) {
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
		return DateUtils.millisecondsToDate(strDate);
	}

	private String dateToStr(Date date) {
		return DateUtils.dateToMillisecondsAsString(date);
	}

	@RequestMapping(value = "/planning", params = { "scheduledStart" })
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

	@RequestMapping(value = "/planning", params = { "scheduledEnd" })
	@ResponseBody
	public String setScheduledEnd(HttpServletResponse response, @PathVariable long iterationId,
			@RequestParam(value = "scheduledEnd") String strDate) {

		Date newScheduledEnd = strToDate(strDate);
		String toReturn = dateToStr(newScheduledEnd);

		LOGGER.info("IterationModificationController : setting scheduled start date for iteration " + iterationId
				+ ", new date : " + newScheduledEnd);

		iterationModService.changeScheduledEndDate(iterationId, newScheduledEnd);

		return toReturn;

	}

	/** the next functions may receive null arguments : empty string **/

	@RequestMapping(value = "/planning", params = { "actualStart" })
	@ResponseBody
	public String setActualStart(HttpServletResponse response, @PathVariable long iterationId,
			@RequestParam(value = "actualStart") String strDate) {

		Date newActualStart = strToDate(strDate);
		String toReturn = dateToStr(newActualStart);

		LOGGER.info("IterationModificationController : setting scheduled start date for iteration " + iterationId
				+ ", new date : " + newActualStart);

		iterationModService.changeActualStartDate(iterationId, newActualStart);

		return toReturn;

	}

	@RequestMapping(value = "/planning", params = { "actualEnd" })
	@ResponseBody
	public String setActualEnd(HttpServletResponse response, @PathVariable long iterationId,
			@RequestParam(value = "actualEnd") String strDate) {

		Date newActualEnd = strToDate(strDate);
		String toReturn = dateToStr(newActualEnd);

		LOGGER.info("IterationModificationController : setting scheduled start date for iteration " + iterationId
				+ ", new date : " + newActualEnd);

		iterationModService.changeActualEndDate(iterationId, newActualEnd);

		return toReturn;

	}

	@RequestMapping(value = "/planning", params = { "setActualStartAuto" })
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

	@RequestMapping(value = "/planning", params = { "setActualEndAuto" })
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

	/***
	 * Method called when you drag a test case and change its position in the selected iteration
	 * 
	 * @param testPlanId
	 *            : the iteration owning the moving test plan items
	 * 
	 * @param itemIds
	 *            the ids of the items we are trying to move
	 * 
	 * @param newIndex
	 *            the new position of the first of them
	 */
	@RequestMapping(value = "/test-case/move", method = RequestMethod.POST, params = { "newIndex", "itemIds[]" })
	@ResponseBody
	public void moveTestPlanItems(@PathVariable("iterationId") long iterationId, @RequestParam int newIndex,
			@RequestParam("itemIds[]") List<Long> itemIds) {
		iterationModService.changeTestPlanPosition(iterationId, newIndex, itemIds);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("iteration " + iterationId + ": moving " + itemIds.size() + " test plan items  to " + newIndex);
		}
	}

	// returns the ID of the newly created execution
	@RequestMapping(value = "/test-plan/{testPlanId}/executions/new", method = RequestMethod.POST, params = { "mode=manual" })
	public @ResponseBody
	String addManualExecution(@PathVariable("testPlanId") long testPlanId, @PathVariable("iterationId") long iterationId) {
		iterationModService.addExecution(iterationId, testPlanId);
		List<Execution> executionList = iterationModService.findExecutionsByTestPlan(iterationId, testPlanId);

		return executionList.get(executionList.size() - 1).getId().toString();

	}

	@RequestMapping(value = "/test-plan/{testPlanId}/executions/new", method = RequestMethod.POST, params = { "mode=auto" })
	public @ResponseBody
	String addAutoExecution(@PathVariable("testPlanId") long testPlanId, @PathVariable("iterationId") long iterationId) {
		Collection<Long> testPlanIds = new ArrayList<Long>(1);
		testPlanIds.add(testPlanId);

		iterationModService.createAndExecuteAutomatedSuite(iterationId, testPlanIds);

		return "ok";

	}

	@RequestMapping(value = "/test-case-executions/{testPlanId}", method = RequestMethod.GET)
	public ModelAndView getExecutionsForTestPlan(@PathVariable long iterationId, @PathVariable long testPlanId) {

		List<Execution> executionList = iterationModService.findExecutionsByTestPlan(iterationId, testPlanId);
		// get the iteraction to check access rights
		Iteration iter = iterationModService.findById(iterationId);
		boolean editable = permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", iter);
		IterationTestPlanItem testPlanItem = testPlanFinder.findTestPlanItem(iterationId, testPlanId);
		ModelAndView mav = new ModelAndView("fragment/iterations/iteration-test-plan-row");

		mav.addObject("editableIteration", editable);
		mav.addObject("testPlanItem", testPlanItem);
		mav.addObject("iterationId", iterationId);
		mav.addObject("iteration", iter);
		mav.addObject("executions", executionList);

		return mav;

	}

	@RequestMapping(value = "/test-plan", params = "sEcho")
	public @ResponseBody
	DataTableModel getTestPlanModel(@PathVariable Long iterationId, final DataTableDrawParameters params,
			final Locale locale) {

		CollectionSorting filter = createCollectionSorting(params, testPlanMapper);

		FilteredCollectionHolder<List<IterationTestPlanItem>> holder = testPlanFinder.findTestPlan(iterationId, filter);

		return new DataTableModelHelper<IterationTestPlanItem>() {
			@Override
			public Map<String, Object> buildItemData(IterationTestPlanItem item) {

				Map<String, Object> res = new HashMap<String, Object>();

				String projectName;
				String testCaseName;
				String importance;
				String testCaseExecutionMode;
				String automationMode = item.isAutomated() ? "A" : "M";

				String testSuiteName;
				Long assignedId = (item.getUser() != null) ? item.getUser().getId() : User.NO_USER_ID;

				if (item.isTestCaseDeleted()) {
					projectName = formatNoData(locale);
					testCaseName = formatDeleted(locale);
					testCaseExecutionMode = formatNoData(locale);
					importance = formatNoData(locale);
				} else {
					projectName = item.getReferencedTestCase().getProject().getName();
					testCaseName = item.getReferencedTestCase().getName();
					importance = messageSource.internationalize(item.getReferencedTestCase().getImportance(), locale);
					testCaseExecutionMode = messageSource.internationalize(item.getReferencedTestCase()
							.getExecutionMode(), locale);
				}

				if (item.getTestSuite() == null) {
					testSuiteName = formatNone(locale);
				} else {
					testSuiteName = item.getTestSuite().getName();
				}

				res.put(DataTableModelHelper.DEFAULT_ENTITY_ID_KEY, item.getId());
				res.put(DataTableModelHelper.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
				res.put("project-name", projectName);
				res.put("tc-name", testCaseName);
				res.put("importance", importance);
				res.put("type", testCaseExecutionMode);
				res.put("suite", testSuiteName);
				res.put("status", messageSource.internationalize(item.getExecutionStatus(), locale));
				res.put("last-exec-by", formatString(item.getLastExecutedBy(), locale));
				res.put("assigned-to", assignedId);
				res.put("last-exec-on", messageSource.localizeDate(item.getLastExecutedOn(), locale));
				res.put("is-tc-deleted", item.isTestCaseDeleted());
				res.put("empty-delete-holder", " ");
				res.put("exec-mode", automationMode);

				return res;
			}
		}.buildDataModel(holder, filter.getFirstItemIndex() + 1, params.getsEcho());

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

	@RequestMapping(value = "/test-suites/delete", method = RequestMethod.POST, params = { "ids[]" })
	public @ResponseBody
	List<Long> removeTestSuites(@RequestParam("ids[]") List<Long> ids) {
		List<Long> deletedIds = iterationModService.removeTestSuites(ids);
		LOGGER.debug("removal of " + deletedIds.size() + " Test Suites");
		return deletedIds;
	}

	/* ************** execute auto *********************************** */

	@RequestMapping(method = RequestMethod.POST, params = { "id=execute-auto", "testPlanItemsIds[]" })
	public @ResponseBody 
	AutomatedSuiteOverview  executeSelectionAuto(@PathVariable long iterationId, @RequestParam("testPlanItemsIds[]") List<Long> ids , Locale locale){
	

			AutomatedSuite suite = iterationModService.createAndExecuteAutomatedSuite(ids); 
		LOGGER.debug("Iteration #" + iterationId + " : execute selected test plans");
			return 	AutomatedExecutionViewUtils.buildExecInfo(suite, locale, messageSource);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=execute-auto", "testPlanItemsIds" })
	public @ResponseBody AutomatedSuiteOverview executeAllAuto(@PathVariable long iterationId, Locale locale ){


		iterationModService.createAndExecuteAutomatedSuite(iterationId);
		LOGGER.debug("Iteration #" + iterationId + " : execute all test plan auto");
		return 	AutomatedExecutionViewUtils.buildExecInfo(suite, locale, messageSource);
	}

	/* ************** /execute auto *********************************** */

	/* ************** private stuffs are below ********************** */

	private CollectionSorting createCollectionSorting(final DataTableDrawParameters params, DataTableMapper mapper) {
		return new DataTableFilterSorter(params, mapper);
	}

	/* ***************** data formatter *************************** */

	private String formatString(String arg, Locale locale) {
		return arg == null ? formatNoData(locale) : arg;
	}

	private String formatNoData(Locale locale) {
		return messageSource.internationalize("squashtm.nodata", locale);
	}

	private String formatDeleted(Locale locale) {
		return messageSource.internationalize("squashtm.itemdeleted", locale);
	}

	private String formatNone(Locale locale) {
		return messageSource.internationalize("squashtm.none.f", locale);
	}

}

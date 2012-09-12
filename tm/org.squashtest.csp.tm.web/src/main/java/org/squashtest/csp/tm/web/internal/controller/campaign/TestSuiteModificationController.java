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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
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
import org.squashtest.csp.tm.domain.campaign.TestSuiteStatistics;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;
import org.squashtest.csp.tm.service.IterationModificationService;
import org.squashtest.csp.tm.service.IterationTestPlanFinder;
import org.squashtest.csp.tm.service.TestSuiteModificationService;
import org.squashtest.csp.tm.web.internal.controller.execution.AutomatedExecutionViewUtils;
import org.squashtest.csp.tm.web.internal.controller.execution.AutomatedExecutionViewUtils.AutomatedSuiteOverview;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;

@Controller
@RequestMapping("/test-suites/{id}")
public class TestSuiteModificationController {

	private static final String NAME = "name";

	private TestSuiteModificationService service;
	
	private IterationModificationService iterationModService;
	
	private IterationTestPlanFinder iterationTestPlanFinder;

	@Inject
	private PermissionEvaluationService permissionService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteModificationController.class);
	
	@ServiceReference
	public void setIterationTestPlanFinder (IterationTestPlanFinder iterationTestPlanFinder){
		this.iterationTestPlanFinder = iterationTestPlanFinder;
	}
	
	@ServiceReference
	public void setTestSuiteModificationService(TestSuiteModificationService service){
		this.service=service;
	}
	
	@ServiceReference
	public void setIterationModificationService(IterationModificationService iterationModService){
		this.iterationModService=iterationModService;
	}

	@Inject
	private MessageSource messageSource;
	
	private final DataTableMapper testPlanMapper = new DataTableMapper("unused", IterationTestPlanItem.class,
			TestCase.class, Project.class, TestSuite.class).initMapping(11)
			.mapAttribute(Project.class, 3, NAME, String.class)
			.mapAttribute(TestCase.class, 4, NAME, String.class)
			.mapAttribute(TestCase.class, 5, "importance", TestCaseImportance.class)			
			.mapAttribute(TestCase.class, 6, "executionMode", TestCaseExecutionMode.class)
			.mapAttribute(IterationTestPlanItem.class, 7, "executionStatus", ExecutionStatus.class)
			.mapAttribute(IterationTestPlanItem.class, 8, "lastExecutedBy", String.class)
			.mapAttribute(IterationTestPlanItem.class, 9, "lastExecutedOn", Date.class);
	
	// will return the fragment only
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showTestSuite(@PathVariable long id) {
		TestSuite testSuite = service.findById(id);
		TestSuiteStatistics testSuiteStats = service.findTestSuiteStatistics(id);
		
		ModelAndView mav = new ModelAndView("fragment/test-suites/edit-test-suite");
		mav.addObject("testSuite", testSuite);
		mav.addObject("statistics", testSuiteStats);
		return mav;
	}

	// will return the iteration in a full page
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView showTestSuiteInfo(@PathVariable long id) {

		TestSuite testSuite = service.findById(id);

		TestSuiteStatistics testSuiteStats = service.findTestSuiteStatistics(id);
		
		ModelAndView mav = new ModelAndView("page/campaign-libraries/show-test-suite");

		if (testSuite != null) {
			mav.addObject("testSuite", testSuite);
			mav.addObject("statistics", testSuiteStats);
		} else {
			testSuite = new TestSuite();
			testSuite.setName("Not found");
			testSuite.setDescription("This test suite either do not exists, or was removed");
			mav.addObject("testSuite", testSuite);

		}
		return mav;
	}
	
	@RequestMapping(value = "/general", method = RequestMethod.GET)
	public ModelAndView refreshGeneralInfos(@PathVariable long id) {

		TestSuite testSuite = service.findById(id);

		ModelAndView mav = new ModelAndView("fragment/generics/general-information-fragment");

		mav.addObject("auditableEntity", testSuite);
		mav.addObject("entityContextUrl", "/test-suites/" + id);

		return mav;
	}
	
	@RequestMapping(value = "/stats", method = RequestMethod.GET)
	public ModelAndView refreshStats(@PathVariable long id) {
		
		TestSuiteStatistics testSuiteStats = service.findTestSuiteStatistics(id);

		ModelAndView mav = new ModelAndView("fragment/generics/test-suite-statistics-fragment");

		mav.addObject("statisticsEntity", testSuiteStats);

		return mav;
	}
	
	@RequestMapping(value = "/exec-button", method = RequestMethod.GET)
	public ModelAndView refreshExecButton(@PathVariable long id) {
		
		TestSuiteStatistics testSuiteStats = service.findTestSuiteStatistics(id);

		ModelAndView mav = new ModelAndView("fragment/generics/test-suite-execution-button");

		mav.addObject("testSuiteId", id);
		mav.addObject("statisticsEntity", testSuiteStats);

		return mav;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=test-suite-description", VALUE })
	@ResponseBody
	public String updateDescription(@RequestParam(VALUE) String newDescription, @PathVariable long id) {

		service.changeDescription(id, newDescription);
		LOGGER.trace("Test-suite " + id + ": updated description to " + newDescription);
		return newDescription;

	}
	

	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	@ResponseBody
	public Object rename(HttpServletResponse response, @RequestParam("newName") String newName,
			@PathVariable long id) {

		LOGGER.info("TestSuiteModificationController : renaming " + id + " as " + newName);
		service.rename(id, newName);
		return new RenameModel(newName);

	}

	//that method is useful too so don't remove it !
	@RequestMapping(value="/rename", method=RequestMethod.POST, params=NAME )
	public @ResponseBody Map<String, String> renameTestSuite(@PathVariable("id") Long id, @RequestParam(NAME) String name ){
		service.rename(id, name);
		Map<String, String> result = new HashMap<String, String>();
		result.put("id", id.toString());
		result.put(NAME, name);
		return result;
	}
	
	@RequestMapping(value="/test-cases", method=RequestMethod.POST, params="test-cases[]")
	public @ResponseBody Map<String, String> bindTestPlan(@PathVariable("id") long suiteId, @RequestParam("test-cases[]") List<Long> itpIds){
		service.bindTestPlan(suiteId, itpIds);
		Map<String, String> result = new HashMap<String, String>();
		result.put("id", Long.toString(suiteId));
		return result;
	}
	
	/***
	 * Method called when you drag a test case and change its position in the selected iteration
	 * 
	 * @param testPlanId : 
	 * 				the iteration owning the moving test plan items
	 * 
	 * @param itemIds
	 *            the ids of the items we are trying to move
	 * 
	 * @param newIndex
	 *            the new position of the first of them
	 */
	@RequestMapping(value = "/test-case/move", method = RequestMethod.POST, params = { "newIndex", "itemIds[]" })
	@ResponseBody
	public void changeTestPlanIndex(@PathVariable("id") long testSuiteId, @RequestParam int newIndex, @RequestParam("itemIds[]") List<Long> itemIds){
		service.changeTestPlanPosition(testSuiteId, newIndex, itemIds);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("test-suite " + testSuiteId+ ": moving "+itemIds.size()+" test plan items  to " + newIndex);
		}
	}
	
	@RequestMapping(value = "{iterationId}/test-case-executions/{testPlanId}", method = RequestMethod.GET)
	public ModelAndView getExecutionsForTestPlan(@PathVariable long id, @PathVariable long iterationId, @PathVariable long testPlanId) {
		TestSuite testSuite = service.findById(id);
		List<Execution> executionList = iterationModService.findExecutionsByTestPlan(iterationId, testPlanId);
		// get the iteraction to check access rights
		Iteration iter = iterationModService.findById(iterationId);
		IterationTestPlanItem  iterationTestPlanItem = iterationTestPlanFinder.findTestPlanItem(iterationId, testPlanId);
		boolean editable = permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", iter);

		ModelAndView mav = new ModelAndView("fragment/test-suites/test-suite-test-plan-row");

		mav.addObject("editableIteration", editable);
		mav.addObject("testPlanItem", iterationTestPlanItem);
		mav.addObject("iterationId", iterationId);
		mav.addObject("executions", executionList);
		mav.addObject("testSuite", testSuite);
		
		return mav;

	}
	
	@RequestMapping(value = "/test-plan/table", params = "sEcho")
	public @ResponseBody
	DataTableModel getTestPlanModel(@PathVariable long id, final DataTableDrawParameters params,
			final Locale locale) {

		Paging paging = new DataTableMapperPagingAndSortingAdapter(params, testPlanMapper);
		
		PagedCollectionHolder<List<IterationTestPlanItem>> holder = service.findTestSuiteTestPlan(
				id, paging);
		
		return new DataTableModelHelper<IterationTestPlanItem>() {
			@Override
			public Object[] buildItemData(IterationTestPlanItem item) {

				String projectName;
				String testCaseName;
				String testCaseExecutionMode;
				String importance;
				String automationMode = item.isAutomated() ? "A" : "M";

				if (item.isTestCaseDeleted()) {
					projectName = formatNoData(locale);
					testCaseName = formatDeleted(locale);
					importance = formatNoData(locale);
					testCaseExecutionMode = formatNoData(locale);
				} else {
					projectName = item.getReferencedTestCase().getProject().getName();
					testCaseName = item.getReferencedTestCase().getName();
					importance = formatImportance(item.getReferencedTestCase().getImportance(), locale);
					testCaseExecutionMode = formatExecutionMode(item.getReferencedTestCase().getExecutionMode(), locale);
				}

				return new Object[] { item.getId(), 
						getCurrentIndex(),
						automationMode,
						projectName, 
						testCaseName,
						importance,
						testCaseExecutionMode, 
						formatStatus(item.getExecutionStatus(), locale),
						formatString(item.getLastExecutedBy(), locale), 
						formatDate(item.getLastExecutedOn(), locale),
						item.isTestCaseDeleted(), 
						" "

				};
			}
		}.buildDataModel(holder, params.getsEcho());

	}
	
	/* ************** execute auto *********************************** */
	
	@RequestMapping(method = RequestMethod.POST, params= {"id=execute-auto", "testPlanItemsIds[]"} )
	public @ResponseBody AutomatedSuiteOverview  executeSelectionAuto(@PathVariable long id, @RequestParam("testPlanItemsIds[]") List<Long> ids , Locale locale){
		//TODO we were in a hurry when i did this but must remove iterationModService from here 
		// must use iteration finder and create needed write methods in testSutiteModificationService.
		AutomatedSuite suite = iterationModService.createAndExecuteAutomatedSuite(ids); 
		LOGGER.debug("Test-Suite #"+id+" : execute selected test plans");
			return 	AutomatedExecutionViewUtils.buildExecInfo(suite, locale, messageSource);
		
	}
	
	
	@RequestMapping(method = RequestMethod.POST, params= {"id=execute-auto", "testPlanItemsIds"} )
	public @ResponseBody AutomatedSuiteOverview executeAllAuto(@PathVariable long id, Locale locale ){
		AutomatedSuite suite = service.createAndExecuteAutomatedSuite(id);
		LOGGER.debug("Test-Suite #"+id+" : execute all test plan auto");
		return 	AutomatedExecutionViewUtils.buildExecInfo(suite, locale, messageSource);
		
	}
	
/* ************** /execute auto *********************************** */

/* ***************** data formatter *************************** */

	private String formatString(String arg, Locale locale) {
		if (arg == null) {
			return formatNoData(locale);
		} else {
			return arg;
		}
	}

	private String formatDate(Date date, Locale locale) {
		try {
			String format = messageSource.getMessage("squashtm.dateformat", null, locale);
			return new SimpleDateFormat(format).format(date);
		} catch (Exception anyException) {
			return formatNoData(locale);
		}

	}

	private String formatNoData(Locale locale) {
		return messageSource.getMessage("squashtm.nodata", null, locale);
	}

	private String formatDeleted(Locale locale) {
		return messageSource.getMessage("squashtm.itemdeleted", null, locale);
	}

	private String formatExecutionMode(TestCaseExecutionMode mode, Locale locale) {
		return messageSource.getMessage(mode.getI18nKey(), null, locale);
	}

	private String formatStatus(ExecutionStatus status, Locale locale) {
		return messageSource.getMessage(status.getI18nKey(), null, locale);
	}
	
	private String formatImportance(TestCaseImportance importance, Locale locale) {
		return messageSource.getMessage(importance.getI18nKey(), null, locale);
	}
	
}

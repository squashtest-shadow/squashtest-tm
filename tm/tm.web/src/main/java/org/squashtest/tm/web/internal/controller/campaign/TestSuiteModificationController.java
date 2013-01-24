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
package org.squashtest.tm.web.internal.controller.campaign;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.campaign.IterationTestPlanFinder;
import org.squashtest.tm.service.campaign.TestSuiteModificationService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testautomation.TestAutomationFinderService;
import org.squashtest.tm.web.internal.controller.execution.AutomatedExecutionViewUtils;
import org.squashtest.tm.web.internal.controller.execution.AutomatedExecutionViewUtils.AutomatedSuiteOverview;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.viewmapper.DataTableMapper;

@Controller
@RequestMapping("/test-suites/{id}")
public class TestSuiteModificationController {

	private static final String NAME = "name";

	private TestSuiteModificationService service;

	// TODO : move to TestSuiteModificationService everything handled here bu the other services. In order to remove
	// those
	// extra services.

	private IterationModificationService iterationModService;

	private IterationTestPlanFinder iterationTestPlanFinder;

	@Inject
	private PermissionEvaluationService permissionService;

	private TestAutomationFinderService testAutomationService;
	
	@Inject
	private CustomFieldValueFinderService cufValueService;
	

	private static final String TEST_SUITE = "testSuite";
	private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteModificationController.class);

	@ServiceReference
	public void setTestAutomationFinderService(TestAutomationFinderService testAutomationService) {
		this.testAutomationService = testAutomationService;
	}

	@ServiceReference
	public void setIterationTestPlanFinder(IterationTestPlanFinder iterationTestPlanFinder) {
		this.iterationTestPlanFinder = iterationTestPlanFinder;
	}

	@ServiceReference
	public void setTestSuiteModificationService(TestSuiteModificationService service) {
		this.service = service;
	}

	@ServiceReference
	public void setIterationModificationService(IterationModificationService iterationModService) {
		this.iterationModService = iterationModService;
	}
	
	@Inject
	private InternationalizationHelper messageSource;

	private final DataTableMapper testPlanMapper = new DataTableMapper("unused", IterationTestPlanItem.class,
			TestCase.class, Project.class, TestSuite.class).initMapping(12)
			.mapAttribute(Project.class, 2, NAME, String.class).mapAttribute(TestCase.class, 4, NAME, String.class)
			.mapAttribute(TestCase.class, 4, "reference", String.class)
			.mapAttribute(TestCase.class, 5, NAME, String.class)
			.mapAttribute(TestCase.class, 6, "importance", TestCaseImportance.class)			
			.mapAttribute(TestCase.class, 7, "executionMode", TestCaseExecutionMode.class)
			.mapAttribute(IterationTestPlanItem.class, 8, "executionStatus", ExecutionStatus.class)
			.mapAttribute(IterationTestPlanItem.class, 9, "lastExecutedBy", String.class)
			.mapAttribute(IterationTestPlanItem.class, 10, "lastExecutedOn", Date.class);

	// will return the fragment only
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showTestSuite(@PathVariable long id) {
		TestSuite testSuite = service.findById(id);
		TestPlanStatistics testSuiteStats = service.findTestSuiteStatistics(id);
		boolean hasCUF = cufValueService.hasCustomFields(testSuite);

		ModelAndView mav = new ModelAndView("fragment/test-suites/edit-test-suite");
		mav.addObject(TEST_SUITE, testSuite);
		mav.addObject("statistics", testSuiteStats);
		mav.addObject("hasCUF", hasCUF);
		return mav;
	}

	// will return the iteration in a full page
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView showTestSuiteInfo(@PathVariable long id) {

		TestSuite testSuite = service.findById(id);

		TestPlanStatistics testSuiteStats = service.findTestSuiteStatistics(id);

		ModelAndView mav = new ModelAndView("page/campaign-libraries/show-test-suite");

		if (testSuite != null) {
			boolean hasCUF = cufValueService.hasCustomFields(testSuite);
			mav.addObject(TEST_SUITE, testSuite);
			mav.addObject("statistics", testSuiteStats);
			mav.addObject("hasCUF", hasCUF);
		} else {
			testSuite = new TestSuite();
			testSuite.setName("Not found");
			testSuite.setDescription("This test suite either do not exists, or was removed");
			mav.addObject(TEST_SUITE, testSuite);
			mav.addObject("hasCUF", false);

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

	@RequestMapping(value = "/statistics", method = RequestMethod.GET)
	public ModelAndView refreshStats(@PathVariable long id) {

		TestPlanStatistics testSuiteStats = service.findTestSuiteStatistics(id);

		ModelAndView mav = new ModelAndView("fragment/generics/statistics-fragment");
		mav.addObject("statisticsEntity", testSuiteStats);
		
		return mav;
	}

	@RequestMapping(value = "/exec-button", method = RequestMethod.GET)
	public ModelAndView refreshExecButton(@PathVariable long id) {

		TestPlanStatistics testSuiteStats = service.findTestSuiteStatistics(id);

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
	public Object rename(HttpServletResponse response, @RequestParam("newName") String newName, @PathVariable long id) {

		LOGGER.info("TestSuiteModificationController : renaming " + id + " as " + newName);
		service.rename(id, newName);
		return new RenameModel(newName);

	}

	// that method is useful too so don't remove it !
	@RequestMapping(value = "/rename", method = RequestMethod.POST, params = NAME)
	public @ResponseBody
	Map<String, String> renameTestSuite(@PathVariable("id") Long id, @RequestParam(NAME) String name) {
		service.rename(id, name);
		Map<String, String> result = new HashMap<String, String>();
		result.put("id", id.toString());
		result.put(NAME, name);
		return result;
	}

	@RequestMapping(value = "/test-cases", method = RequestMethod.POST, params = "test-cases[]")
	public @ResponseBody
	Map<String, String> bindTestPlan(@PathVariable("id") long suiteId, @RequestParam("test-cases[]") List<Long> itpIds) {
		service.bindTestPlan(suiteId, itpIds);
		Map<String, String> result = new HashMap<String, String>();
		result.put("id", Long.toString(suiteId));
		return result;
	}

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
	public void changeTestPlanIndex(@PathVariable("id") long testSuiteId, @RequestParam int newIndex,
			@RequestParam("itemIds[]") List<Long> itemIds) {
		service.changeTestPlanPosition(testSuiteId, newIndex, itemIds);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("test-suite " + testSuiteId + ": moving " + itemIds.size() + " test plan items  to "
					+ newIndex);
		}
	}
	
	@RequestMapping(value = "{iterationId}/test-case-executions/{testPlanId}", method = RequestMethod.GET)
	public ModelAndView getExecutionsForTestPlan(@PathVariable long id, @PathVariable long iterationId,
			@PathVariable long testPlanId) {
		TestSuite testSuite = service.findById(id);
		List<Execution> executionList = iterationModService.findExecutionsByTestPlan(iterationId, testPlanId);
		// get the iteraction to check access rights
		Iteration iter = iterationModService.findById(iterationId);
		IterationTestPlanItem iterationTestPlanItem = iterationTestPlanFinder.findTestPlanItem(iterationId, testPlanId);
		boolean editable = permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", iter);

		ModelAndView mav = new ModelAndView("fragment/test-suites/test-suite-test-plan-row");

		mav.addObject("editableIteration", editable);
		mav.addObject("testPlanItem", iterationTestPlanItem);
		mav.addObject("iterationId", iterationId);
		mav.addObject("executions", executionList);
		mav.addObject(TEST_SUITE, testSuite);

		return mav;

	}
	
	@RequestMapping(value = "/test-plan/table", params = "sEcho")
	public @ResponseBody
	DataTableModel getTestPlanModel(@PathVariable long id, final DataTableDrawParameters params, final Locale locale) {

		Paging paging = new DataTableMapperPagingAndSortingAdapter(params, testPlanMapper);

		PagedCollectionHolder<List<IterationTestPlanItem>> holder = service.findTestSuiteTestPlan(id, paging);

		return new IterationTestPlanItemDataTableModelHelper(messageSource, locale).buildDataModel(holder,
				params.getsEcho());

	}

	private static class IterationTestPlanItemDataTableModelHelper extends DataTableModelHelper<IterationTestPlanItem> {
		private InternationalizationHelper messageSource;
		private Locale locale;

		private IterationTestPlanItemDataTableModelHelper(InternationalizationHelper messageSource, Locale locale) {
			this.messageSource = messageSource;
			this.locale = locale;
		}

		@Override
		public Object[] buildItemData(IterationTestPlanItem item) {

			String projectName;
			String testCaseName;
			String reference;
			final String testCaseExecutionMode = messageSource.internationalize(item.getExecutionMode(), locale);
			String importance;
			final String automationMode = item.isAutomated() ? "A" : "M";

			if (item.isTestCaseDeleted()) {
				projectName = formatNoData(locale, messageSource);
				testCaseName = formatDeleted(locale, messageSource);
				importance = formatNoData(locale, messageSource);
				reference = formatNoData(locale, messageSource);
			} else {
				projectName = item.getReferencedTestCase().getProject().getName();
				testCaseName = item.getReferencedTestCase().getName();
				reference = item.getReferencedTestCase().getReference();
				importance = messageSource.internationalize(item.getReferencedTestCase().getImportance(), locale);
			}

			return new Object[] { item.getId(), getCurrentIndex(), projectName, automationMode, reference,
					testCaseName, importance, testCaseExecutionMode,
					messageSource.internationalize(item.getExecutionStatus(), locale),
					formatString(item.getLastExecutedBy(), locale, messageSource),
					messageSource.localizeDate(item.getLastExecutedOn(), locale), item.isTestCaseDeleted(), " ", " " };
		}
	}

	/* ************** execute auto *********************************** */

	@RequestMapping(method = RequestMethod.POST, params = { "id=execute-auto", "testPlanItemsIds[]" })
	public @ResponseBody
	AutomatedSuiteOverview executeSelectionAuto(@PathVariable long id,
			@RequestParam("testPlanItemsIds[]") List<Long> ids, Locale locale) {

		TestSuite suite = service.findById(id);
		long iterationId = suite.getIteration().getId();

		AutomatedSuite autoSuite = iterationModService.createAutomatedSuite(iterationId, ids);
		testAutomationService.startAutomatedSuite(autoSuite);

		LOGGER.debug("Test-Suite #" + id + " : execute selected test plans");

		return AutomatedExecutionViewUtils.buildExecInfo(autoSuite, locale, messageSource);

	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=execute-auto", "!testPlanItemsIds[]" })
	public @ResponseBody
	AutomatedSuiteOverview executeAllAuto(@PathVariable long id, Locale locale) {
		AutomatedSuite suite = service.createAutomatedSuite(id);
		testAutomationService.startAutomatedSuite(suite);

		LOGGER.debug("Test-Suite #" + id + " : execute all test plan auto");

		return AutomatedExecutionViewUtils.buildExecInfo(suite, locale, messageSource);

	}

	/* ************** /execute auto *********************************** */

	/* ***************** data formatter *************************** */

	private static String formatString(String arg, Locale locale, InternationalizationHelper messageSource) {
		if (arg == null) {
			return formatNoData(locale, messageSource);
		} else {
			return arg;
		}
	}

	private static String formatNoData(Locale locale, InternationalizationHelper messageSource) {
		return messageSource.internationalize("squashtm.nodata", locale);
	}

	private static String formatDeleted(Locale locale, InternationalizationHelper messageSource) {
		return messageSource.internationalize("squashtm.itemdeleted", locale);
	}
}

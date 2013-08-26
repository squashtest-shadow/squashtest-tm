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
package org.squashtest.tm.web.internal.controller.bugtracker;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.bugtracker.advanceddomain.AdvancedIssue;
import org.squashtest.tm.bugtracker.advanceddomain.DelegateCommand;
import org.squashtest.tm.bugtracker.definition.Attachment;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.campaign.CampaignFinder;
import org.squashtest.tm.service.campaign.IterationFinder;
import org.squashtest.tm.service.campaign.TestSuiteFinder;
import org.squashtest.tm.service.execution.ExecutionFinder;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.testcase.TestCaseFinder;
import org.squashtest.tm.web.internal.controller.bugtracker.BugTrackerControllerHelper.ExecutionIssuesTableModel;
import org.squashtest.tm.web.internal.controller.bugtracker.BugTrackerControllerHelper.IterationIssuesTableModel;
import org.squashtest.tm.web.internal.controller.bugtracker.BugTrackerControllerHelper.StepIssuesTableModel;
import org.squashtest.tm.web.internal.controller.bugtracker.BugTrackerControllerHelper.TestCaseIssuesTableModel;
import org.squashtest.tm.web.internal.model.customeditor.AttachmentPropertyEditorSupport;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;

@Controller
@RequestMapping("/bugtracker")
public class BugTrackerController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerController.class);

	private BugTrackersLocalService bugTrackersLocalService;
	private CampaignFinder campaignFinder;
	private IterationFinder iterationFinder;
	private TestSuiteFinder testSuiteFinder;
	private ExecutionFinder executionFinder;
	private TestCaseFinder testCaseFinder;
	private ProjectFinder projectFinder;
	private BugTrackerFinderService bugTrackerFinderService;

	private static final String EXECUTION_STEP_TYPE = "execution-step";
	private static final String EXECUTION_TYPE = "execution";
	private static final String ITERATION_TYPE = "iteration";
	private static final String CAMPAIGN_TYPE = "campaign";
	private static final String TEST_SUITE_TYPE = "test-suite";
	private static final String TEST_CASE_TYPE = "test-case";
	private static final String BUGTRACKER_ID = "bugTrackerId";
	private static final String EMPTY_BUGTRACKER_MAV = "fragment/issues/bugtracker-panel-empty";

	private static final String STYLE = "style";
	private static final String TOGGLE = "toggle";
	private static final String DELEGATE_POPUP = "useParentContextPopup";

	@Inject
	private MessageSource messageSource;

	@ServiceReference
	public void setProjectFinder(ProjectFinder projectFinder) {
		this.projectFinder = projectFinder;
	}

	@ServiceReference
	public void setCampaignFinder(CampaignFinder campaignFinder) {
		this.campaignFinder = campaignFinder;
	}

	@ServiceReference
	public void setIterationFinder(IterationFinder iterationFinder) {
		this.iterationFinder = iterationFinder;
	}

	@ServiceReference
	public void setTestSuiteFinder(TestSuiteFinder testSuiteFinder) {
		this.testSuiteFinder = testSuiteFinder;
	}

	@ServiceReference
	public void setExecutionFinder(ExecutionFinder executionFinder) {
		this.executionFinder = executionFinder;
	}

	@ServiceReference
	public void setTestCaseFinder(TestCaseFinder testCaseFinder) {
		this.testCaseFinder = testCaseFinder;
	}

	@ServiceReference
	public void setBugTrackersLocalService(BugTrackersLocalService bugTrackersLocalService) {
		if (bugTrackersLocalService == null) {
			throw new IllegalArgumentException("BugTrackerController : no service provided");
		}
		this.bugTrackersLocalService = bugTrackersLocalService;
	}

	@ServiceReference
	public void setBugTrackerFinderService(BugTrackerFinderService bugTrackerFinderService) {
		this.bugTrackerFinderService = bugTrackerFinderService;
	}

	@InitBinder
	public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
		binder.registerCustomEditor(org.squashtest.tm.domain.attachment.Attachment.class,
				new AttachmentPropertyEditorSupport());
	}

	/* **************************************************************************************************************
	 * *
	 * Navigation button * *
	 * ***********************************************************************************************************
	 */
	@RequestMapping(value = "workspace-button", method = RequestMethod.GET)
	public ModelAndView getNavButton(Locale locale) {
		List<Project> projects = projectFinder.findAllReadable();
		List<Long> projectsIds = IdentifiedUtil.extractIds(projects);
		List<BugTracker> readableBugTrackers = bugTrackerFinderService.findDistinctBugTrackersForProjects(projectsIds);
		if (readableBugTrackers.isEmpty()) {
			LOGGER.trace("no bugtracker");
			return new ModelAndView("fragment/issues/bugtracker-panel-empty");
		} else {
			LOGGER.trace("return bugtracker nav button");
			ModelAndView mav = new ModelAndView("fragment/issues/bugtracker-nav-button");
			mav.addObject("bugtrackers", readableBugTrackers);
			return mav;
		}
	}

	@RequestMapping(value = "{bugtrackerId}/workspace", method = RequestMethod.GET)
	public ModelAndView showWorkspace(@PathVariable Long bugtrackerId) {
		BugTracker bugTracker = bugTrackerFinderService.findById(bugtrackerId);
		ModelAndView mav = new ModelAndView("page/bugtracker-workspace");
		mav.addObject("bugtrackerUrl", bugTracker.getUrl().toString());
		return mav;
	}

	/* **************************************************************************************************************
	 * *
	 * ExecutionStep level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * <p>
	 * returns the panel displaying the current bugs of that execution step and the stub for the report form. Remember
	 * that the report bug dialog will be populated later.
	 * </p>
	 * <p>
	 * Note : accepts as optional parameter : 
	 * <ul><li>useParentContextPopup : will tell the panel to use a delegate report issue popup (that's how the OER works)  
	 * </p>
	 * @param stepId
	 * @return
	 */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}", method = RequestMethod.GET)
	public ModelAndView getExecStepIssuePanel(@PathVariable Long stepId, Locale locale,
			@RequestParam(value = STYLE, required = false, defaultValue = TOGGLE) String panelStyle,
			@RequestParam(value = "useDelegatePopup", required = false, defaultValue = "false") Boolean useParentPopup) {

		ExecutionStep step = executionFinder.findExecutionStepById(stepId);
		ModelAndView mav = makeIssuePanel(step, EXECUTION_STEP_TYPE, locale, panelStyle, step.getProject());
		mav.addObject("useParentContextPopup", useParentPopup);
		return mav;
	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getExecStepKnownIssuesData(@PathVariable("stepId") Long stepId,
			final DataTableDrawParameters params, final Locale locale) {

		PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> filteredCollection;
		PagingAndSorting sorter = new IssueCollectionSorting(params);
		try {

			filteredCollection = bugTrackersLocalService.findSortedIssueOwnerShipsForExecutionStep(stepId, sorter);
		}

		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyIssueDecoratorCollectionHolder(EXECUTION_STEP_TYPE, stepId, noCrdsException,
					sorter);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyIssueDecoratorCollectionHolder(EXECUTION_STEP_TYPE, stepId, npException,
					sorter);
		}

		return new StepIssuesTableModel(bugTrackersLocalService).buildDataModel(filteredCollection, params.getsEcho());

	}

	/**
	 * will prepare a bug report for an execution step. The returned json infos will populate the form.
	 * 
	 * @param stepId
	 * @return
	 */

	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/new-issue")
	@ResponseBody
	public RemoteIssue getExecStepReportStub(@PathVariable Long stepId, Locale locale, HttpServletRequest request) {

		ExecutionStep step = executionFinder.findExecutionStepById(stepId);

		String executionUrl = BugTrackerControllerHelper.buildExecutionUrl(request, step.getExecution());

		return makeReportIssueModel(step, locale, executionUrl);
	}

	/**
	 * posts a new issue (simple model)
	 * 
	 */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/new-issue", method = RequestMethod.POST)
	@ResponseBody
	public Object postExecStepIssueReport(@PathVariable("stepId") Long stepId, @RequestBody BTIssue jsonIssue) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution-step " + stepId);

		IssueDetector entity = executionFinder.findExecutionStepById(stepId);

		if (jsonIssue.hasBlankId()) {
			return processIssue(jsonIssue, entity);
		} else {
			return attachIssue(jsonIssue, entity);
		}
	}

	/**
	 * posts a new issue (advanced model)
	 * 
	 * 
	 */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/new-advanced-issue", method = RequestMethod.POST)
	@ResponseBody
	public Object postExecStepAdvancedIssueReport(@PathVariable("stepId") Long stepId,
			@RequestBody AdvancedIssue jsonIssue) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution-step " + stepId);

		IssueDetector entity = executionFinder.findExecutionStepById(stepId);

		if (jsonIssue.hasBlankId()) {
			return processIssue(jsonIssue, entity);
		} else {
			return attachIssue(jsonIssue, entity);
		}
	}

	/* **************************************************************************************************************
	 * *
	 * Execution level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that execution and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 * 
	 * @param stepId
	 * @return
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}", method = RequestMethod.GET)
	public ModelAndView getExecIssuePanel(@PathVariable Long execId, Locale locale,
			@RequestParam(value = STYLE, required = false, defaultValue = TOGGLE) String panelStyle) {
		Execution bugged = executionFinder.findById(execId);
		return makeIssuePanel(bugged, EXECUTION_TYPE, locale, panelStyle, bugged.getProject());

	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getExecKnownIssuesData(@PathVariable("execId") Long execId, final DataTableDrawParameters params,
			final Locale locale) {

		PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> filteredCollection;

		PagingAndSorting sorter = new IssueCollectionSorting(params);
		try {
			filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipsforExecution(execId, sorter);

		}

		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyIssueDecoratorCollectionHolder(EXECUTION_TYPE, execId, noCrdsException,
					sorter);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyIssueDecoratorCollectionHolder(EXECUTION_TYPE, execId, npException, sorter);
		}

		return new ExecutionIssuesTableModel(bugTrackersLocalService, messageSource, locale).buildDataModel(
				filteredCollection, params.getsEcho());

	}

	/**
	 * will prepare a bug report for an execution. The returned json infos will populate the form.
	 * 
	 * @param execId
	 * @return
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/new-issue")
	@ResponseBody
	public RemoteIssue getExecReportStub(@PathVariable Long execId, Locale locale, HttpServletRequest request) {
		Execution execution = executionFinder.findById(execId);
		String executionUrl = BugTrackerControllerHelper.buildExecutionUrl(request, execution);
		return makeReportIssueModel(execution, locale, executionUrl);
	}

	/**
	 * posts a new issue (simple model)
	 * 
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/new-issue", method = RequestMethod.POST)
	@ResponseBody
	public Object postExecIssueReport(@PathVariable("execId") Long execId, @RequestBody BTIssue jsonIssue) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution-step " + execId);

		Execution entity = executionFinder.findById(execId);

		if (jsonIssue.hasBlankId()) {
			return processIssue(jsonIssue, entity);
		} else {
			return attachIssue(jsonIssue, entity);
		}
	}

	/**
	 * posts a new issue (advanced model)
	 * 
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/new-advanced-issue", method = RequestMethod.POST)
	@ResponseBody
	public Object postExecAdvancedIssueReport(@PathVariable("execId") Long execId, @RequestBody AdvancedIssue jsonIssue) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution-step " + execId);

		Execution entity = executionFinder.findById(execId);

		if (jsonIssue.hasBlankId()) {
			return processIssue(jsonIssue, entity);
		} else {
			return attachIssue(jsonIssue, entity);
		}
	}

	/* **************************************************************************************************************
	 * *
	 * TestCase level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that testCase and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 * 
	 * @param tcId
	 * @return
	 */
	@RequestMapping(value = TEST_CASE_TYPE + "/{tcId}", method = RequestMethod.GET)
	public ModelAndView getTestCaseIssuePanel(@PathVariable Long tcId, Locale locale,
			@RequestParam(value = STYLE, required = false, defaultValue = TOGGLE) String panelStyle) {

		TestCase testCase = testCaseFinder.findById(tcId);
		return makeIssuePanel(testCase, TEST_CASE_TYPE, locale, panelStyle, testCase.getProject());

	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = TEST_CASE_TYPE + "/{tcId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getTestCaseKnownIssuesData(@PathVariable("tcId") Long tcId, final DataTableDrawParameters params,
			final Locale locale) {

		PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> filteredCollection;
		PagingAndSorting sorter = new IssueCollectionSorting(params);
		try {
			filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipForTestCase(tcId, sorter);
		}
		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyIssueDecoratorCollectionHolder(TEST_CASE_TYPE, tcId, noCrdsException, sorter);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyIssueDecoratorCollectionHolder(TEST_CASE_TYPE, tcId, npException, sorter);
		}
		return new TestCaseIssuesTableModel(bugTrackersLocalService, messageSource, locale).buildDataModel(
				filteredCollection, params.getsEcho());

	}

	/* **************************************************************************************************************
	 * *
	 * Iteration level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that iteration and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 * 
	 * @param iterId
	 * @return
	 */
	@RequestMapping(value = ITERATION_TYPE + "/{iterId}", method = RequestMethod.GET)
	public ModelAndView getIterationIssuePanel(@PathVariable Long iterId, Locale locale,
			@RequestParam(value = STYLE, required = false, defaultValue = TOGGLE) String panelStyle) {

		Iteration iteration = iterationFinder.findById(iterId);
		return makeIssuePanel(iteration, ITERATION_TYPE, locale, panelStyle, iteration.getProject());

	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = ITERATION_TYPE + "/{iterId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getIterationKnownIssuesData(@PathVariable("iterId") Long iterId,
			final DataTableDrawParameters params, final Locale locale) {

		PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> filteredCollection;
		PagingAndSorting sorter = new IssueCollectionSorting(params);
		try {
			filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipForIteration(iterId, sorter);
			// no credentials exception are okay, the rest is to be treated as usual

		} catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyIssueDecoratorCollectionHolder(ITERATION_TYPE, iterId, noCrdsException,
					sorter);

		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyIssueDecoratorCollectionHolder(ITERATION_TYPE, iterId, npException, sorter);

		}
		return new IterationIssuesTableModel(bugTrackersLocalService, messageSource, locale).buildDataModel(
				filteredCollection, params.getsEcho());

	}

	/* **************************************************************************************************************
	 * *
	 * Campaign level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that campaign and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 * 
	 * @param iterId
	 * @return
	 */
	@RequestMapping(value = CAMPAIGN_TYPE + "/{campId}", method = RequestMethod.GET)
	public ModelAndView getCampaignIssuePanel(@PathVariable Long campId, Locale locale,
			@RequestParam(value = STYLE, required = false, defaultValue = TOGGLE) String panelStyle) {

		Campaign campaign = campaignFinder.findById(campId);
		return makeIssuePanel(campaign, CAMPAIGN_TYPE, locale, panelStyle, campaign.getProject());

	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = CAMPAIGN_TYPE + "/{campId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getCampaignKnownIssuesData(@PathVariable("campId") Long campId,
			final DataTableDrawParameters params, final Locale locale) {

		PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> filteredCollection;
		PagingAndSorting sorter = new IssueCollectionSorting(params);
		try {
			filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipsForCampaigns(campId, sorter);
			// no credentials exception are okay, the rest is to be treated as usual
		} catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyIssueDecoratorCollectionHolder(CAMPAIGN_TYPE, campId, noCrdsException, sorter);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyIssueDecoratorCollectionHolder(CAMPAIGN_TYPE, campId, npException, sorter);
		}
		return new IterationIssuesTableModel(bugTrackersLocalService, messageSource, locale).buildDataModel(
				filteredCollection, params.getsEcho());
	}

	/* **************************************************************************************************************
	 * *
	 * TestSuite level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that test-suite and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 * 
	 * @param testSuiteId
	 * @return
	 */
	@RequestMapping(value = TEST_SUITE_TYPE + "/{testSuiteId}", method = RequestMethod.GET)
	public ModelAndView getTestSuiteIssuePanel(@PathVariable Long testSuiteId, Locale locale,
			@RequestParam(value = STYLE, required = false, defaultValue = TOGGLE) String panelStyle) {

		TestSuite testSuite = testSuiteFinder.findById(testSuiteId);
		return makeIssuePanel(testSuite, TEST_SUITE_TYPE, locale, panelStyle, testSuite.getIteration().getProject());

	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = TEST_SUITE_TYPE + "/{testSuiteId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getTestSuiteKnownIssuesData(@PathVariable("testSuiteId") Long testSuiteId,
			final DataTableDrawParameters params, final Locale locale) {

		PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> filteredCollection;
		PagingAndSorting sorter = new IssueCollectionSorting(params);
		try {
			filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipsForTestSuite(testSuiteId, sorter);
		}
		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyIssueDecoratorCollectionHolder(TEST_SUITE_TYPE, testSuiteId, noCrdsException,
					sorter);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyIssueDecoratorCollectionHolder(TEST_SUITE_TYPE, testSuiteId, npException,
					sorter);
		}
		return new IterationIssuesTableModel(bugTrackersLocalService, messageSource, locale).buildDataModel(
				filteredCollection, params.getsEcho());

	}

	/* ************************* Generic code section ************************** */

	@RequestMapping(value = "/find-issue/{remoteKey}", method = RequestMethod.GET, params = { BUGTRACKER_ID })
	@ResponseBody
	public RemoteIssue findIssue(@PathVariable("remoteKey") String remoteKey,
			@RequestParam(BUGTRACKER_ID) long bugTrackerId) {
		BugTracker bugTracker = bugTrackerFinderService.findById(bugTrackerId);
		return bugTrackersLocalService.getIssue(remoteKey, bugTracker);
	}

	@RequestMapping(value = "/credentials", method = RequestMethod.POST, params = { "login", "password", BUGTRACKER_ID })
	public @ResponseBody
	Map<String, String> setCredendials(@RequestParam("login") String login, @RequestParam("password") String password,
			@RequestParam(BUGTRACKER_ID) long bugTrackerId) {
		BugTracker bugTracker = bugTrackerFinderService.findById(bugTrackerId);
		bugTrackersLocalService.setCredentials(login, password, bugTracker);

		Map<String, String> map = new HashMap<String, String>();
		map.put("status", "ok");
		return map;

	}

	@RequestMapping(value = "/status", method = RequestMethod.GET, params = { "projectId" })
	public @ResponseBody
	Object getBugTrackerStatus(@RequestParam("projectId") Long projectId) {
		String strStatus = null;

		BugTrackerStatus status = checkStatus(projectId);

		if (status == BugTrackerStatus.BUGTRACKER_READY) {
			strStatus = "ready";
		} else if (status == BugTrackerStatus.BUGTRACKER_NEEDS_CREDENTIALS) {
			strStatus = "needs_credentials";
		} else {
			strStatus = "bt_undefined";
		}

		Map<String, String> result = new HashMap<String, String>();
		result.put("status", strStatus);
		return result;
	}

	// FIXME : check first if a bugtracker is defined and if the credentials are set
	private Map<String, String> processIssue(RemoteIssue issue, IssueDetector entity) {
		final RemoteIssue postedIssue = bugTrackersLocalService.createIssue(entity, issue);
		final URL issueUrl = bugTrackersLocalService.getIssueUrl(postedIssue.getId(), entity.getBugTracker());

		Map<String, String> result = new HashMap<String, String>();
		result.put("url", issueUrl.toString());
		result.put("issueId", postedIssue.getId());

		return result;
	}

	private Map<String, String> attachIssue(final RemoteIssue issue, IssueDetector entity) {

		bugTrackersLocalService.attachIssue(entity, issue.getId());
		final URL issueUrl = bugTrackersLocalService.getIssueUrl(issue.getId(), entity.getBugTracker());

		Map<String, String> result = new HashMap<String, String>();
		result.put("url", issueUrl.toString());
		result.put("issueId", issue.getId());

		return result;
	}

	@RequestMapping(value = "/issues/{issueId}", method = RequestMethod.DELETE)
	public @ResponseBody
	void detachIssue(@PathVariable("issueId") Long issueId) {
		bugTrackersLocalService.detachIssue(issueId);
	}

	@RequestMapping(value = "/{btName}/remote-issues/{remoteIssueId}/attachments", method = RequestMethod.POST)
	public @ResponseBody
	void forwardAttachmentsToIssue(@PathVariable("btName") String btName,
			@PathVariable("remoteIssueId") String remoteIssueId,
			@RequestParam("attachment[]") List<org.squashtest.tm.domain.attachment.Attachment> attachments) {

		List<Attachment> issueAttachments = new ArrayList<Attachment>(attachments.size());
		for (org.squashtest.tm.domain.attachment.Attachment attach : attachments) {
			Attachment newAttachment = new Attachment(attach.getName(), attach.getSize(), attach.getContent()
					.getContent());
			issueAttachments.add(newAttachment);
		}

		bugTrackersLocalService.forwardAttachments(remoteIssueId, btName, issueAttachments);

		// now ensure that the input streams are closed
		for (Attachment attachment : issueAttachments) {
			try {
				attachment.getStreamContent().close();
			} catch (IOException ex) {
				LOGGER.warn("issue attachments : could not close stream for " + attachment.getName()
						+ ", this is non fatal anyway");
			}
		}

	}

	@RequestMapping(value = "{btName}/command", method = RequestMethod.POST)
	public @ResponseBody
	Object forwardDelegateCommand(@PathVariable("btName") String bugtrackerName, @RequestBody DelegateCommand command) {
		return bugTrackersLocalService.forwardDelegateCommand(command, bugtrackerName);
	}

	/* ********* generates a json model for an issue ******* */

	private RemoteIssue makeReportIssueModel(Execution exec, Locale locale, String executionUrl) {
		String defaultDescription = BugTrackerControllerHelper.getDefaultDescription(exec, locale, messageSource,
				executionUrl);
		return makeReportIssueModel(exec, defaultDescription);
	}

	private RemoteIssue makeReportIssueModel(ExecutionStep step, Locale locale, String executionUrl) {
		String defaultDescription = BugTrackerControllerHelper.getDefaultDescription(step, locale, messageSource,
				executionUrl);
		String defaultAdditionalInformations = BugTrackerControllerHelper.getDefaultAdditionalInformations(step,
				locale, messageSource);
		return makeReportIssueModel(step, defaultDescription, defaultAdditionalInformations, locale);
	}

	private RemoteIssue makeReportIssueModel(ExecutionStep step, String defaultDescription,
			String defaultAdditionalInformations, Locale locale) {
		RemoteIssue emptyIssue = makeReportIssueModel(step, defaultDescription);
		String comment = BugTrackerControllerHelper.getDefaultAdditionalInformations(step, locale, messageSource);
		emptyIssue.setComment(comment);
		return emptyIssue;
	}

	private RemoteIssue makeReportIssueModel(IssueDetector entity, String defaultDescription) {
		String projectName = entity.getProject().getBugtrackerBinding().getProjectName();

		RemoteIssue emptyIssue = bugTrackersLocalService.createReportIssueTemplate(projectName, entity.getBugTracker());

		emptyIssue.setDescription(defaultDescription);

		return emptyIssue;

	}

	/*
	 * generates the ModelAndView for the bug section.
	 * 
	 * If the bugtracker isn'st defined no panel will be sent at all.
	 */
	private ModelAndView makeIssuePanel(Identified entity, String type, Locale locale, String panelStyle,
			Project project) {
		if (project.isBugtrackerConnected()) {
			BugTrackerStatus status = checkStatus(project.getId());
			// JSON STATUS TODO

			BugTrackerInterfaceDescriptor descriptor = bugTrackersLocalService.getInterfaceDescriptor(project
					.findBugTracker());
			descriptor.setLocale(locale);

			ModelAndView mav = new ModelAndView("fragment/issues/bugtracker-panel");
			mav.addObject("entity", entity);
			mav.addObject("entityType", type);
			mav.addObject("interfaceDescriptor", descriptor);
			mav.addObject("panelStyle", panelStyle);
			mav.addObject("bugTrackerStatus", status);
			mav.addObject("project", project);
			mav.addObject("bugTracker", project.findBugTracker());
			mav.addObject("delete", "");
			return mav;
		} else {
			return new ModelAndView(EMPTY_BUGTRACKER_MAV);
		}

	}

	/* ******************************* private methods ********************************************** */

	private BugTrackerStatus checkStatus(long projectId) {
		return bugTrackersLocalService.checkBugTrackerStatus(projectId);
	}

	private static final class IssueCollectionSorting implements PagingAndSorting {

		private DataTableDrawParameters params;

		private IssueCollectionSorting(final DataTableDrawParameters params) {
			this.params = params;
		}

		@Override
		public int getFirstItemIndex() {
			return params.getiDisplayStart();
		}


		@Override
		public String getSortedAttribute() {
			return "Issue.id";
		}

		@Override
		public int getPageSize() {
			return params.getiDisplayLength();
		}

		@Override
		public boolean shouldDisplayAll() {
			return (getPageSize() < 0);
		}

		/**
		 * @see org.squashtest.tm.core.foundation.collection.Sorting#getSortOrder()
		 */
		@Override
		public SortOrder getSortOrder() {
			return SortOrder.coerceFromCode(params.getsSortDir_0());
		}

	}

	private PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> makeEmptyIssueDecoratorCollectionHolder(
			String entityName, Long entityId, Exception cause, PagingAndSorting paging) {
		LOGGER.trace("BugTrackerController : fetching known issues for  " + entityName + " " + entityId
				+ " failed, exception : ", cause);
		List<IssueOwnership<RemoteIssueDecorator>> emptyList = new LinkedList<IssueOwnership<RemoteIssueDecorator>>();
		return new PagingBackedPagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>>(paging, 0, emptyList);
	}

}

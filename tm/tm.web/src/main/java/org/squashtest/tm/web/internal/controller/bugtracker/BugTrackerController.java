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
import org.springframework.context.i18n.LocaleContextHolder;
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
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.domain.Identified;
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
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.campaign.CampaignFinder;
import org.squashtest.tm.service.campaign.IterationFinder;
import org.squashtest.tm.service.campaign.TestSuiteFinder;
import org.squashtest.tm.service.execution.ExecutionFinder;
import org.squashtest.tm.service.testcase.TestCaseFinder;
import org.squashtest.tm.web.internal.controller.attachment.UploadedData;
import org.squashtest.tm.web.internal.controller.attachment.UploadedDataPropertyEditorSupport;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;

@Controller
@RequestMapping("/bugtracker")
public class BugTrackerController {

	private static final String SORTING_DEFAULT_ATTRIBUTE = "Issue.id";


	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerController.class);


	@Inject private BugTrackersLocalService bugTrackersLocalService;
	@Inject	private CampaignFinder campaignFinder;
	@Inject private IterationFinder iterationFinder;
	@Inject private TestSuiteFinder testSuiteFinder;
	@Inject private ExecutionFinder executionFinder;
	@Inject private TestCaseFinder testCaseFinder;
	@Inject private BugTrackerManagerService bugTrackerManagerService;

	@Inject private MessageSource messageSource;
	@Inject private BugTrackerControllerHelper helper;

	static final String EXECUTION_STEP_TYPE = "execution-step";
	static final String EXECUTION_TYPE = "execution";
	static final String ITERATION_TYPE = "iteration";
	static final String CAMPAIGN_TYPE = "campaign";
	static final String TEST_SUITE_TYPE = "test-suite";
	static final String TEST_CASE_TYPE = "test-case";
	private static final String EMPTY_BUGTRACKER_MAV = "fragment/issues/bugtracker-panel-empty";

	private static final String BUGTRACKER_ID = "bugTrackerId";
	private static final String EMPTY_BUGTRACKER_MAV = "fragment/bugtracker/bugtracker-panel-empty";
	private static final String STYLE_ARG = "style";
	private static final String STYLE_TOGGLE = "toggle";
	private static final String STYLE_TAB = "fragment-tab";
	private static final String MODEL_TABLE_ENTRIES = "tableEntries";
	private static final String MODEL_BUG_TRACKER_STATUS = "bugTrackerStatus";


	@InitBinder
	public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
		binder.registerCustomEditor(UploadedData.class,
				new UploadedDataPropertyEditorSupport());
	}

	/* **************************************************************************************************************
	 * *
	 * Navigation button * *
	 * ***********************************************************************************************************
	 */


	@RequestMapping(value = "{bugtrackerId}/workspace", method = RequestMethod.GET)
	public ModelAndView showWorkspace(@PathVariable Long bugtrackerId) {
		BugTracker bugTracker = bugTrackerManagerService.findById(bugtrackerId);
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
			@RequestParam(value = STYLE_ARG, required = false, defaultValue = STYLE_TOGGLE) String panelStyle,
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

		PagingAndSorting sorter = new IssueCollectionSorting(params);

		return getKnownIssuesData(EXECUTION_STEP_TYPE, stepId, sorter, params.getsEcho());

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
	 *
	 * Execution level section
	 * 
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
			@RequestParam(value = STYLE_ARG, required = false, defaultValue = STYLE_TOGGLE) String panelStyle) {
		Execution bugged = executionFinder.findById(execId);
		return makeIssuePanel(bugged, EXECUTION_TYPE, locale, panelStyle, bugged.getProject());

	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getExecKnownIssuesData(@PathVariable("execId") Long execId, final DataTableDrawParameters params) {

		PagingAndSorting sorter = new IssueCollectionSorting(params);

		return getKnownIssuesData(EXECUTION_TYPE, execId, sorter, params.getsEcho());

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
			@RequestParam(value = STYLE_ARG, required = false, defaultValue = STYLE_TOGGLE) String panelStyle) {

		TestCase testCase = testCaseFinder.findById(tcId);

		ModelAndView mav =  makeIssuePanel(testCase, TEST_CASE_TYPE, locale, panelStyle, testCase.getProject());

		/*
		 * issue 4178
		 * eagerly fetch the row entries if panelStyle is 'fragment-tab'
		 * and if the user is authenticated
		 * (we need the table to be shipped along with the panel in one call)
		 */
		if (shouldGetTableData(mav, panelStyle)){
			DataTableModel issues = getKnownIssuesData(TEST_CASE_TYPE, tcId, new DefaultPagingAndSorting(SORTING_DEFAULT_ATTRIBUTE), "0");
			mav.addObject(MODEL_TABLE_ENTRIES, issues.getAaData());
		}

		return mav;

	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = TEST_CASE_TYPE + "/{tcId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getTestCaseKnownIssuesData(@PathVariable("tcId") Long tcId, final DataTableDrawParameters params) {

		PagingAndSorting sorter = new IssueCollectionSorting(params);

		return getKnownIssuesData(TEST_CASE_TYPE, tcId, sorter, params.getsEcho());

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
			@RequestParam(value = STYLE_ARG, required = false, defaultValue = STYLE_TOGGLE) String panelStyle) {

		Iteration iteration = iterationFinder.findById(iterId);
		ModelAndView mav = makeIssuePanel(iteration, ITERATION_TYPE, locale, panelStyle, iteration.getProject());

		/*
		 * issue 4178
		 * eagerly fetch the row entries if panelStyle is 'fragment-tab'
		 * and if the user is authenticated
		 * (we need the table to be shipped along with the panel in one call)
		 */
		if (shouldGetTableData(mav, panelStyle)){
			DataTableModel issues = getKnownIssuesData(ITERATION_TYPE, iterId, new DefaultPagingAndSorting(SORTING_DEFAULT_ATTRIBUTE), "0");
			mav.addObject(MODEL_TABLE_ENTRIES, issues.getAaData());
		}

		return mav;

	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = ITERATION_TYPE + "/{iterId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getIterationKnownIssuesData(@PathVariable("iterId") Long iterId,
			final DataTableDrawParameters params) {

		PagingAndSorting sorter = new IssueCollectionSorting(params);

		return getKnownIssuesData(ITERATION_TYPE, iterId, sorter, params.getsEcho());

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
			@RequestParam(value = STYLE_ARG, required = false, defaultValue = STYLE_TOGGLE) String panelStyle) {

		Campaign campaign = campaignFinder.findById(campId);
		ModelAndView mav = makeIssuePanel(campaign, CAMPAIGN_TYPE, locale, panelStyle, campaign.getProject());

		/*
		 * issue 4178
		 * eagerly fetch the row entries if panelStyle is 'fragment-tab'
		 * and if the user is authenticated
		 * (we need the table to be shipped along with the panel in one call)
		 */
		if (shouldGetTableData(mav, panelStyle)){
			DataTableModel issues = getKnownIssuesData(CAMPAIGN_TYPE, campId, new DefaultPagingAndSorting(SORTING_DEFAULT_ATTRIBUTE), "0");
			mav.addObject(MODEL_TABLE_ENTRIES, issues.getAaData());
		}

		return mav;
	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = CAMPAIGN_TYPE + "/{campId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getCampaignKnownIssuesData(@PathVariable("campId") Long campId,
			final DataTableDrawParameters params) {

		PagingAndSorting sorter = new IssueCollectionSorting(params);

		return getKnownIssuesData(CAMPAIGN_TYPE, campId, sorter, params.getsEcho());
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
			@RequestParam(value = STYLE_ARG, required = false, defaultValue = STYLE_TOGGLE) String panelStyle) {

		TestSuite testSuite = testSuiteFinder.findById(testSuiteId);
		ModelAndView mav = makeIssuePanel(testSuite, TEST_SUITE_TYPE, locale, panelStyle, testSuite.getIteration().getProject());


		/*
		 * issue 4178
		 * eagerly fetch the row entries if panelStyle is 'fragment-tab'
		 * and if the user is authenticated
		 * (we need the table to be shipped along with the panel in one call)
		 */
		if (shouldGetTableData(mav, panelStyle)){
			DataTableModel issues = getKnownIssuesData(TEST_SUITE_TYPE, testSuiteId, new DefaultPagingAndSorting(SORTING_DEFAULT_ATTRIBUTE), "0");
			mav.addObject(MODEL_TABLE_ENTRIES, issues.getAaData());
		}

		return mav;
	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = TEST_SUITE_TYPE + "/{testSuiteId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getTestSuiteKnownIssuesData(@PathVariable("testSuiteId") Long testSuiteId,
			final DataTableDrawParameters params, final Locale locale) {

		PagingAndSorting sorter = new IssueCollectionSorting(params);

		return getKnownIssuesData(TEST_SUITE_TYPE, testSuiteId, sorter, params.getsEcho());

	}

	/* ************************* Generic code section ************************** */

	@RequestMapping(value = "/find-issue/{remoteKey}", method = RequestMethod.GET, params = { BUGTRACKER_ID })
	@ResponseBody
	public RemoteIssue findIssue(@PathVariable("remoteKey") String remoteKey,
			@RequestParam(BUGTRACKER_ID) long bugTrackerId) {
		BugTracker bugTracker = bugTrackerManagerService.findById(bugTrackerId);
		return bugTrackersLocalService.getIssue(remoteKey, bugTracker);
	}

	@RequestMapping(value = "/credentials", method = RequestMethod.POST, params = { "login", "password", BUGTRACKER_ID })
	public @ResponseBody
	Map<String, String> setCredendials(@RequestParam("login") String login, @RequestParam("password") String password,
			@RequestParam(BUGTRACKER_ID) long bugTrackerId) {
		BugTracker bugTracker = bugTrackerManagerService.findById(bugTrackerId);
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
			@RequestParam("attachment[]") List<UploadedData> uploads) {

		List<Attachment> issueAttachments = new ArrayList<Attachment>(uploads.size());
		for (UploadedData upload : uploads) {
			Attachment newAttachment = new Attachment(upload.name, upload.sizeInBytes, upload.stream);
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
			mav.addObject(MODEL_BUG_TRACKER_STATUS, status);
			mav.addObject("project", project);
			mav.addObject("bugTracker", project.findBugTracker());
			mav.addObject("delete", "");

			return mav;
		} else {
			return new ModelAndView(EMPTY_BUGTRACKER_MAV);
		}

	}

	/* ******************************* private methods ********************************************** */

	private DataTableModel getKnownIssuesData(String entityType, Long id, PagingAndSorting paging, String sEcho) {

		Locale locale = LocaleContextHolder.getLocale();

		PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> filteredCollection;
		DataTableModel model ;

		try {
			switch(entityType){
			case TEST_CASE_TYPE :
				filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipForTestCase(id, paging);
				break;
			case CAMPAIGN_TYPE :
				filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipsForCampaigns(id, paging);
				break;
			case ITERATION_TYPE :
				filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipForIteration(id, paging);
				break;
			case TEST_SUITE_TYPE :
				filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipsForTestSuite(id, paging);
				break;
			case EXECUTION_TYPE :
				filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipsforExecution(id, paging);
				break;
			case EXECUTION_STEP_TYPE :
				filteredCollection = bugTrackersLocalService.findSortedIssueOwnerShipsForExecutionStep(id, paging);
				break;
			default :
				String error = "BugTrackerController : cannot fetch issues for unknown entity type '"+entityType+"'";
				if (LOGGER.isErrorEnabled()){
					LOGGER.error(error);
				}
				throw new IllegalArgumentException(error);
			}
		}
		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException | NullArgumentException exception) {
			filteredCollection = makeEmptyIssueDecoratorCollectionHolder(entityType, id, exception, paging);
		}

		return model = helper.createModelBuilderFor(entityType).buildDataModel(filteredCollection, sEcho);

	}


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
			return SORTING_DEFAULT_ATTRIBUTE;
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

	private boolean shouldGetTableData(ModelAndView mav, String panelStyle){
		return mav.getModel().get(MODEL_BUG_TRACKER_STATUS) == BugTrackerStatus.BUGTRACKER_READY && STYLE_TAB.equals(panelStyle);
	}

}

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
package org.squashtest.csp.tm.web.internal.controller.bugtracker;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.csp.core.domain.Identified;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.csp.tm.domain.bugtracker.IssueDetector;
import org.squashtest.csp.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.BugTrackerLocalService;
import org.squashtest.csp.tm.service.CampaignFinder;
import org.squashtest.csp.tm.service.ExecutionFinder;
import org.squashtest.csp.tm.service.IterationFinder;
import org.squashtest.csp.tm.service.TestCaseFinder;
import org.squashtest.csp.tm.service.TestSuiteFinder;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;

@Controller
@RequestMapping("/bugtracker")
public class BugtrackerController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BugtrackerController.class);

	private BugTrackerLocalService bugTrackerLocalService;
	private CampaignFinder campaignFinder;
	private IterationFinder iterationFinder;
	private TestSuiteFinder testSuiteFinder;
	private ExecutionFinder executionFinder;
	private TestCaseFinder testCaseFinder;

	private static final String EXECUTION_STEP_TYPE = "execution-step";
	private static final String EXECUTION_TYPE = "execution";
	private static final String ITERATION_TYPE = "iteration";
	private static final String CAMPAIGN_TYPE = "campaign";
	private static final String TEST_SUITE_TYPE = "test-suite";
	private static final String TEST_CASE_TYPE = "test-case";

	@Inject
	private MessageSource messageSource;

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
	public void setBugTrackerLocalService(BugTrackerLocalService bugTrackerLocalService) {
		if (bugTrackerLocalService == null) {
			throw new IllegalArgumentException("BugTrackerController : no service provided");
		}
		this.bugTrackerLocalService = bugTrackerLocalService;
	}

	/* **************************************************************************************************************
	 * *
	 * Navigation button * *
	 * ***********************************************************************************************************
	 */
	@RequestMapping(value = "workspace-button", method = RequestMethod.GET)
	public ModelAndView getNavButton(Locale locale) {
		BugTrackerStatus status = checkStatus();

		if (status == BugTrackerStatus.BUGTRACKER_UNDEFINED) {
			LOGGER.trace("no bugtracker");
			return new ModelAndView("fragment/issues/bugtracker-panel-empty");
		} else {
			LOGGER.trace("return bugtracker nav button");
			ModelAndView mav = new ModelAndView("fragment/issues/bugtracker-nav-button");
			mav.addObject("bugtrackerUrl", bugTrackerLocalService.getBugtrackerUrl().toString());
			mav.addObject("iframeFriendly", bugTrackerLocalService.getBugtrackerIframeFriendly());
			return mav;
		}
	}

	@RequestMapping(value = "workspace", method = RequestMethod.GET)
	public ModelAndView showWorkspace() {
		ModelAndView mav = new ModelAndView("page/bugtracker-workspace");
		mav.addObject("bugtrackerUrl", bugTrackerLocalService.getBugtrackerUrl().toString());
		return mav;
	}

	/* **************************************************************************************************************
	 * *
	 * ExecutionStep level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that execution step and the stub for the report form. Remember
	 * that the report bug dialog will be populated later.
	 * 
	 * @param stepId
	 * @return
	 */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}", method = RequestMethod.GET)
	public ModelAndView getExecStepIssuePanel(@PathVariable Long stepId, Locale locale,
			@RequestParam(value = "style", required = false, defaultValue = "toggle") String panelStyle) {

		ExecutionStep step = executionFinder.findExecutionStepById(stepId);
		return makeIssuePanel(step, EXECUTION_STEP_TYPE, locale, panelStyle);
	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getExecStepKnownIssuesData(@PathVariable("stepId") Long stepId,
			final DataTableDrawParameters params, final Locale locale) {

		FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> filteredCollection;
		CollectionSorting sorter = createCollectionSorting(params);

		try {
			
			filteredCollection = bugTrackerLocalService.findSortedIssueOwnerShipsForExecutionStep(stepId, sorter);
		}

		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyCollectionHolder(EXECUTION_STEP_TYPE, stepId, noCrdsException);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyCollectionHolder(EXECUTION_STEP_TYPE, stepId, npException);
		}

		return new StepIssuesTableModel().buildDataModel(filteredCollection, sorter.getFirstItemIndex() + 1,
				params.getsEcho());

	}

	/**
	 * will prepare a bug report for an execution step. The returned json infos will populate the form.
	 * 
	 * @param stepId
	 * @return
	 */

	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/new-issue")
	@ResponseBody
	public BTIssue getExecStepReportStub(@PathVariable Long stepId) {
		IssueDetector bugged = executionFinder.findExecutionStepById(stepId);

		return makeReportIssueModel(bugged);
	}

	/**
	 * gets the data of a new issue to be reported
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
			@RequestParam(value = "style", required = false, defaultValue = "toggle") String panelStyle) {

		Execution bugged = executionFinder.simpleGetExecutionById(execId);
		return makeIssuePanel(bugged, EXECUTION_TYPE, locale, panelStyle);
	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getExecKnownIssuesData(@PathVariable("execId") Long execId, final DataTableDrawParameters params,
			final Locale locale) {

		FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> filteredCollection;

		CollectionSorting sorter = createCollectionSorting(params);

		try {
			filteredCollection = bugTrackerLocalService.findSortedIssueOwnershipsforExecution(execId, sorter);

		}

		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyCollectionHolder(EXECUTION_TYPE, execId, noCrdsException);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyCollectionHolder(EXECUTION_TYPE, execId, npException);
		}

		return new ExecutionIssuesTableModel(messageSource, locale).buildDataModel(filteredCollection,
				sorter.getFirstItemIndex() + 1, params.getsEcho());

	}

	/**
	 * will prepare a bug report for an execution. The returned json infos will populate the form.
	 * 
	 * @param execId
	 * @return
	 */

	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/new-issue")
	@ResponseBody
	public BTIssue getExecReportStub(@PathVariable Long execId) {
		Execution execution = executionFinder.simpleGetExecutionById(execId);

		return makeReportIssueModel(execution);
	}

	/**
	 * gets the data of a new issue to be reported
	 * 
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/new-issue", method = RequestMethod.POST)
	@ResponseBody
	public Object postExecIssueReport(@PathVariable("execId") Long execId, @RequestBody BTIssue jsonIssue) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution-step " + execId);

		Execution entity = executionFinder.simpleGetExecutionById(execId);

		if (jsonIssue.hasBlankId()) {
			return processIssue(jsonIssue, entity);
		} else {
			return attachIssue(jsonIssue, entity);
		}
	}
	/* **************************************************************************************************************
	 * *
	 * TestCase  level section * *
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
			@RequestParam(value = "style", required = false, defaultValue = "toggle") String panelStyle) {

		TestCase testCase = testCaseFinder.findById(tcId);
		return makeIssuePanel(testCase, TEST_CASE_TYPE, locale, panelStyle);
	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = TEST_CASE_TYPE + "/{tcId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getTestCaseKnownIssuesData(@PathVariable("tcId") Long tcId,
			final DataTableDrawParameters params, final Locale locale) {

		FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> filteredCollection;
		CollectionSorting sorter = createCollectionSorting(params);

		try {
			filteredCollection = bugTrackerLocalService.findSortedIssueOwnershipForTestCase(tcId, sorter);
		}
		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyCollectionHolder(TEST_CASE_TYPE, tcId, noCrdsException);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyCollectionHolder(TEST_CASE_TYPE, tcId, npException);
		}
		return new TestCaseIssuesTableModel(messageSource, locale).buildDataModel(filteredCollection,
				sorter.getFirstItemIndex() + 1, params.getsEcho());

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
			@RequestParam(value = "style", required = false, defaultValue = "toggle") String panelStyle) {

		Iteration iteration = iterationFinder.findById(iterId);
		return makeIssuePanel(iteration, ITERATION_TYPE, locale, panelStyle);
	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = ITERATION_TYPE + "/{iterId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getIterationKnownIssuesData(@PathVariable("iterId") Long iterId,
			final DataTableDrawParameters params, final Locale locale) {

		FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> filteredCollection;
		CollectionSorting sorter = createCollectionSorting(params);

		try {
			filteredCollection = bugTrackerLocalService.findSortedIssueOwnershipForIteration(iterId, sorter);
		}
		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyCollectionHolder(ITERATION_TYPE, iterId, noCrdsException);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyCollectionHolder(ITERATION_TYPE, iterId, npException);
		}
		return new IterationIssuesTableModel(messageSource, locale).buildDataModel(filteredCollection,
				sorter.getFirstItemIndex() + 1, params.getsEcho());

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
			@RequestParam(value = "style", required = false, defaultValue = "toggle") String panelStyle) {

		Campaign campaign = campaignFinder.findById(campId);
		return makeIssuePanel(campaign, CAMPAIGN_TYPE, locale, panelStyle);
	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = CAMPAIGN_TYPE + "/{campId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getCampaignKnownIssuesData(@PathVariable("campId") Long campId,
			final DataTableDrawParameters params, final Locale locale) {

		FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> filteredCollection;
		CollectionSorting sorter = createCollectionSorting(params);

		try {
			filteredCollection = bugTrackerLocalService.findSortedIssueOwnershipsForCampaigns(campId, sorter);
		}
		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyCollectionHolder(CAMPAIGN_TYPE, campId, noCrdsException);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyCollectionHolder(CAMPAIGN_TYPE, campId, npException);
		}
		return new IterationIssuesTableModel(messageSource, locale).buildDataModel(filteredCollection,
				sorter.getFirstItemIndex() + 1, params.getsEcho());
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
			@RequestParam(value = "style", required = false, defaultValue = "toggle") String panelStyle) {

		TestSuite testSuite = testSuiteFinder.findById(testSuiteId);
		return makeIssuePanel(testSuite, TEST_SUITE_TYPE, locale, panelStyle);
	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = TEST_SUITE_TYPE + "/{testSuiteId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getTestSuiteKnownIssuesData(@PathVariable("testSuiteId") Long testSuiteId,
			final DataTableDrawParameters params, final Locale locale) {

		FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> filteredCollection;
		CollectionSorting sorter = createCollectionSorting(params);

		try {
			filteredCollection = bugTrackerLocalService.findSortedIssueOwnershipsForTestSuite(testSuiteId, sorter);
		}
		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyCollectionHolder(TEST_SUITE_TYPE, testSuiteId, noCrdsException);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyCollectionHolder(TEST_SUITE_TYPE, testSuiteId, npException);
		}
		return new IterationIssuesTableModel(messageSource, locale).buildDataModel(filteredCollection,
				sorter.getFirstItemIndex() + 1, params.getsEcho());
	}

	/* ************************* Generic code section ************************** */

	@RequestMapping(value = "/find-issue/{remoteKey}", method = RequestMethod.GET)
	@ResponseBody
	public BTIssue findIssue(@PathVariable("remoteKey") String remoteKey) {
		BTIssue remoteIssue = bugTrackerLocalService.getIssue(remoteKey);
		return remoteIssue;
	}

	@RequestMapping(value = "/credentials", method = RequestMethod.POST, params = { "login", "password" })
	public @ResponseBody
	Map<String, String> setCredendials(@RequestParam("login") String login, @RequestParam("password") String password) {

		bugTrackerLocalService.setCredentials(login, password);

		Map<String, String> map = new HashMap<String, String>();
		map.put("status", "ok");
		return map;

	}

	@RequestMapping(value = "/check", method = RequestMethod.GET)
	public @ResponseBody
	Object checkOperationReady() {
		return jsonStatus();
	}

	// FIXME : check first if a bugtracker is defined and if the credentials are set
	private Map<String, String> processIssue(BTIssue issue, IssueDetector entity) {

		final BTIssue postedIssue = bugTrackerLocalService.createIssue(entity, issue);
		final URL issueUrl = bugTrackerLocalService.getIssueUrl(postedIssue.getId());

		Map<String, String> result = new HashMap<String, String>();
		result.put("url", issueUrl.toString());
		result.put("issueId", postedIssue.getId());

		return result;
	}

	private Map<String, String> attachIssue(final BTIssue issue, IssueDetector entity) {

		bugTrackerLocalService.attachIssue(entity, issue.getId());
		final URL issueUrl = bugTrackerLocalService.getIssueUrl(issue.getId());

		Map<String, String> result = new HashMap<String, String>();
		result.put("url", issueUrl.toString());
		result.put("issueId", issue.getId());

		return result;
	}

	/* ********* generates a json model for an issue ******* */

	private BTIssue makeReportIssueModel(IssueDetector entity) {
		String projectName = entity.getProject().getName();
		final BTProject project = bugTrackerLocalService.findRemoteProject(projectName);

		String defaultDescription = entity.getDefaultDescription();

		BTIssue emptyIssue = new BTIssue();
		emptyIssue.setProject(project);

		return emptyIssue;
	}

	/*
	 * generates the ModelAndView for the bug section.
	 * 
	 * If the bugtracker isn'st defined no panel will be sent at all.
	 */
	private ModelAndView makeIssuePanel(Identified entity, String type, Locale locale, String panelStyle) {

		BugTrackerStatus status = checkStatus();

		if (status == BugTrackerStatus.BUGTRACKER_UNDEFINED) {
			return new ModelAndView("fragment/issues/bugtracker-panel-empty");
		} else {

			BugTrackerInterfaceDescriptor descriptor = bugTrackerLocalService.getInterfaceDescriptor();
			descriptor.setLocale(locale);

			ModelAndView mav = new ModelAndView("fragment/issues/bugtracker-panel");
			mav.addObject("entity", entity);
			mav.addObject("entityType", type);
			mav.addObject("interfaceDescriptor", descriptor);
			mav.addObject("panelStyle", panelStyle);
			mav.addObject("bugTrackerStatus", status);
			return mav;
		}
	}

	/* ******************************* private methods ********************************************** */

	private BugTrackerStatus checkStatus() {
		return bugTrackerLocalService.checkBugTrackerStatus();
	}

	private Object jsonStatus() {
		String strStatus = null;

		BugTrackerStatus status = checkStatus();

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

	private CollectionSorting createCollectionSorting(final DataTableDrawParameters params) {
		return new CollectionSorting() {
			@Override
			public int getMaxNumberOfItems() {
				return params.getiDisplayLength();
			}

			@Override
			public int getFirstItemIndex() {
				return params.getiDisplayStart();
			}

			@Override
			public String getSortingOrder() {
				return params.getsSortDir_0();
			}

			@Override
			public String getSortedAttribute() {
				return "Issue.id";
			}

			@Override
			public int getPageSize() {
				return getMaxNumberOfItems();
			}
		};
	}

	/* ****************************** bug ownership naming ****************************** */

	private static interface IssueOwnershipNameBuilder {
		void setMessageSource(MessageSource source);

		void setLocale(Locale locale);

		String buildName(IssueDetector bugged);
	}

	private static class ExecutionModelOwnershipNamebuilder implements IssueOwnershipNameBuilder {

		private Locale locale;
		private MessageSource messageSource;

		@Override
		public void setLocale(Locale locale) {
			this.locale = locale;
		}

		@Override
		public void setMessageSource(MessageSource source) {
			this.messageSource = source;
		}

		// FIXME : I'm too lazy to implement something serious for now.
		// The solution is probably to add adequate methods in the Bugged interface, so that we don't
		// have to rely on reflection here.
		@Override
		public String buildName(IssueDetector bugged) {
			String name = "this is clearly a bug";

			if (bugged instanceof ExecutionStep) {
				ExecutionStep step = ((ExecutionStep) bugged);
				name = buildStepName(step);
			} else if (bugged instanceof Execution) {
				name = "";
			}

			return name;
		}

		private String buildStepName(ExecutionStep bugged) {
			Integer index = bugged.getExecutionStepOrder() + 1;
			return messageSource.getMessage("squashtm.generic.hierarchy.execution.step.name", new Object[] { index },
					locale);
		}

	}

	private static class IterationModelOwnershipNamebuilder implements IssueOwnershipNameBuilder {

		private Locale locale;
		private MessageSource messageSource;

		@Override
		public void setLocale(Locale locale) {
			this.locale = locale;
		}

		@Override
		public void setMessageSource(MessageSource source) {
			this.messageSource = source;
		}

		// FIXME : I'm too lazy to implement something serious for now.
		// The solution is probably to add adequate methods in the Bugged interface, so that we don't
		// have to rely on reflection here.
		@Override
		public String buildName(IssueDetector bugged) {
			String name = "this is clearly a bug";

			if (bugged instanceof ExecutionStep) {
				ExecutionStep step = ((ExecutionStep) bugged);
				name = buildExecName(step.getExecution());
			} else if (bugged instanceof Execution) {
				Execution exec = ((Execution) bugged);
				name = buildExecName(exec);
			}

			return name;
		}

		// for a given execution we don't need to remind which one, so the name is ignored.
		private String buildExecName(Execution bugged) {
			String suiteName = findTestSuiteName(bugged);

			return messageSource.getMessage("squashtm.generic.hierarchy.execution.name",
					new Object[] { bugged.getName(), suiteName, bugged.getExecutionOrder() + 1 }, locale);
		}

	}
	
	private static class TestCaseModelOwnershipNamebuilder implements IssueOwnershipNameBuilder {


		private Locale locale;
		private MessageSource messageSource;

		@Override
		public void setLocale(Locale locale) {
			this.locale = locale;
		}

		@Override
		public void setMessageSource(MessageSource source) {
			this.messageSource = source;
		}

		// FIXME : I'm too lazy to implement something serious for now.
		// The solution is probably to add adequate methods in the Bugged interface, so that we don't
		// have to rely on reflection here.
		@Override
		public String buildName(IssueDetector bugged) {
			String name = "this is clearly a bug";

			if (bugged instanceof ExecutionStep) {
				ExecutionStep step = ((ExecutionStep) bugged);
				name = buildExecName(step.getExecution());
			} else if (bugged instanceof Execution) {
				Execution exec = ((Execution) bugged);
				name = buildExecName(exec);
			}

			return name;
		}

		// for a given execution we don't need to remind which one, so the name is ignored.
		private String buildExecName(Execution execution) {
			String iterationName = findIterationName(execution);			
			String suiteName = findTestSuiteName(execution);

			return messageSource.getMessage("squashtm.test-case.hierarchy.execution.name",
					new Object[] {iterationName, suiteName, execution.getExecutionOrder() + 1 }, locale);
		}

		
		}

	private static String findTestSuiteName(Execution execution) {
		TestSuite buggedSuite = execution.getTestPlan().getTestSuite();
		String suiteName = "";
		if(buggedSuite != null){
			suiteName = buggedSuite.getName();
		}
		return suiteName;
	}

	private static String findIterationName(Execution execution) {
		Iteration iteration = execution.getTestPlan().getIteration();
		String iterationName ="";
		if(iteration!= null){
			iterationName = iteration.getName();
		}
		return iterationName;
	}
	// **************************************** private utilities
	// *******************************************************

	private FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> makeEmptyCollectionHolder(String entityName,
			Long entityId, Exception cause) {
		LOGGER.trace("BugTrackerController : fetching known issues for  " + entityName + " " + entityId
				+ " failed, exception : ", cause);
		List<IssueOwnership<BTIssue>> emptyList = new LinkedList<IssueOwnership<BTIssue>>();
		return new FilteredCollectionHolder<List<IssueOwnership<BTIssue>>>(0, emptyList);
	}

	/**
	 * <p>
	 * the DataTableModel for an execution will hold the same informations than IterationIssuesTableModel (for now) :
	 * <ul>
	 * <li>the url of that issue,</li>
	 * <li>the id,</li>
	 * <li>the summary</li>,
	 * <li>the priority,</li>
	 * <li>the status,</li>
	 * <li>the assignee,</li>
	 * <li>the owning entity</li>
	 * </ul>
	 * </p>
	 */
	private class IterationIssuesTableModel extends DataTableModelHelper<IssueOwnership<BTIssue>> {

		private IssueOwnershipNameBuilder nameBuilder = new IterationModelOwnershipNamebuilder();

		public IterationIssuesTableModel(MessageSource source, Locale locale) {
			nameBuilder.setMessageSource(source);
			nameBuilder.setLocale(locale);
		}

		@Override
		public Object[] buildItemData(IssueOwnership<BTIssue> ownership) {
			return new Object[] { bugTrackerLocalService.getIssueUrl(ownership.getIssue().getId()).toExternalForm(),
					ownership.getIssue().getId(), ownership.getIssue().getSummary(),
					ownership.getIssue().getPriority().getName(), ownership.getIssue().getStatus().getName(),
					ownership.getIssue().getAssignee().getName(), nameBuilder.buildName(ownership.getOwner()) };
		}
	}
	/**
	 * <p>
	 * the DataTableModel for a TestCase will hold following informations :
	 * <ul>
	 * <li>the url of that issue,</li>
	 * <li>the id,</li>
	 * <li>the summary</li>,
	 * <li>the priority,</li>
	 * <li>the status,</li>
	 * <li>the assignee,</li>
	 * <li>the iteration name</li>
	 * </ul>
	 * </p>
	 */
	private class TestCaseIssuesTableModel extends DataTableModelHelper<IssueOwnership<BTIssue>> {

		private IssueOwnershipNameBuilder nameBuilder = new TestCaseModelOwnershipNamebuilder();

		public TestCaseIssuesTableModel(MessageSource source, Locale locale) {
			nameBuilder.setMessageSource(source);
			nameBuilder.setLocale(locale);
		}

		@Override
		public Object[] buildItemData(IssueOwnership<BTIssue> ownership) {
			return new Object[] { bugTrackerLocalService.getIssueUrl(ownership.getIssue().getId()).toExternalForm(),
					ownership.getIssue().getId(), ownership.getIssue().getSummary(),
					ownership.getIssue().getPriority().getName(), ownership.getIssue().getStatus().getName(),
					ownership.getIssue().getAssignee().getName(), nameBuilder.buildName(ownership.getOwner()) };
		}
	}
	/**
	 * <p>
	 * the DataTableModel for an execution will hold the same informations than IterationIssuesTableModel (for now) :
	 * <ul>
	 * <li>the url of that issue,</li>
	 * <li>the id,</li>
	 * <li>the summary</li>,
	 * <li>the priority,</li>
	 * <li>the status,</li>
	 * <li>the assignee,</li>
	 * <li>the owning entity</li>
	 * </ul>
	 * </p>
	 */
	private class ExecutionIssuesTableModel extends DataTableModelHelper<IssueOwnership<BTIssue>> {

		private IssueOwnershipNameBuilder nameBuilder = new ExecutionModelOwnershipNamebuilder();

		public ExecutionIssuesTableModel(MessageSource source, Locale locale) {
			nameBuilder.setMessageSource(source);
			nameBuilder.setLocale(locale);
		}

		@Override
		public Object[] buildItemData(IssueOwnership<BTIssue> ownership) {
			BTIssue issue = ownership.getIssue();
			
			return new Object[] { bugTrackerLocalService.getIssueUrl(issue.getId()).toExternalForm(),
					issue.getId(), issue.getSummary(),
					issue.getPriority().getName(), issue.getStatus().getName(),
					issue.getAssignee().getName(), nameBuilder.buildName(ownership.getOwner()) };
		}
	}

	/**
	 * <p>
	 * the DataTableModel will hold :
	 * <ul>
	 * <li>the url of that issue,</li>
	 * <li>the id,</li>
	 * <li>the summary,</li>
	 * <li>the priority</li>
	 * </ul>
	 * </p>
	 */
	private class StepIssuesTableModel extends DataTableModelHelper<IssueOwnership<BTIssue>> {

		@Override
		public Object[] buildItemData(IssueOwnership<BTIssue> ownership) {
			return new Object[] { bugTrackerLocalService.getIssueUrl(ownership.getIssue().getId()).toExternalForm(),
					ownership.getIssue().getId(), ownership.getIssue().getSummary(),
					ownership.getIssue().getPriority().getName() };
		}
	}
	/*
	 * @RequestMapping(value = EXECUTION_TYPE + "/{execId}/debug", method = RequestMethod.GET) public ModelAndView
	 * getExecIssuePanelDebug(@PathVariable Long execId, Locale locale,
	 * 
	 * @RequestParam(value = "style", required = false, defaultValue = "toggle") String panelStyle) {
	 * 
	 * Bugged bugged = bugTrackerLocalService.findBuggedEntity(execId, Execution.class); return
	 * makeIssuePanelDebug(bugged, EXECUTION_TYPE, locale, panelStyle); }
	 * 
	 * private ModelAndView makeIssuePanelDebug(Bugged entity, String type, Locale locale, String panelStyle) {
	 * 
	 * BugTrackerStatus status = checkStatus();
	 * 
	 * if (status == BugTrackerStatus.BUGTRACKER_UNDEFINED) { return new
	 * ModelAndView("fragment/issues/bugtracker-panel-empty"); } else {
	 * 
	 * BugTrackerInterfaceDescriptor descriptor = bugTrackerLocalService.getInterfaceDescriptor();
	 * descriptor.setLocale(locale);
	 * 
	 * ModelAndView mav = new ModelAndView("fragment/issues/bugtracker-panel-debug"); mav.addObject("entity", entity);
	 * mav.addObject("entityType", type); mav.addObject("interfaceDescriptor", descriptor); mav.addObject("panelStyle",
	 * panelStyle); mav.addObject("bugTrackerStatus", status); return mav; } }
	 */

}

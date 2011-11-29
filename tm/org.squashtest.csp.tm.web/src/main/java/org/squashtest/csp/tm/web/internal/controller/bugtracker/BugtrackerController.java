/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.Category;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.domain.User;
import org.squashtest.csp.core.bugtracker.domain.Version;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.csp.core.web.utils.HTMLCleanupUtils;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.csp.tm.domain.bugtracker.Bugged;
import org.squashtest.csp.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.BugTrackerLocalService;
import org.squashtest.csp.tm.web.internal.model.customeditors.CategoryPropertyEditorSupport;
import org.squashtest.csp.tm.web.internal.model.customeditors.PriorityPropertyEditorSupport;
import org.squashtest.csp.tm.web.internal.model.customeditors.ProjectPropertyEditorSupport;
import org.squashtest.csp.tm.web.internal.model.customeditors.UserPropertyEditorSupport;
import org.squashtest.csp.tm.web.internal.model.customeditors.VersionPropertyEditorSupport;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.jquery.IssueModel;
import org.squashtest.csp.tm.web.internal.model.jquery.JsonSimpleData;

@Controller
@RequestMapping("/bugtracker")
public class BugtrackerController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BugtrackerController.class);

	private BugTrackerLocalService bugTrackerLocalService;

	private static final String EXECUTION_STEP_TYPE = "execution-step";
	private static final String EXECUTION_TYPE = "execution";

	@Inject
	private MessageSource messageSource;

	@ServiceReference
	public void setBugTrackerLocalService(BugTrackerLocalService bugTrackerLocalService) {
		if (bugTrackerLocalService == null) {
			throw new IllegalArgumentException("BugTrackerController : no service provided");
		}
		this.bugTrackerLocalService = bugTrackerLocalService;
	}

	@InitBinder
	public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {

		binder.registerCustomEditor(User.class, new UserPropertyEditorSupport());
		binder.registerCustomEditor(Priority.class, new PriorityPropertyEditorSupport());
		binder.registerCustomEditor(Version.class, new VersionPropertyEditorSupport());
		binder.registerCustomEditor(Category.class, new CategoryPropertyEditorSupport());
		binder.registerCustomEditor(BTProject.class, new ProjectPropertyEditorSupport());
	}

	/* ********************* Execution Step level section *************************** */

	/**
	 * returns the panel displaying the current bugs of that execution step and the stub for the report form. Remember
	 * that the report bug dialog will be populated later.
	 *
	 * @param stepId
	 * @return
	 */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}", method = RequestMethod.GET)
	public ModelAndView getExecStepIssuePanel(@PathVariable Long stepId, Locale locale) {

		Bugged bugged = bugTrackerLocalService.findBuggedEntity(stepId, ExecutionStep.class);
		return makeIssuePanel(bugged, EXECUTION_STEP_TYPE, locale);
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
			Bugged bugged = bugTrackerLocalService.findBuggedEntity(stepId, ExecutionStep.class);

			filteredCollection = bugTrackerLocalService.findBugTrackerIssues(bugged, sorter);
		}
		// we only filter here the exception happening when the credentials aren't set. Every other Exceptions will be
		// handled via the normal way
		// (see the various implementations of AbstractHandlerExceptionResolver in
		// org.squashtest.csp.tm.web.internal.handler)
		catch (BugTrackerNoCredentialsException noCrdsException) {
			LOGGER.trace("BugTrackerController : fetching known issues for execution step " + stepId
					+ " failed, exception : ", noCrdsException);
			List<IssueOwnership<BTIssue>> emptyList = new LinkedList<IssueOwnership<BTIssue>>();
			filteredCollection = new FilteredCollectionHolder<List<IssueOwnership<BTIssue>>>(0, emptyList);
		} catch (NullArgumentException npException) {
			LOGGER.trace("BugTrackerController : fetching known issues for execution step " + stepId
					+ " failed, exception : ", npException);
			List<IssueOwnership<BTIssue>> emptyList = new LinkedList<IssueOwnership<BTIssue>>();
			filteredCollection = new FilteredCollectionHolder<List<IssueOwnership<BTIssue>>>(0, emptyList);
		}

		/*
		 * the DataTableModel will hold :
		 *
		 * - the url of that issue, - the id, - the summary, - the priority
		 */
		return new DataTableModelHelper<IssueOwnership<BTIssue>>() {
			@Override
			public Object[] buildItemData(IssueOwnership<BTIssue> ownership) {
				return new Object[] {
						bugTrackerLocalService.getIssueUrl(ownership.getIssue().getId()).toExternalForm(),
						ownership.getIssue().getId(), ownership.getIssue().getSummary(),
						ownership.getIssue().getPriority().getName() };
			}
		}.buildDataModel(filteredCollection, sorter.getFirstItemIndex() + 1, params.getsEcho());

	}

	/**
	 * will prepare a bug report for an execution step. The returned json infos will populate the form.
	 *
	 * @param stepId
	 * @return
	 */

	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/bug-report")
	@ResponseBody
	public IssueModel getExecStepReportStub(@PathVariable Long stepId) {
		Bugged bugged = bugTrackerLocalService.findBuggedEntity(stepId, ExecutionStep.class);

		return makeIssueModel(bugged);
	}

	/**
	 * gets the data of a new issue to be reported
	 *
	 */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/bug-report", method = RequestMethod.POST)
	@ResponseBody
	public String postExecStepIssueReport(@PathVariable("stepId") Long stepId, @ModelAttribute BTIssue jsonIssue) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution-step " + stepId);

		Bugged entity = bugTrackerLocalService.findBuggedEntity(stepId, ExecutionStep.class);

		return processIssue(jsonIssue, entity);
	}

	/* ************************* Execution level section ********************** */

	/**
	 * returns the panel displaying the current bugs of that execution and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 *
	 * @param stepId
	 * @return
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}", method = RequestMethod.GET)
	public ModelAndView getExecIssuePanel(@PathVariable Long execId, Locale locale) {

		Bugged bugged = bugTrackerLocalService.findBuggedEntity(execId, Execution.class);
		return makeIssuePanel(bugged, EXECUTION_TYPE, locale);
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
			Bugged bugged = bugTrackerLocalService.findBuggedEntity(execId, Execution.class);

			filteredCollection = bugTrackerLocalService.findBugTrackerIssues(bugged, sorter);

		}
		// we only filter here the exception happening when the credentials aren't set. Every other Exceptions will be
		// handled via the normal way
		// (see the various implementations of AbstractHandlerExceptionResolver in
		// org.squashtest.csp.tm.web.internal.handler)
		catch (BugTrackerNoCredentialsException noCrdsException) {
			LOGGER.trace("BugTrackerController : fetching known issues for execution " + execId
					+ " failed, exception : ", noCrdsException);
			List<IssueOwnership<BTIssue>> emptyList = new LinkedList<IssueOwnership<BTIssue>>();
			filteredCollection = new FilteredCollectionHolder<List<IssueOwnership<BTIssue>>>(0, emptyList);
		} catch (NullArgumentException npException) {
			LOGGER.trace("BugTrackerController : fetching known issues for execution " + execId
					+ " failed, exception : ", npException);
			List<IssueOwnership<BTIssue>> emptyList = new LinkedList<IssueOwnership<BTIssue>>();
			filteredCollection = new FilteredCollectionHolder<List<IssueOwnership<BTIssue>>>(0, emptyList);
		}
		/*
		 * the DataTableModel for an execution will hold :
		 *
		 * - the url of that issue, - the id, - the summary, - the priority, - the status, - the assignee, - the owning
		 * entity
		 */
		return new DataTableModelHelper<IssueOwnership<BTIssue>>() {
			@Override
			public Object[] buildItemData(IssueOwnership<BTIssue> ownership) {
				return new Object[] {
						bugTrackerLocalService.getIssueUrl(ownership.getIssue().getId()).toExternalForm(),
						ownership.getIssue().getId(), ownership.getIssue().getSummary(),
						ownership.getIssue().getPriority().getName(), ownership.getIssue().getStatus().getName(),
						ownership.getIssue().getAssignee().getName(), buildOwnerName(ownership.getOwner(), locale) };
			}
		}.buildDataModel(filteredCollection, sorter.getFirstItemIndex() + 1, params.getsEcho());

	}

	/**
	 * will prepare a bug report for an execution step. The returned json infos will populate the form.
	 *
	 * @param stepId
	 * @return
	 */

	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/bug-report")
	@ResponseBody
	public IssueModel getExecReportStub(@PathVariable Long execId) {
		Bugged bugged = bugTrackerLocalService.findBuggedEntity(execId, Execution.class);

		return makeIssueModel(bugged);
	}

	/**
	 * gets the data of a new issue to be reported
	 *
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/bug-report", method = RequestMethod.POST)
	@ResponseBody
	public String postExecIssueReport(@PathVariable("execId") Long execId, @ModelAttribute BTIssue jsonIssue) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution-step " + execId);

		Bugged entity = bugTrackerLocalService.findBuggedEntity(execId, Execution.class);

		return processIssue(jsonIssue, entity);
	}

	/* ************************* Generic code section ************************** */

	@RequestMapping(value = "/credentials", method = RequestMethod.POST, params = { "login", "password" })
	public @ResponseBody
	String setCredendials(@RequestParam("login") String login, @RequestParam("password") String password) {

		bugTrackerLocalService.setCredentials(login, password);

		return new JsonSimpleData().addAttr("status", "ok").toString();
	}

	@RequestMapping(value = "/check", method = RequestMethod.GET)
	public @ResponseBody
	String checkOperationReady() {
		return jsonStatus();
	}

	// FIXME : check first if a bugtracker is defined and if the credentials are set
	private String processIssue(BTIssue issue, Bugged entity) {

		BTIssue cleanIssue = cleanHtmlFromIssue(issue);

		BTIssue postedIssue = bugTrackerLocalService.createIssue(entity, cleanIssue);
		URL issueUrl = bugTrackerLocalService.getIssueUrl(postedIssue.getId());

		return new JsonSimpleData().addAttr("url", issueUrl.toString()).addAttr("issueId", postedIssue.getId())
				.toString();
	}

	/* ********* generates a json model for an issue ******* */
	private IssueModel makeIssueModel(Bugged entity) {
		List<Priority> priorities = bugTrackerLocalService.getRemotePriorities();

		String projectName = entity.getProject().getName();
		BTProject project = bugTrackerLocalService.findRemoteProject(projectName);

		String defaultDescription = entity.getDefaultDescription();

		IssueModel model = new IssueModel();
		model.setPriorities(priorities.toArray());
		model.setUsers(project.getUsers().toArray());
		model.setVersions(project.getVersions().toArray());
		model.setCategories(project.getCategories().toArray());
		model.setDefaultDescription(defaultDescription);
		model.setProjectId(project.getId());

		return model;
	}

	/*
	 * generates the ModelAndView for the bug section.
	 *
	 * If the bugtracker isn'st defined no panel will be sent at all.
	 */
	private ModelAndView makeIssuePanel(Bugged entity, String type, Locale locale) {

		BugTrackerStatus status = checkStatus();

		if (status == BugTrackerStatus.BUGTRACKER_UNDEFINED) {
			return null;
		} else {

			BugTrackerInterfaceDescriptor descriptor = bugTrackerLocalService.getInterfaceDescriptor();
			descriptor.setLocale(locale);

			ModelAndView mav = new ModelAndView("fragment/issues/bugtracker-panel");
			mav.addObject("entity", entity);
			mav.addObject("entityType", type);
			mav.addObject("interfaceDescriptor", descriptor);
			mav.addObject("projectName", entity.getProject().getName());
			mav.addObject("bugTrackerStatus", status);
			return mav;
		}
	}

	/* ******************************* private methods ********************************************** */

	private BTIssue cleanHtmlFromIssue(BTIssue issue) {

		String dirtyComment = issue.getComment();
		String cleanComment = HTMLCleanupUtils.htmlToText(dirtyComment);
		issue.setComment(cleanComment);

		String dirtyDescription = issue.getDescription();
		String cleanDescription = HTMLCleanupUtils.htmlToText(dirtyDescription);
		issue.setDescription(cleanDescription);

		String dirtySummary = issue.getSummary();
		String cleanSummary = HTMLCleanupUtils.htmlToText(dirtySummary);
		issue.setSummary(cleanSummary);

		return issue;

	}

	private BugTrackerStatus checkStatus() {
		return bugTrackerLocalService.checkBugTrackerStatus();
	}

	private String jsonStatus() {
		JsonSimpleData jsonStatus = new JsonSimpleData();

		BugTrackerStatus status = checkStatus();

		if (status == BugTrackerStatus.BUGTRACKER_READY) {
			jsonStatus.addAttr("status", "ready");
		} else if (status == BugTrackerStatus.BUGTRACKER_NEEDS_CREDENTIALS) {
			jsonStatus.addAttr("status", "needs_credentials");
		} else {
			jsonStatus.addAttr("status", "bt_undefined");
		}

		return jsonStatus.toString();
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

	// FIXME : I'm too lazy to implement something serious for now.
	// The solution is probably to add adequate methods in the Bugged interface, so that we don't
	// have to rely on reflection here.
	private String buildOwnerName(Bugged bugged, Locale locale) {
		String name = "this is clearly a bug";

		if (bugged instanceof ExecutionStep) {
			Integer index = ((ExecutionStep) bugged).getExecutionStepOrder() + 1;
			name = messageSource.getMessage("squashtm.generic.execstep.label", null, locale) + " #" + index.toString();
		} else if (bugged instanceof Execution) {
			name = ((Execution) bugged).getName();
		}

		return name;
	}

	/* ******************************** debug code section **************************** */

	/* debug for an execution step */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/debug", method = RequestMethod.GET)
	public ModelAndView getExecStepIssuePanelDebug(@PathVariable Long stepId, Locale locale) {
		Bugged bugged = bugTrackerLocalService.findBuggedEntity(stepId, ExecutionStep.class);

		BugTrackerInterfaceDescriptor descriptor = bugTrackerLocalService.getInterfaceDescriptor();
		descriptor.setLocale(locale);

		ModelAndView mav = new ModelAndView("fragment/issues/bugtracker-panel-debug");
		mav.addObject("entity", bugged);
		mav.addObject("entityType", "execution-step");
		mav.addObject("interfaceDescriptor", descriptor);
		mav.addObject("projectName", bugged.getProject().getName());
		mav.addObject("bugTrackerStatus", checkStatus());
		return mav;
	}

	/* debug for an execution */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/debug", method = RequestMethod.GET)
	public ModelAndView getExecIssuePanelDebug(@PathVariable Long execId, Locale locale) {
		Bugged bugged = bugTrackerLocalService.findBuggedEntity(execId, Execution.class);

		BugTrackerInterfaceDescriptor descriptor = bugTrackerLocalService.getInterfaceDescriptor();
		descriptor.setLocale(locale);

		ModelAndView mav = new ModelAndView("fragment/issues/bugtracker-panel-debug");
		mav.addObject("entity", bugged);
		mav.addObject("entityType", "execution");
		mav.addObject("interfaceDescriptor", descriptor);
		mav.addObject("projectName", bugged.getProject().getName());
		mav.addObject("bugTrackerStatus", checkStatus());
		return mav;
	}

}

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
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.csp.tm.domain.bugtracker.Bugged;
import org.squashtest.csp.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.BugTrackerLocalService;
import org.squashtest.csp.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.csp.tm.web.internal.model.customeditors.CategoryPropertyEditorSupport;
import org.squashtest.csp.tm.web.internal.model.customeditors.PriorityPropertyEditorSupport;
import org.squashtest.csp.tm.web.internal.model.customeditors.ProjectPropertyEditorSupport;
import org.squashtest.csp.tm.web.internal.model.customeditors.UserPropertyEditorSupport;
import org.squashtest.csp.tm.web.internal.model.customeditors.VersionPropertyEditorSupport;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.jquery.IssueModel;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode;

@Controller
@RequestMapping("/bugtracker")
public class BugtrackerController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BugtrackerController.class);

	private BugTrackerLocalService bugTrackerLocalService;

	private static final String EXECUTION_STEP_TYPE = "execution-step";
	private static final String EXECUTION_TYPE = "execution";
	private static final String ITERATION_TYPE = "iteration";
	

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
	/* **************************************************************************************************************
	 * 																												*
	 *  								Navigation button           												*
	 *  																											*
	 *  *********************************************************************************************************** */
	@RequestMapping(value = "workspace-button", method = RequestMethod.GET)
	public ModelAndView getNavButton(Locale locale) {
		BugTrackerStatus status = checkStatus();

		if (status == BugTrackerStatus.BUGTRACKER_UNDEFINED) {
			LOGGER.trace("no bugtracker");
			return new ModelAndView("fragment/issues/bugtracker-panel-empty");			
		} else {
			LOGGER.trace("return bugtracker nav button");
			ModelAndView mav =  new ModelAndView("fragment/issues/bugtracker-nav-button");
			mav.addObject("bugtrackerUrl", bugTrackerLocalService.getBugtrackerUrl().toString());
			mav.addObject("iframeFriendly", bugTrackerLocalService.getBugtrackerIframeFriendly());
			return mav;
		}
	}
	
	@RequestMapping(value = "workspace",method = RequestMethod.GET)
	public ModelAndView showWorkspace() {
		ModelAndView mav = new ModelAndView("page/bugtracker-workspace");
		mav.addObject("bugtrackerUrl", bugTrackerLocalService.getBugtrackerUrl().toString());
		return mav;
	}
	/* **************************************************************************************************************
	 * 																												*
	 *  								ExecutionStep level section 												*
	 *  																											*
	 *  *********************************************************************************************************** */

	/**
	 * returns the panel displaying the current bugs of that execution step and the stub for the report form. Remember
	 * that the report bug dialog will be populated later.
	 *
	 * @param stepId
	 * @return
	 */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}", method = RequestMethod.GET)
	public ModelAndView getExecStepIssuePanel(@PathVariable Long stepId, Locale locale, 
			@RequestParam(value="style", required=false, defaultValue="toggle") String panelStyle) {

		Bugged bugged = bugTrackerLocalService.findBuggedEntity(stepId, ExecutionStep.class);
		return makeIssuePanel(bugged, EXECUTION_STEP_TYPE, locale, panelStyle);
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
		
		
		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyCollectionHolder(EXECUTION_STEP_TYPE, stepId, noCrdsException);
		} 
		catch (NullArgumentException npException) {
			filteredCollection = makeEmptyCollectionHolder(EXECUTION_STEP_TYPE, stepId, npException);
		}

		return new StepIssuesTableModel().buildDataModel(filteredCollection, sorter.getFirstItemIndex() + 1, params.getsEcho());

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
	public Object postExecStepIssueReport(@PathVariable("stepId") Long stepId, @ModelAttribute BTIssue jsonIssue) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution-step " + stepId);

		Bugged entity = bugTrackerLocalService.findBuggedEntity(stepId, ExecutionStep.class);

		return processIssue(jsonIssue, entity);
	}

	/* **************************************************************************************************************
	 * 																												*
	 *  								Execution level section 													*
	 *  																											*
	 *  *********************************************************************************************************** */

	/**
	 * returns the panel displaying the current bugs of that execution and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 *
	 * @param stepId
	 * @return
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}", method = RequestMethod.GET)
	public ModelAndView getExecIssuePanel(@PathVariable Long execId, Locale locale, 
			@RequestParam(value="style", required=false, defaultValue="toggle") String panelStyle) {

		Bugged bugged = bugTrackerLocalService.findBuggedEntity(execId, Execution.class);
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
			Bugged bugged = bugTrackerLocalService.findBuggedEntity(execId, Execution.class);

			filteredCollection = bugTrackerLocalService.findBugTrackerIssues(bugged, sorter);

		}

		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyCollectionHolder(EXECUTION_TYPE, execId, noCrdsException);
		} 
		catch (NullArgumentException npException) {
			filteredCollection = makeEmptyCollectionHolder(EXECUTION_TYPE, execId, npException);
		}

		return new ExecutionIssuesTableModel(messageSource,locale)
					.buildDataModel(filteredCollection, sorter.getFirstItemIndex() + 1, params.getsEcho());

	}

	/**
	 * will prepare a bug report for an execution. The returned json infos will populate the form.
	 *
	 * @param execId
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
	public Object postExecIssueReport(@PathVariable("execId") Long execId, @ModelAttribute BTIssue jsonIssue) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution-step " + execId);

		Bugged entity = bugTrackerLocalService.findBuggedEntity(execId, Execution.class);

		return processIssue(jsonIssue, entity);
	}
	
	
	/* **************************************************************************************************************
	 * 																												*
	 *  								Iteration level section 													*
	 *  																											*
	 *  *********************************************************************************************************** */	
	
	

	/**
	 * returns the panel displaying the current bugs of that execution and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 *
	 * @param stepId
	 * @return
	 */
	@RequestMapping(value = ITERATION_TYPE + "/{iterId}", method = RequestMethod.GET)
	public ModelAndView getIterationIssuePanel(@PathVariable Long iterId, Locale locale, 
			@RequestParam(value="style", required=false, defaultValue="toggle") String panelStyle) {

		Bugged bugged = bugTrackerLocalService.findBuggedEntity(iterId, Iteration.class);
		return makeIssuePanel(bugged, ITERATION_TYPE, locale, panelStyle);
	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value =ITERATION_TYPE + "/{iterId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getIterationKnownIssuesData(@PathVariable("iterId") Long iterId, final DataTableDrawParameters params,
			final Locale locale) {

		FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> filteredCollection;

		CollectionSorting sorter = createCollectionSorting(params);

		try {
			Bugged bugged = bugTrackerLocalService.findBuggedEntity(iterId, Iteration.class);

			filteredCollection = bugTrackerLocalService.findBugTrackerIssues(bugged, sorter);

		}

		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyCollectionHolder(ITERATION_TYPE, iterId, noCrdsException);
		} 
		catch (NullArgumentException npException) {
			filteredCollection = makeEmptyCollectionHolder(ITERATION_TYPE, iterId, npException);
		}

		return new IterationIssuesTableModel(messageSource, locale)
					.buildDataModel(filteredCollection, sorter.getFirstItemIndex() + 1, params.getsEcho());

	}


	

	/* ************************* Generic code section ************************** */

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
	private Map<String, String> processIssue(BTIssue issue, Bugged entity) {

		final BTIssue postedIssue = bugTrackerLocalService.createIssue(entity, issue);
		final URL issueUrl = bugTrackerLocalService.getIssueUrl(postedIssue.getId());
		
		Map<String, String> result = new HashMap<String, String>();
		result.put("url", issueUrl.toString());
		result.put("issueId", postedIssue.getId());

		return result;
	}

	
	
	/* ********* generates a json model for an issue ******* */
	
	private IssueModel makeIssueModel(Bugged entity) {
		List<Priority> priorities = bugTrackerLocalService.getRemotePriorities();

		String projectName = entity.getProject().getName();
		final BTProject project = bugTrackerLocalService.findRemoteProject(projectName);

		String defaultDescription = entity.getDefaultDescription();

		IssueModel model = new IssueModel();
		model.setPriorities(priorities.toArray());
		model.setUsers(project.getUsers().toArray());
		model.setVersions(project.getVersions().toArray());
		model.setCategories(project.getCategories().toArray());
		model.setDefaultDescription(defaultDescription);
		model.setProject(new IssueModel.ProjectModel(project.getId(), project.getName()));

		return model;
	}

	/*
	 * generates the ModelAndView for the bug section.
	 *
	 * If the bugtracker isn'st defined no panel will be sent at all.
	 */
	private ModelAndView makeIssuePanel(Bugged entity, String type, Locale locale, String panelStyle) {

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
		String strStatus=null;

		BugTrackerStatus status = checkStatus();

		if (status == BugTrackerStatus.BUGTRACKER_READY) {
			strStatus = "ready";
		} else if (status == BugTrackerStatus.BUGTRACKER_NEEDS_CREDENTIALS) {
			strStatus ="needs_credentials";
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
	
	
	/* ****************************** bug ownership naming *******************************/ 
	
	
	private static interface IssueOwnershipNameBuilder{
		void setMessageSource(MessageSource source);
		void setLocale(Locale locale);
		String buildName(Bugged bugged);
	}

	
	private static class ExecutionModelOwnershipNamebuilder implements IssueOwnershipNameBuilder{
		
		private Locale locale;
		private MessageSource messageSource;
		
		@Override
		public void setLocale(Locale locale){
			this.locale=locale;
		}
		
		@Override
		public void setMessageSource(MessageSource source){
			this.messageSource=source;
		}
	
		// FIXME : I'm too lazy to implement something serious for now.
		// The solution is probably to add adequate methods in the Bugged interface, so that we don't
		// have to rely on reflection here.
		@Override
		public String buildName(Bugged bugged) {
			String name = "this is clearly a bug";
	
			if (bugged instanceof ExecutionStep) {
				ExecutionStep step = ((ExecutionStep) bugged);
				name = buildStepName(step);
			} 
			else if (bugged instanceof Execution) {
				name = "";
			}
	
			return name;
		}
		
		
		private String buildStepName(ExecutionStep bugged){
			Integer index = bugged.getExecutionStepOrder() + 1;
			return messageSource.getMessage("squashtm.generic.hierarchy.execution.step.name", 
					new Object[]{index}, 
					locale) ;
		}

		
	}


	private static class IterationModelOwnershipNamebuilder implements IssueOwnershipNameBuilder{
		
		private Locale locale;
		private MessageSource messageSource;
		
		@Override
		public void setLocale(Locale locale){
			this.locale=locale;
		}
		
		@Override
		public void setMessageSource(MessageSource source){
			this.messageSource=source;
		}
	
		// FIXME : I'm too lazy to implement something serious for now.
		// The solution is probably to add adequate methods in the Bugged interface, so that we don't
		// have to rely on reflection here.
		@Override
		public String buildName(Bugged bugged) {
			String name = "this is clearly a bug";
	
			if (bugged instanceof ExecutionStep) {
				ExecutionStep step = ((ExecutionStep) bugged);
				name = buildExecName(step.getExecution());
			} 
			else if (bugged instanceof Execution) {
				Execution exec = ((Execution) bugged);
				name = buildExecName(exec);
			}
	
			return name;
		}
		
		//for a given execution we don't need to remind which one, so the name is ignored.
		private String buildExecName(Execution bugged){
			return messageSource.getMessage("squashtm.generic.hierarchy.execution.name", 
								new Object[]{
									bugged.getName(), 
									bugged.getExecutionOrder()+1
								},
								locale);
		}
		
		
	}	
	
	
	// **************************************** private utilities *******************************************************
	
	
	private FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> makeEmptyCollectionHolder(String entityName, Long entityId, Exception cause){
		LOGGER.trace("BugTrackerController : fetching known issues for  " +entityName+" "+ entityId
				+ " failed, exception : ", cause);
		List<IssueOwnership<BTIssue>> emptyList = new LinkedList<IssueOwnership<BTIssue>>();
		return new FilteredCollectionHolder<List<IssueOwnership<BTIssue>>>(0, emptyList);
	}
	
	
	/**
	 * <p>the DataTableModel for an execution will hold the same informations than IterationIssuesTableModel (for now) :
	 *<ul>
	 *	<li>the url of that issue,</li>
	 *	<li>the id,</li>
	 *	<li>the summary</li>,
	 *	<li>the priority,</li>
	 *	<li>the status,</li>
	 *	<li>the assignee,</li>
	 *	<li>the owning entity</li>
	 *</ul>
	 * </p>
	 */
	private class IterationIssuesTableModel extends DataTableModelHelper<IssueOwnership<BTIssue>>{
		
		private IssueOwnershipNameBuilder nameBuilder = new IterationModelOwnershipNamebuilder();
		
		public IterationIssuesTableModel(MessageSource source, Locale locale){
			nameBuilder.setMessageSource(source);
			nameBuilder.setLocale(locale);
		}
		
		@Override
		public Object[] buildItemData(IssueOwnership<BTIssue> ownership) {
			return new Object[] {
					bugTrackerLocalService.getIssueUrl(ownership.getIssue().getId()).toExternalForm(),
					ownership.getIssue().getId(), ownership.getIssue().getSummary(),
					ownership.getIssue().getPriority().getName(), ownership.getIssue().getStatus().getName(),
					ownership.getIssue().getAssignee().getName(), nameBuilder.buildName(ownership.getOwner()) };
		}	
	}
	
	
	/**
	 * <p>the DataTableModel for an execution will hold the same informations than IterationIssuesTableModel (for now) :
	 *<ul>
	 *	<li>the url of that issue,</li>
	 *	<li>the id,</li>
	 *	<li>the summary</li>,
	 *	<li>the priority,</li>
	 *	<li>the status,</li>
	 *	<li>the assignee,</li>
	 *	<li>the owning entity</li>
	 *</ul>
	 * </p>
	 */
	private class ExecutionIssuesTableModel extends DataTableModelHelper<IssueOwnership<BTIssue>>{
		
		private IssueOwnershipNameBuilder nameBuilder = new ExecutionModelOwnershipNamebuilder();
		
		public ExecutionIssuesTableModel(MessageSource source, Locale locale){
			nameBuilder.setMessageSource(source);
			nameBuilder.setLocale(locale);
		}
		
		@Override
		public Object[] buildItemData(IssueOwnership<BTIssue> ownership) {
			return new Object[] {
					bugTrackerLocalService.getIssueUrl(ownership.getIssue().getId()).toExternalForm(),
					ownership.getIssue().getId(), ownership.getIssue().getSummary(),
					ownership.getIssue().getPriority().getName(), ownership.getIssue().getStatus().getName(),
					ownership.getIssue().getAssignee().getName(), nameBuilder.buildName(ownership.getOwner()) };
		}		
	}
	

	/**
	 * <p>the DataTableModel will hold : 
	 *	<ul>
	 * 		<li>the url of that issue,</li>
	 * 		<li>the id,</li>
	 * 		<li>the summary,</li>
	 * 		<li>the priority</li>
	 * 	</ul>
	 * </p>
	 */
	private class StepIssuesTableModel extends DataTableModelHelper<IssueOwnership<BTIssue>>{

		@Override
		public Object[] buildItemData(IssueOwnership<BTIssue> ownership) {
			return new Object[] {
					bugTrackerLocalService.getIssueUrl(ownership.getIssue().getId()).toExternalForm(),
					ownership.getIssue().getId(), ownership.getIssue().getSummary(),
					ownership.getIssue().getPriority().getName() };
		}	
	}
	
}

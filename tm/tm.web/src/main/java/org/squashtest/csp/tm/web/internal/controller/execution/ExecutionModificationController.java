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
package org.squashtest.csp.tm.web.internal.controller.execution;

/*
 * TODO : activate execution suppression once the service is ready
 *
 *
 */
import static org.squashtest.csp.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

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
import org.squashtest.csp.tm.domain.bugtracker.Issue;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.ExecutionModificationService;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTablePagedFilter;
import org.squashtest.tm.core.foundation.collection.Paging;

@Controller
@RequestMapping("/executions/{executionId}")
public class ExecutionModificationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionModificationController.class);

	private ExecutionModificationService executionModService;

	@Inject
	private MessageSource messageSource;

	@ServiceReference
	public void setIterationModificationService(ExecutionModificationService iterationModificationService) {
		this.executionModService = iterationModificationService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView getExecution(@PathVariable long executionId) {
		Execution execution = executionModService.findAndInitExecution(executionId);
		int rank = executionModService.findExecutionRank(executionId);

		LOGGER.trace("ExecutionModService : getting execution {}, rank {}", executionId, rank);

		ModelAndView mav = new ModelAndView("page/campaign-libraries/show-execution");

		mav.addObject("execution", execution);
		mav.addObject("executionRank", Integer.valueOf(rank + 1));

		return mav;

	}

	@RequestMapping(value = "/steps", method = RequestMethod.GET, params = "sEcho")
	@ResponseBody
	public DataTableModel getStepsTableModel(@PathVariable long executionId, DataTableDrawParameters params,
			final Locale locale) {
		LOGGER.trace("ExecutionModificationController: getStepsTableModel called ");

		Paging filter = createPaging(params);

		FilteredCollectionHolder<List<ExecutionStep>> holder = executionModService.findExecutionSteps(executionId,
				filter);

		return new ManualExecutionStepDataTableModelHelper(locale, messageSource).buildDataModel(holder,
				filter.getFirstItemIndex() + 1, params.getsEcho());

	}
	private static class ExecutionStepDataTableModelHelper extends DataTableModelHelper<ExecutionStep> {
		private Locale locale;
		private MessageSource messageSource;

		private ExecutionStepDataTableModelHelper(Locale locale, MessageSource messageSource) {
			this.locale = locale;
			this.messageSource = messageSource;
		}

		@Override
		public Map<String, Object> buildItemData(ExecutionStep item) {
			Map<String, Object> res = new HashMap<String, Object>();
			res.put(DataTableModelHelper.DEFAULT_ENTITY_ID_KEY, item.getId());
			res.put(DataTableModelHelper.DEFAULT_ENTITY_INDEX_KEY, item.getExecutionStepOrder() + 1);
			res.put("action", item.getAction());
			res.put("expected", item.getExpectedResult());
			res.put("last-exec-on", formatDate(item.getLastExecutedOn(), locale, messageSource));
			res.put("last-exec-by", item.getLastExecutedBy());
			res.put("comment", item.getComment());
			res.put("bugged", createBugList(item));
			res.put(DataTableModelHelper.DEFAULT_NB_ATTACH_KEY, item.getAttachmentList().size());
			res.put(DEFAULT_ATTACH_LIST_ID_KEY, item.getAttachmentList().getId());
			return res;
		}
	}
	private static class ManualExecutionStepDataTableModelHelper extends ExecutionStepDataTableModelHelper {
		private ManualExecutionStepDataTableModelHelper(Locale locale, MessageSource messageSource) {
			super(locale, messageSource);
		}

		@Override
		public Map<String, Object> buildItemData(ExecutionStep item) {
			Map<String, Object> res = super.buildItemData(item);
			res.put("status", localizedStatus(item.getExecutionStatus(), super.locale, super.messageSource));
			return res;
			
		}
	}

	@RequestMapping(value = "/auto-steps", method = RequestMethod.GET, params = "sEcho")
	@ResponseBody
	public DataTableModel getAutoStepsTableModel(@PathVariable long executionId, DataTableDrawParameters params,
			final Locale locale) {
		LOGGER.trace("ExecutionModificationController: getStepsTableModel called ");

		Paging filter = createPaging(params);

		FilteredCollectionHolder<List<ExecutionStep>> holder = executionModService.findExecutionSteps(executionId,
				filter);

		return new AutomatedExecutionStepDataTableModelHelper(locale, messageSource).buildDataModel(holder, filter.getFirstItemIndex() + 1, params.getsEcho());

	}

	private static class AutomatedExecutionStepDataTableModelHelper extends ExecutionStepDataTableModelHelper {
		private AutomatedExecutionStepDataTableModelHelper(Locale locale, MessageSource messageSource) {
			super(locale, messageSource);
		}

		@Override
		public Map<String, Object> buildItemData(ExecutionStep item) {
			Map<String, Object> res = super.buildItemData(item);
			res.put("status", "--");
			return res;
		}
	}

	private static String createBugList(ExecutionStep item) {
		StringBuffer toReturn = new StringBuffer();
		List<Issue> issueList = item.getIssueList().getAllIssues();
		if (issueList.size() > 0) {
			toReturn.append(issueList.get(0).getId());
		}
		for (int i = 1; i < issueList.size(); i++) {
			toReturn.append(',');
			toReturn.append(issueList.get(i).getId());
		}
		return toReturn.toString();
	}

	private Paging createPaging(final DataTableDrawParameters params) {
		return new DataTablePagedFilter(params);
	}

	@RequestMapping(value = "/steps/{stepId}/comment", method = RequestMethod.POST, params = { "id", VALUE })
	@ResponseBody
	String updateStepComment(@PathVariable Long stepId, @RequestParam(VALUE) String newComment) {
		executionModService.setExecutionStepComment(stepId, newComment);
		LOGGER.trace("ExecutionModificationController : updated comment for step " + stepId);
		return newComment;
	}

	private static String localizedStatus(ExecutionStatus status, Locale locale, MessageSource messageSource) {
		return messageSource.getMessage(status.getI18nKey(), null, locale);
	}

	// will return the iteration in a full page
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView showIterationInfo(@PathVariable long executionId) {

		Execution execution = executionModService.findAndInitExecution(executionId);
		int rank = executionModService.findExecutionRank(executionId);

		ModelAndView mav = new ModelAndView("page/campaign-libraries/show-execution");

		LOGGER.trace("ExecutionModService : getting info execution " + executionId + ", rank " + rank);

		if (execution != null) {
			mav.addObject("execution", execution);
			mav.addObject("executionRank", Integer.valueOf(rank + 1));
		} else {
			// TODO SHOULD THROW AN EXCEPTION INSTEAD
			execution = new Execution();
			execution.setName("Not found");
			execution.setDescription("This iteration either do not exists, or was removed");
			mav.addObject("execution", new Execution());
			mav.addObject("executionRank", -1);
		}
		return mav;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=execution-description", VALUE })
	@ResponseBody
	public String updateDescription(@RequestParam(VALUE) String newDescription, @PathVariable long executionId) {

		executionModService.setExecutionDescription(executionId, newDescription);
		LOGGER.trace("Execution " + executionId + ": updated description to " + newDescription);
		return newDescription;

	}

	@RequestMapping(value = "/general", method = RequestMethod.GET)
	public ModelAndView refreshGeneralInfos(@PathVariable long libraryId, @PathVariable long executionId) {

		Execution execution = executionModService.findAndInitExecution(executionId);

		ModelAndView mav = new ModelAndView("fragment/generics/execution-information-fragment");

		mav.addObject("auditableEntity", execution);

		return mav;
	}

	// still to be done.

	@RequestMapping(method = RequestMethod.DELETE)
	public @ResponseBody
	Object removeExecution(@PathVariable long executionId) {
		Execution execution = executionModService.findById(executionId);
		IterationTestPlanItem testPlan = execution.getTestPlan();
		Iteration iteration = testPlan.getIteration();
		executionModService.deleteExecution(execution);
		// final IterationTestPlanItem reTestPlanItem = testPlan;
		final Long reNewStartDate;
		if (iteration.getActualStartDate() != null) {
			reNewStartDate = iteration.getActualStartDate().getTime();
		} else {
			reNewStartDate = null;
		}
		final Long reNewEndDate;
		if (iteration.getActualEndDate() != null) {
			reNewEndDate = iteration.getActualEndDate().getTime();
		} else {
			reNewEndDate = null;
		}
		return new StartEndDate(reNewStartDate, reNewEndDate);
	}
	private static class StartEndDate{
		private Long newStartDate;
		private Long newEndDate;

		private StartEndDate(Long newStartDate, Long newEndDate){
			this.newStartDate = newStartDate;
			this.newEndDate = newEndDate;
		}
		@SuppressWarnings("unused")
		public Long getNewStartDate(){
			return this.newStartDate;
		}
		@SuppressWarnings("unused")
		public Long getNewEndDate() {
			return this.newEndDate;
		}
	}

	private static String formatDate(Date date, Locale locale, MessageSource messageSource) {
		try {
			String format = messageSource.getMessage("squashtm.dateformat", null, locale);
			return new SimpleDateFormat(format).format(date);
		} catch (Exception anyException) {
			return formatNoData(locale, messageSource);
		}

	}

	private static String formatNoData(Locale locale, MessageSource messageSource) {
		return messageSource.getMessage("squashtm.nodata", null, locale);
	}

}

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
package org.squashtest.tm.web.internal.controller.execution;

/*
 * TODO : activate execution suppression once the service is ready
 *
 *
 */
import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.service.customfield.CustomFieldHelper;
import org.squashtest.tm.service.customfield.CustomFieldHelperService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.customfield.DenormalizedFieldHelper;
import org.squashtest.tm.service.denormalizedfield.DenormalizedFieldValueManager;
import org.squashtest.tm.service.execution.ExecutionModificationService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.generic.DataTableColumnDefHelper;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.controller.widget.AoColumnDef;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldJsonConverter;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTablePaging;
import org.squashtest.tm.web.internal.model.json.JsonExecutionInfo;

@Controller
@RequestMapping("/executions/{executionId}")
public class ExecutionModificationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionModificationController.class);

	@Inject
	private ExecutionModificationService executionModService;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private DenormalizedFieldValueManager denormalizedFieldValueFinder;

	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentHelper;

	@Inject
	private InternationalizationHelper messageSource;

	// ****** custom field services ******************

	@Inject
	private CustomFieldHelperService cufHelperService;

	@Inject
	private CustomFieldValueFinderService cufValueService;

	@Inject
	private CustomFieldJsonConverter converter;

	// ****** /custom field services ******************

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView getExecution(@PathVariable long executionId) {
		// execution properties
		Execution execution = executionModService.findAndInitExecution(executionId);
		int rank = executionModService.findExecutionRank(executionId);
		LOGGER.trace("ExecutionModService : getting execution {}, rank {}", executionId, rank);
		List<DenormalizedFieldValue> values = denormalizedFieldValueFinder.findAllForEntity(execution);

		// step properties
		List<AoColumnDef> columnDefs;

		boolean hasCUF = cufHelperService.hasCustomFields(execution);
		List<CustomFieldValue> customFieldValues = cufHelperService.newHelper(execution).getCustomFieldValues();
		columnDefs = findColumnDefForSteps(execution);
		List<CustomFieldModel> dfvDefinitions = Collections.emptyList();

		if (!execution.getSteps().isEmpty()) {

			CustomFieldHelper<ExecutionStep> helper = cufHelperService.newHelper(execution.getSteps())
					.setRenderingLocations(RenderingLocation.STEP_TABLE).restrictToCommonFields();

			List<CustomFieldModel> cufDefinitions = convertToJsonCustomField(helper.getCustomFieldConfiguration());

			List<DenormalizedFieldValue> firstStepDfv = denormalizedFieldValueFinder.findAllForEntityAndRenderingLocation(execution.getSteps().get(0), RenderingLocation.STEP_TABLE);
			dfvDefinitions = convertToJsonCustomField(firstStepDfv);
			dfvDefinitions.addAll(cufDefinitions);
		}

		ModelAndView mav = new ModelAndView("page/campaign-libraries/show-execution");
		mav.addObject("execution", execution);
		mav.addObject("executionRank", Integer.valueOf(rank + 1));
		mav.addObject("denormalizedFieldValues", values);
		mav.addObject("stepsAoColumnDefs", JsonHelper.serialize(columnDefs));
		mav.addObject("attachmentSet", attachmentHelper.findAttachments(execution));
		mav.addObject("hasCUF", hasCUF);
		mav.addObject("executionCufValues", customFieldValues);

		mav.addObject("cufDefinitions", dfvDefinitions);
		return mav;

	}

	private List<CustomFieldModel> convertToJsonCustomField(Collection<CustomField> customFields) {
		List<CustomFieldModel> models = new ArrayList<CustomFieldModel>(customFields.size());
		for (CustomField field : customFields) {
			models.add(converter.toJson(field));
		}
		return models;
	}

	private List<CustomFieldModel> convertToJsonCustomField(List<DenormalizedFieldValue> denormalizedFields) {
		List<CustomFieldModel> models = new ArrayList<CustomFieldModel>(denormalizedFields.size());
		for (DenormalizedFieldValue field : denormalizedFields) {
			models.add(converter.toCustomFieldJsonModel(field));
		}
		return models;
	}


	private List<AoColumnDef> findColumnDefForSteps(Execution execution) {
		List<AoColumnDef> columnDefs;
		boolean editable = permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "EXECUTE", execution);
		boolean isBugtrackerConnected = execution.getProject().isBugtrackerConnected();
		columnDefs = new ExecutionStepTableColumnDefHelper().getAoColumnDfvDefs(editable, isBugtrackerConnected);
		return columnDefs;
	}

	@RequestMapping(value = "/steps", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getStepsTableModel(@PathVariable long executionId, DataTableDrawParameters params,
			final Locale locale) {
		LOGGER.trace("execsteps table : entering controller");

		Execution exec = executionModService.findById(executionId);
		Paging filter = createPaging(params);

		LOGGER.trace("execsteps table : fetching steps");
		PagedCollectionHolder<List<ExecutionStep>> holder = executionModService.findExecutionSteps(executionId,
				filter);
		LOGGER.trace("execsteps table : finished steps");
		
		
		LOGGER.trace("execsteps table : fetching cufs / deno");
		CustomFieldHelper<ExecutionStep> cufHelper = cufHelperService.newHelper(holder.getPagedItems())
																	.setRenderingLocations(RenderingLocation.STEP_TABLE).
																	restrictToCommonFields();
		
		List<CustomFieldValue> cufValues = cufHelper.getCustomFieldValues();
		int nbCufs = cufHelper.getCustomFieldConfiguration().size();
		
		DenormalizedFieldHelper<ExecutionStep> denoHelper = cufHelperService.newDenormalizedHelper(holder.getPagedItems())
																	.setRenderingLocations(RenderingLocation.STEP_TABLE);
		
		List<DenormalizedFieldValue> denoValues = denoHelper.getDenormalizedFieldValues();
		int nbDeno = denoHelper.getCustomFieldConfiguration().size();
		
		LOGGER.trace("execsteps table : finished cufs / deno");
		
		
		LOGGER.trace("execsteps table : creating model");
		ExecutionStepDataTableModelHelper tableHelper = new ExecutionStepDataTableModelHelper(locale, messageSource, exec.isAutomated());
		tableHelper.usingCustomFields(cufValues, nbCufs);
		tableHelper.usingDenormalizedFields(denoValues, nbDeno);
	
		DataTableModel model = tableHelper.buildDataModel(holder,  params.getsEcho());
		LOGGER.trace("execsteps table : finished model");
		
		return model;

	}
	

	@RequestMapping(value = "/auto-steps", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getAutoStepsTableModel(@PathVariable long executionId, DataTableDrawParameters params,
			final Locale locale) {
		
		return getStepsTableModel(executionId, params, locale);
	}


	private static final class ExecutionStepTableColumnDefHelper extends DataTableColumnDefHelper {
		private static final List<AoColumnDef> baseColumns = new ArrayList<AoColumnDef>(5);
		static {
			String smallWidth = "2em";
			// columns.add(new AoColumnDef(bVisible, bSortable, sClass, sWidth, mDataProp))
			baseColumns.add(new AoColumnDef(false, false, "", smallWidth, "entity-id"));// 0
			baseColumns.add(new AoColumnDef(true, false, "select-handle centered", smallWidth, "entity-index"));// 1
			baseColumns.add(new AoColumnDef(true, false, "", null, "action"));// 2
			baseColumns.add(new AoColumnDef(true, false, "", null, "expected"));// 3
			baseColumns.add(new AoColumnDef(true, false, "has-status", null, "status"));// 4
			baseColumns.add(new AoColumnDef(true, false, "", null, "last-exec-on"));// 5
			baseColumns.add(new AoColumnDef(true, false, "", null, "last-exec-by"));// 6
			baseColumns.add(new AoColumnDef(true, false, "smallfonts rich-editable-comment", null, "comment"));// 7
			baseColumns.add(new AoColumnDef(false, false, "bug-list", null, "bug-list"));// 8
			baseColumns.add(new AoColumnDef(true, false, "centered bug-button", smallWidth, "bug-button"));// 9
			baseColumns.add(new AoColumnDef(false, false, "", null, "nb-attachments"));// 10
			baseColumns.add(new AoColumnDef(true, false, "centered has-attachment-cell", smallWidth, "attach-list-id"));// 11
			baseColumns.add(new AoColumnDef(true, false, "centered run-step-button", smallWidth, "run-step-button"));// 12
		}
		private List<AoColumnDef> columns = new ArrayList<AoColumnDef>();

		private ExecutionStepTableColumnDefHelper() {
			columns.addAll(baseColumns);
		}

		private List<AoColumnDef> getAoColumnDfvDefs(boolean editable, boolean isBugtrackerConnected) {
			columns.get(columns.size() - 2).setbVisible(editable);
			columns.get(columns.size() - 4).setbVisible(editable && isBugtrackerConnected);
			addATargets(columns);
			return columns;
		}
	}

	static String createBugList(ExecutionStep item) {
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
		return new DataTablePaging(params);
	}

	@RequestMapping(value = "/steps/{stepId}/comment", method = RequestMethod.POST, params = { "id", VALUE })
	@ResponseBody
	String updateStepComment(@PathVariable Long stepId, @RequestParam(VALUE) String newComment) {
		executionModService.setExecutionStepComment(stepId, newComment);
		LOGGER.trace("ExecutionModificationController : updated comment for step " + stepId);
		return newComment;
	}

	@RequestMapping(value = "/steps/{stepId}/status", method = RequestMethod.GET)
	@ResponseBody
	String getStepStatus(@PathVariable("stepId") Long stepId) {
		return executionModService.findExecutionStepById(stepId).getExecutionStatus().toString();
	}

	static String localizedStatus(ExecutionStatus status, Locale locale, MessageSource messageSource) {
		return messageSource.getMessage(status.getI18nKey(), null, locale);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=execution-description", VALUE })
	@ResponseBody
	public String updateDescription(@RequestParam(VALUE) String newDescription, @PathVariable long executionId) {

		executionModService.setExecutionDescription(executionId, newDescription);
		LOGGER.trace("Execution " + executionId + ": updated description to " + newDescription);
		return newDescription;

	}

	@RequestMapping(value = "/general", method = RequestMethod.GET, produces=ContentTypes.APPLICATION_JSON)
	@ResponseBody
	public JsonExecutionInfo refreshGeneralInfos(@PathVariable long executionId) {

		Execution execution = executionModService.findAndInitExecution(executionId);
		return toJson(execution);

	}

	@RequestMapping(method = RequestMethod.DELETE)
	public @ResponseBody
	Object removeExecution(@PathVariable("executionId") long executionId) {
		Execution execution = executionModService.findById(executionId);
		IterationTestPlanItem testPlan = execution.getTestPlan();
		Iteration iteration = testPlan.getIteration();
		executionModService.deleteExecution(execution);
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


	// ************* private stuffs *************

	private JsonExecutionInfo toJson(Execution exec){
		if (exec.isAutomated()){
			return new JsonExecutionInfo(
					exec.getLastExecutedOn(),
					exec.getLastExecutedBy(),
					exec.getExecutionStatus().getCanonicalStatus(),
					exec.getExecutionStatus(),
					exec.getAutomatedExecutionExtender().getResultURL()
					);
		}
		else{
			return new JsonExecutionInfo(
					exec.getLastExecutedOn(),
					exec.getLastExecutedBy(),
					exec.getExecutionStatus(),
					null,
					null
					);
		}
	}

	private static final class StartEndDate {
		private Long newStartDate;
		private Long newEndDate;

		private StartEndDate(Long newStartDate, Long newEndDate) {
			this.newStartDate = newStartDate;
			this.newEndDate = newEndDate;
		}

		@SuppressWarnings("unused")
		public Long getNewStartDate() {
			return this.newStartDate;
		}

		@SuppressWarnings("unused")
		public Long getNewEndDate() {
			return this.newEndDate;
		}
	}

}

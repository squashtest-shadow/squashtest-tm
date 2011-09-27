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
package org.squashtest.csp.tm.web.internal.controller.execution;

/*
 * TODO : activate execution suppression once the service is ready
 *
 *
 */

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionFilter;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.ExecutionModificationService;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTablePagedFilter;

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

	@RequestMapping(value="/steps", method=RequestMethod.GET)
	@ResponseBody
	public DataTableModel getStepsTableModel(@PathVariable long executionId,DataTableDrawParameters params, final Locale locale){
		LOGGER.trace("ExecutionModificationController: getStepsTableModel called ");

		CollectionFilter filter = createCollectionFilter(params);

		FilteredCollectionHolder<List<ExecutionStep>> holder = executionModService.getExecutionSteps(executionId, filter);


		return new DataTableModelHelper<ExecutionStep>(){
			@Override
			public Object[] buildItemData(ExecutionStep item) {
				return new Object[] { item.getId(),
						item.getExecutionStepOrder()+1,
						item.getAction(),
						item.getExpectedResult(),
						localizedStatus(item.getExecutionStatus(), locale),
						formatDate(item.getLastExecutedOn(),locale),
						item.getLastExecutedBy(),
						item.getComment(),
						item.getAttachmentCollection().size(),
						item.getAttachmentCollectionId()
						};
			}
		}.buildDataModel(holder, filter.getFirstItemIndex()+1, params.getsEcho());

	}


	private CollectionFilter createCollectionFilter(final DataTableDrawParameters params) {
		return new DataTablePagedFilter(params);
	}



	@RequestMapping(value = "/steps/{stepId}/comment", method = RequestMethod.POST, params = { "id", "value" })
	@ResponseBody
	String updateStepComment(@PathVariable Long stepId, @RequestParam("value") String newComment){
		executionModService.setExecutionStepComment(stepId, newComment);
		LOGGER.trace("ExecutionModificationController : updated comment for step " + stepId);
		return newComment;
	}





	private String localizedStatus(ExecutionStatus status, Locale locale){

		String toReturn;

		// TODO add a "localization key" property to status and remove this switch
		switch(status){
			case BLOQUED :
				toReturn=messageSource.getMessage("execution.combo.BLOQUED.label", null, locale);
				break;

			case FAILURE :
				toReturn=messageSource.getMessage("execution.combo.FAILURE.label", null, locale);
				break;

			case SUCCESS :
				toReturn=messageSource.getMessage("execution.combo.SUCCESS.label", null, locale);
				break;

			case RUNNING :
				toReturn=messageSource.getMessage("execution.combo.RUNNING.label", null, locale);
				break;

			case READY :
				toReturn=messageSource.getMessage("execution.combo.READY.label", null, locale);
				break;

			default :
				toReturn="unknown";
				break;
		}

		return toReturn;
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

	@RequestMapping(method = RequestMethod.POST, params = { "id=execution-description", "value" })
	@ResponseBody
	public String updateDescription(@RequestParam("value") String newDescription, @PathVariable long executionId) {

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
	String removeExecution(@PathVariable long executionId, HttpServletResponse response) {

		//todo

		response.setStatus(500);

		LOGGER.info("ExecutionModificationController : deleting " + executionId+ "NOT SUPPORED YET");

		return "Operation not supported yet";

	}

	private String formatDate(Date date, Locale locale){
		try{
			String format = messageSource.getMessage("squashtm.dateformat", null, locale);
			return new SimpleDateFormat(format).format(date);
		}
		catch(Exception anyException){
			return formatNoData(locale);
		}

	}
	

	private String formatNoData(Locale locale){
		return messageSource.getMessage("squashtm.nodata",null, locale);
	}
	

}

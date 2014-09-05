/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.controller.customfield;

import java.util.List;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;


@Controller
@RequestMapping("administration/projects/{projectId}/custom-fields-binding")
public class CustomFieldBindingManagerController {

	private CustomFieldBindingFinderService service;
	
	//private static final Logger LOGGER = LoggerFactory.getLogger(CustomFieldBindingManagerController.class);
	private static final int DEFAULT_PAGE_SIZE = 10;
	
	@ServiceReference
	public void setCustomFieldBindingFinderService(CustomFieldBindingFinderService service){
		this.service=service;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView getManager(@PathVariable("projectId") Long projectId){
		
		List<CustomFieldBinding> testCaseBindings = service.findCustomFieldsForProjectAndEntity
													(projectId, BindableEntity.TEST_CASE, new DefaultPagingAndSorting(DEFAULT_PAGE_SIZE)).getPagedItems();
		
		List<CustomFieldBinding> testStepBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.TEST_STEP);
		List<CustomFieldBinding> requirementBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.REQUIREMENT_VERSION);
		List<CustomFieldBinding> campaignBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.CAMPAIGN);
		List<CustomFieldBinding> iterationBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.ITERATION);
		List<CustomFieldBinding> testSuiteBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.TEST_SUITE);
		List<CustomFieldBinding> executionBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.EXECUTION);
		List<CustomFieldBinding> executionStepBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.EXECUTION_STEP);
		
		ModelAndView mav = new ModelAndView("project-tabs/custom-field-binding.html");
		mav.addObject("testCaseBindings", testCaseBindings);
		mav.addObject("testStepBindings", testStepBindings);
		mav.addObject("requirementBindings", requirementBindings);
		mav.addObject("campaignBindings", campaignBindings);
		mav.addObject("iterationBindings", iterationBindings);
		mav.addObject("testSuiteBindings", testSuiteBindings);
		mav.addObject("executionBindings", executionBindings);
		mav.addObject("executionStepBindings", executionStepBindings);
		
		mav.addObject("projectIdentifier", projectId);
		
		return mav;
		
	}


	
	
}

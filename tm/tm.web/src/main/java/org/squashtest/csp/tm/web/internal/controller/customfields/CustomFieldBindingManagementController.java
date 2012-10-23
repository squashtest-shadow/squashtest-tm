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
package org.squashtest.csp.tm.web.internal.controller.customfields;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.service.CustomFieldBindingModificationService;
import org.squashtest.csp.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.csp.tm.web.internal.model.customfields.CustomFieldBindingModel;
import org.squashtest.csp.tm.web.internal.model.customfields.CustomFieldJsonConverter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParametersPagingAdapter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;


@Controller
@RequestMapping("/custom-fields-binding")
public class CustomFieldBindingManagementController {

	private CustomFieldBindingModificationService service;

	private CustomFieldJsonConverter converter;
	
	@Inject
	private InternationalizationHelper messageSource;
	
	
	@ServiceReference
	public void setCustomFieldBindingModificationService(CustomFieldBindingModificationService service){
		this.service=service;
	}

	@PostConstruct
	public void init(){
		converter = new CustomFieldJsonConverter(messageSource);
	}
	

	
	@RequestMapping(value="/manager", method = RequestMethod.GET, params = {"projectId"})
	public ModelAndView getManager(@RequestParam("projectId") Long projectId){
		
		List<CustomFieldBinding> testCaseBindings = service.findCustomFieldsForProjectAndEntity
													(projectId, BindableEntity.TEST_CASE, new DefaultPaging())
													.getPagedItems();
		/*List<CustomFieldBinding> requirementBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.REQUIREMENT_VERSION);
		List<CustomFieldBinding> campaignBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.CAMPAIGN);
		List<CustomFieldBinding> iterationBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.ITERATION);
		List<CustomFieldBinding> testSuiteBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.TEST_SUITE);*/
		
		ModelAndView mav = new ModelAndView("custom-field-binding.html");
		mav.addObject("testCaseBindings", testCaseBindings);
		mav.addObject("projectId", projectId);
		
		return mav;
		
	}
	
	
	
	@RequestMapping(method= RequestMethod.GET, params = {"projectId"}, headers="Accept=application/json")
	@ResponseBody
	public List<CustomFieldBindingModel> findAllCustomFieldsForProject(@RequestParam("projectId") Long projectId){
		
		List<CustomFieldBinding> bindings = service.findCustomFieldsForProject(projectId);
		
		return toJson(bindings);
		
	}
	
			
	
	@RequestMapping(method= RequestMethod.GET, params = {"projectId", "bindableEntity"}, headers="Accept=application/json")
	@ResponseBody
	public List<CustomFieldBindingModel> findAllCustomFieldsForProject(@RequestParam("projectId") Long projectId, @RequestParam("bindableEntity") String bindableEntity){
		
		List<CustomFieldBinding> bindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.valueOf(bindableEntity));
		
		return toJson(bindings);
		
	}
	
	@RequestMapping(method= RequestMethod.GET, params = {"projectId", "bindableEntity", "sEcho", "params"})
	@ResponseBody
	public DataTableModel findAllCustomFieldsTableForProject
			(@RequestParam("projectId") Long projectId, 
			 @RequestParam("bindableEntity") String bindableEntity, 
			 DataTableDrawParameters params,
			 Locale locale){
		
		
		DataTableDrawParametersPagingAdapter paging = new DataTableDrawParametersPagingAdapter(params);
		
		PagedCollectionHolder<List<CustomFieldBinding>> bindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.valueOf(bindableEntity), paging);

		CUFBindingDataTableModelHelper helper = new CUFBindingDataTableModelHelper(converter);
		return helper.buildDataModel(bindings, params.getsEcho());
		
	}
	
	
	private List<CustomFieldBindingModel> toJson(List<CustomFieldBinding> bindings){
		List<CustomFieldBindingModel> result = new LinkedList<CustomFieldBindingModel>();
		
		for (CustomFieldBinding binding : bindings){
			CustomFieldBindingModel model = converter.toJson(binding);
			result.add(model);
		}
		
		return result;
	}

	
	// ************************* inner classes ****************************
	
	
	
	private static class CUFBindingDataTableModelHelper extends DataTableModelHelper<CustomFieldBinding> {
		private CustomFieldJsonConverter converter;

		private CUFBindingDataTableModelHelper(CustomFieldJsonConverter converter) {
			this.converter=converter;
		}
		
		@Override
		public Object buildItemData(CustomFieldBinding item) {
			return converter.toJson(item);
		}
	}

	private static class DefaultPaging implements Paging{
		@Override
		public int getFirstItemIndex() {
			return 0;
		}
		
		@Override
		public int getPageSize() {
			return 10;
		}
		
	}
	
}

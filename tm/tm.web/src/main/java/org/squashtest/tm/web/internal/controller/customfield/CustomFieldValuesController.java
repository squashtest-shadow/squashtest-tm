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
package org.squashtest.tm.web.internal.controller.customfield;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.service.customfield.CustomFieldValueManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldJsonConverter;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldValueConfigurationBean;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldValueModel;


@Controller
@RequestMapping("/custom-fields/values")
public class CustomFieldValuesController {

	private CustomFieldValueManagerService managerService;
	
	private PermissionEvaluationService permissionService;

	@Inject
	private CustomFieldJsonConverter converter;
	
	@Inject
	private MessageSource messageSource;
	
	@ServiceReference
	public void setManagerService(CustomFieldValueManagerService managerService) {
		this.managerService = managerService;
	}
	
	@ServiceReference
	public void setPermissionService(PermissionEvaluationService permissionService) {
		this.permissionService = permissionService;
	}




	@RequestMapping(method=RequestMethod.GET, params = {"boundEntityId","boundEntityType"} , headers="Accept=application/json")
	@ResponseBody
	public List<CustomFieldValueModel> getCustomFieldValuesForEntity(@RequestParam("boundEntityId") Long id, @RequestParam("boundEntityType") BindableEntity entityType){
		
		List<CustomFieldValue> values = managerService.findAllCustomFieldValues(id, entityType);
		
		return valuesToJson(values);
		
		
	}
	
	
	@RequestMapping(method=RequestMethod.GET, params = {"boundEntityId", "boundEntityType"})
	public ModelAndView getCustomFieldValuesPanel(@RequestParam("boundEntityId") Long id, @RequestParam("boundEntityType") BindableEntity entityType, Locale locale){
		
		List<CustomFieldValue> values = managerService.findAllCustomFieldValues(id, entityType);
		
		boolean editable = permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "SMALL_EDIT", id, entityType.getReferencedClass().getName());
		
		CustomFieldValueConfigurationBean conf = new CustomFieldValueConfigurationBean(values);
		
		ModelAndView mav = new ModelAndView("custom-field-values-panel.html");
		mav.addObject("editable", editable);
		mav.addObject("configuration", conf);
		
		return mav;
		
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.POST, params="value")
	@ResponseBody
	public void updateCustomValue(@PathVariable("id") Long valueId, @RequestParam("value") String value){
		managerService.update(valueId, value);
	}
	
	
	private List<CustomFieldValueModel> valuesToJson(List<CustomFieldValue> values){
		List<CustomFieldValueModel> models = new LinkedList<CustomFieldValueModel>();
		
		for (CustomFieldValue value : values){
			CustomFieldValueModel model = converter.toJson(value);
			models.add(model);
		}
		
		return models;
	}
	
	
	
}
